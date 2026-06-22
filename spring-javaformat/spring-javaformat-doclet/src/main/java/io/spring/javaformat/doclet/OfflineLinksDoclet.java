/*
 * Copyright 2017-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.javaformat.doclet;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.util.DocTreeScanner;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.StandardDoclet;

/**
 * {@link StandardDoclet} designed to improve the process of working with offline links.
 * Allows {@code -linkoffline} locations to be templated against source directories, and
 * verifies that links actually point to known packages.
 *
 * to check that packages from link tags are found. Currenlt only offline links are
 * checked.
 *
 * @author Phillip Webb
 */
public class OfflineLinksDoclet extends StandardDoclet {

	private final Set<String> sources = new HashSet<>();

	private final Set<String> ignoredPackages = new HashSet<>();

	private final Set<OfflineLinkOption> offlineLinkOptions = new LinkedHashSet<>();

	private boolean debug;

	@Override
	public Set<? extends Option> getSupportedOptions() {
		TreeSet<Option> options = new TreeSet<>();
		options.add(new DocletOption("-offlinelinks-source", 1, "a source directory used to build offline links",
				"<the source directory with '@name@' replaced by the location name>", this::source));
		options.add(new DocletOption("-offlinelinks-ignore-packages", 1, "ignores specific packages from link checking",
				"<comma separated list of packages>", this::ingorePackages));
		options.add(new DocletOption("-offlinelinks-debug", 0, "output debug logging when processing offline links", "",
				(name, arguments) -> this.debug = true));
		super.getSupportedOptions().stream()
			.map((option) -> new DockletOptionWrapper(option, this::processOption))
			.forEach(options::add);
		return options;
	}

	private void source(String name, List<String> arguments) {
		this.sources.addAll(commaDelimitedListToSet(arguments.get(0)));
	}

	private void ingorePackages(String name, List<String> arguments) {
		this.ignoredPackages.addAll(commaDelimitedListToSet(arguments.get(0)));
	}

	private boolean processOption(DockletOptionWrapper wrapper, List<String> arguments) {
		if ("-linkoffline".equals(wrapper.getNames().get(0))) {
			this.offlineLinkOptions.add(new OfflineLinkOption(wrapper.option(), OfflineLink.fromArguments(arguments)));
			return true;
		}
		return false;
	}

	@Override
	public boolean run(DocletEnvironment environment) {
		Set<OfflineLink> offlineLinks = setupOfflineLinks();
		check(environment, offlineLinks);
		return super.run(environment);
	}

	private Set<OfflineLink> setupOfflineLinks() {
		Set<OfflineLink> offlineLinks = new LinkedHashSet<>();
		for (OfflineLinkOption offlineLinkOption : this.offlineLinkOptions) {
			for (OfflineLink offlineLink : expand(offlineLinkOption.offlineLink())) {
				offlineLinks.add(offlineLink);
				offlineLinkOption.option().process("-linkoffline", List.of(offlineLink.url(), offlineLink.location()));
			}
		}
		return offlineLinks;
	}

	private Set<OfflineLink> expand(OfflineLink offlineLink) {
		Path workingDirectory = Path.of(".");
		Set<OfflineLink> expanded = new LinkedHashSet<>();
		for (String name : commaDelimitedListToSet(offlineLink.location())) {
			this.sources.stream().flatMap((source) -> commaDelimitedListToSet(source).stream()).forEach((source) -> {
				String location = source.replace("@name@", name);
				if (Files.isDirectory(workingDirectory.resolve(location))) {
					expanded.add(new OfflineLink(offlineLink.url(), location));
				}
			});
		}
		return (!expanded.isEmpty()) ? expanded : Set.of(offlineLink);
	}

	private void check(DocletEnvironment environment, Set<OfflineLink> offlineLinks) {
		ExternalLinks externalLinks = new ExternalLinks(environment, offlineLinks);
		for (Element element : environment.getIncludedElements()) {
			DocTrees trees = environment.getDocTrees();
			DocCommentTree commentTree = trees.getDocCommentTree(element);
			TreePath path = trees.getPath(element);
			if (path == null) {
				continue;
			}
			Imports imports = Imports.of(path);
			Messages messages = Messages.of(environment, commentTree, path, this.debug);
			new DocTreeScanner<Void, Void>() {

				@Override
				public Void visitLink(LinkTree node, Void p) {
					check(environment, externalLinks, imports, messages, node);
					return null;
				}

			}.scan(commentTree, null);
		}
	}

	private void check(DocletEnvironment environment, ExternalLinks externalLinks, Imports imports, Messages messages,
			LinkTree link) {
		String reference = link.getReference().getSignature();
		int hashIndex = reference.indexOf("#");
		if (hashIndex == 0) {
			messages.note(link, "Not checking method link '%s'".formatted(reference));
			return;
		}
		if (hashIndex > 0) {
			reference = reference.substring(0, hashIndex - 1);
		}
		String qualifiedReference = imports.qualified(reference);
		if (!qualifiedReference.contains(".")) {
			messages.note(link, "Not checking same package link '%s'".formatted(reference));
			return;
		}
		TypeElement type = environment.getElementUtils().getTypeElement(qualifiedReference);
		if (type == null || environment.isIncluded(type)) {
			messages.note(link, "Not checking internal link '%s'".formatted(qualifiedReference));
			return;
		}
		String packageName = environment.getElementUtils().getPackageOf(type).getQualifiedName().toString();
		if (packageName.startsWith("java.")) {
			messages.note(link, "Not checking java package link '%s'".formatted(qualifiedReference));
			return;
		}
		if (this.ignoredPackages.contains(packageName)) {
			messages.note(link, "Not checking excluded package link '%s'".formatted(qualifiedReference));
			return;
		}
		messages.note(link, "Checking offline link '%s'".formatted(qualifiedReference));
		if (!externalLinks.includesPackage(packageName)) {
			messages.warn(link, "No external link found for '%s'".formatted(qualifiedReference));
		}
	};

	Set<String> commaDelimitedListToSet(String string) {
		return Arrays.stream(string.split(","))
			.map(String::trim)
			.filter(Predicate.not(String::isEmpty))
			.collect(Collectors.toCollection(HashSet::new));
	}

	/**
	 * Interface used to provide messages.
	 */
	@FunctionalInterface
	private interface Messages {

		default void note(DocTree node, String message) {
			print(Kind.NOTE, node, message);
		}

		default void warn(DocTree node, String message) {
			print(Kind.WARNING, node, message);
		}

		void print(Kind kind, DocTree node, String message);

		static Messages of(DocletEnvironment environment, DocCommentTree commentTree, TreePath path,
				boolean includeNotes) {
			return (kind, node, message) -> {
				if (kind != Kind.NOTE || includeNotes) {
					environment.getDocTrees().printMessage(kind, message, node, commentTree, path.getCompilationUnit());
				}
			};
		}

	}

	private record OfflineLinkOption(Option option, OfflineLink offlineLink) {

	}

}
