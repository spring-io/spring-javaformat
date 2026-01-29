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

package io.spring.javaformat.checkstyle.check;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Checks compliance with Spring team's nullability conventions. JSpecify annotations
 * should be used to express nullability and type-use annotations ({@code @Nullable}
 * and {@code @NonNull}) should appear immediately before a type.
 *
 * @author Andy Wilkinson
 */
public class SpringNullabilityCheck extends AbstractSpringCheck {

	private final Set<String> unwantedNullabilityImports = new HashSet<>();

	private final List<DetailAST> modifiers = new ArrayList<>();

	@Override
	public int[] getAcceptableTokens() {
		return new int[] { TokenTypes.IMPORT, TokenTypes.MODIFIERS };
	}

	@Override
	public void beginTree(DetailAST rootAST) {
		this.modifiers.clear();
		this.unwantedNullabilityImports.clear();
	}

	@Override
	public void finishTree(DetailAST rootAST) {
		this.modifiers.forEach(this::visitModifiers);
	}

	@Override
	public void visitToken(DetailAST ast) {
		switch (ast.getType()) {
			case TokenTypes.IMPORT:
				visitImport(ast);
				break;
			case TokenTypes.MODIFIERS:
				this.modifiers.add(ast);
				break;
		}
	}

	private void visitImport(DetailAST ast) {
		FullIdent ident = FullIdent.createFullIdentBelow(ast);
		if (!isFullyQualifiedJSpecifyAnnotation(ident)) {
			String simpleName = simpleNameOf(ident);
			for (JSpecifyAnnotation annotation: JSpecifyAnnotation.values()) {
				if (annotation.replaces.contains(simpleName)) {
					log(ident.getLineNo(), ident.getColumnNo(), "nullability.bannedImport", ident.getText(), annotation.name);
					this.unwantedNullabilityImports.add(simpleName);
				}
			}
		}
	}

	private boolean isFullyQualifiedJSpecifyAnnotation(FullIdent ident) {
		for (JSpecifyAnnotation annotation: JSpecifyAnnotation.values()) {
			if (ident.getText().equals(annotation.name)) {
				return true;
			}
		}
		return false;
	}

	private String simpleNameOf(FullIdent ident) {
		String identText = ident.getText();
		return identText.substring(identText.lastIndexOf(".") + 1);
	}

	private void visitModifiers(DetailAST ast) {
		DetailAST annotation = ast.findFirstToken(TokenTypes.ANNOTATION);
		if (annotation != null) {
			DetailAST ident = annotation.findFirstToken(TokenTypes.IDENT);
			if (ident != null) {
				String identText = ident.getText();
				DetailAST lastChild = ast.getLastChild();
				if (isJSpecifyAnnotation(ident) && !annotation.equals(lastChild)) {
					log(annotation.getLineNo(), annotation.getColumnNo(), "nullability.annotationLocation", identText);
				}
			}
		}
	}

	private boolean isJSpecifyAnnotation(DetailAST ident) {
		for (JSpecifyAnnotation annotation: JSpecifyAnnotation.values()) {
			if (ident.getText().equals(annotation.simpleName) && !this.unwantedNullabilityImports.contains(ident.getText())) {
				return true;
			}
		}
		return false;
	}

	private enum JSpecifyAnnotation {

		NULLABLE("Nullable", "Nullable"),

		NON_NULL("NonNull", "NonNull", "Nonnull");

		private final String simpleName;

		private final String name;

		private Set<String> replaces;

		JSpecifyAnnotation(String simpleName, String... replaces) {
			this.simpleName = simpleName;
			this.name = "org.jspecify.annotations." + simpleName;
			this.replaces = new HashSet<>(Arrays.asList(replaces));
		}

	}

}
