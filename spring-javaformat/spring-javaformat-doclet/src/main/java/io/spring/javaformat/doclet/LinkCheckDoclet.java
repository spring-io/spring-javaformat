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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
 * {@link StandardDoclet} to check that packages from link tags are found. Currenlt only
 * offline links are checked.
 *
 * @author Phillip Webb
 */
public class LinkCheckDoclet extends StandardDoclet {

	private final Set<OfflineLink> offlineLinks = new HashSet<>();

	private final Set<String> excludedPackages = new HashSet<>();

	private boolean debug;

	@Override
	public Set<? extends Option> getSupportedOptions() {
		TreeSet<Option> options = new TreeSet<>();
		super.getSupportedOptions().stream()
			.map((option) -> new WatchedDocletOption(option, this::onProcessOption))
			.forEach(options::add);
		options.add(new DocletOption("-linkcheck-exclude", 1, "excludes specific packages from link checking",
				"<comma separated list of packages>", this::processExclude));
		options.add(new DocletOption("-linkcheck-debug", 0, "debugs logging for link checking", "",
				(name, arguments) -> this.debug = true));
		return options;
	}

	private void onProcessOption(String name, List<String> arguments) {
		if ("-linkoffline".equals(name)) {
			this.offlineLinks.add(OfflineLink.fromArguments(arguments));
		}
	}

	private void processExclude(String name, List<String> arguments) {
		this.excludedPackages.addAll(Arrays.asList(arguments.get(0).split(",")));
	}

	@Override
	public boolean run(DocletEnvironment environment) {
		check(environment);
		return super.run(environment);
	}

	private void check(DocletEnvironment environment) {
		ExternalLinks externalLinks = new ExternalLinks(environment, this.offlineLinks);
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
		if (this.excludedPackages.contains(packageName)) {
			messages.note(link, "Not checking excluded package link '%s'".formatted(qualifiedReference));
			return;
		}
		messages.note(link, "Checking offline link '%s'".formatted(qualifiedReference));
		if (!externalLinks.includesPackage(packageName)) {
			messages.warn(link, "No external link found for '%s'".formatted(qualifiedReference));
		}
	};

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

}
