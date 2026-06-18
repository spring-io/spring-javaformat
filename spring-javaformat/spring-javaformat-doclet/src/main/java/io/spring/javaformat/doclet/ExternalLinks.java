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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jdk.javadoc.doclet.DocletEnvironment;

/**
 * External links discovered by loading element and package lists from the offline links.
 *
 * @author Phillip Webb
 */
class ExternalLinks {

	private static final String MODULE_PREFIX = "module:";

	private final Set<String> elements;

	ExternalLinks(DocletEnvironment environment, Set<OfflineLink> offlineLinks) {
		Set<String> elements = new HashSet<>();
		Path workingDirectory = Path.of(".");
		for (OfflineLink offlineLink : offlineLinks) {
			Path path = workingDirectory.resolve(Path.of(offlineLink.location()));
			elements.addAll(load(path.resolve("element-list")));
			elements.addAll(load(path.resolve("package-list")));
		}
		this.elements = Collections.unmodifiableSet(elements);
	}

	private Collection<String> load(Path path) {
		try {
			return (!Files.isReadable(path)) ? Collections.emptySet()
					: Files.readAllLines(path).stream().map(this::stripModulePrefix).toList();
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private String stripModulePrefix(String string) {
		return (string.startsWith(MODULE_PREFIX)) ? string.substring(MODULE_PREFIX.length()) : string;
	}

	boolean includesPackage(String packageName) {
		return this.elements.contains(packageName);
	}

}
