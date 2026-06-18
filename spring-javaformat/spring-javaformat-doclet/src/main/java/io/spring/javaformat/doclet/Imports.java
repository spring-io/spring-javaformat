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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;

/**
 * Imports from a compilation unit.
 *
 * @author Phillip Webb
 */
final class Imports {

	private final Map<String, String> imports;

	private Imports(Map<String, String> imports) {
		this.imports = imports;
	}

	public static Imports of(TreePath path) {
		return of(path.getCompilationUnit());
	}

	private static Imports of(CompilationUnitTree compilationUnit) {
		Map<String, String> imports = new HashMap<>();
		new TreeScanner<Void, Void>() {

			@Override
			public Void visitImport(ImportTree node, Void p) {
				String qualifiedName = node.getQualifiedIdentifier().toString();
				String shortName = qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
				imports.put(shortName, qualifiedName);
				return super.visitImport(node, p);
			};

		}.scan(compilationUnit, null);
		return new Imports(Collections.unmodifiableMap(imports));
	}

	/**
	 * Return the qualified version of the name if possible.
	 * @param name the short name
	 * @return the qualified name or the original name
	 */
	public String qualified(String name) {
		return this.imports.getOrDefault(name, name);
	}

}
