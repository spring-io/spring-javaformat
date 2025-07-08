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
import java.util.Collections;
import java.util.List;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Spring-specific check of the concision of an annotation's attribute values.
 *
 * @author Andy Wilkinson
 */
public class SpringAnnotationAttributeConciseValueCheck extends AbstractCheck {

	private final List<ImportStatement> imports = new ArrayList<>();

	@Override
	public int[] getDefaultTokens() {
		return getRequiredTokens();
	}

	@Override
	public int[] getAcceptableTokens() {
		return getRequiredTokens();
	}

	@Override
	public int[] getRequiredTokens() {
		return new int[] { TokenTypes.ANNOTATION, TokenTypes.IMPORT };
	}

	@Override
	public void init() {
		this.imports.clear();
	}

	@Override
	public void visitToken(DetailAST ast) {
		if (ast.getType() == TokenTypes.IMPORT) {
			visitImport(ast);
		}
		else if (ast.getType() == TokenTypes.ANNOTATION) {
			visitAnnotation(ast);
		}
	}

	private void visitImport(DetailAST importNode) {
		List<String> components = dotSeparatedComponents(importNode.getFirstChild());
		if (components != null) {
			this.imports.add(new ImportStatement(components));
		}
	}

	private void visitAnnotation(DetailAST annotation) {
		int valuePairCount = annotation.getChildCount(TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR);
		if (valuePairCount == 0) {
			DetailAST valueExpression = annotation.findFirstToken(TokenTypes.EXPR);
			visitValueExpression(valueExpression, annotation, "value");
		}
		else {
			DetailAST candidate = annotation.getFirstChild();
			while (candidate != null) {
				if (candidate.getType() == TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR) {
					visitMemberValuePair(candidate);
				}
				candidate = candidate.getNextSibling();
			}
		}
	}

	private void visitMemberValuePair(DetailAST annotationValue) {
		DetailAST annotation = annotationValue.getParent();
		DetailAST attribute = annotationValue.findFirstToken(TokenTypes.IDENT);
		DetailAST valueExpression = annotationValue.findFirstToken(TokenTypes.EXPR);
		visitValueExpression(valueExpression, annotation, attribute.getText());
	}

	private void visitValueExpression(DetailAST valueExpression, DetailAST annotation, String attributeName) {
		if (valueExpression != null && valueExpression.getChildCount() == 1) {
			List<String> expressionComponents = dotSeparatedComponents(valueExpression.getFirstChild());
			if (expressionComponents != null && expressionComponents.size() > 2) {
				String outerTypeName = expressionComponents.get(0);
				String annotationName = annotation.findFirstToken(TokenTypes.IDENT).getText();
				if (outerTypeName.equals(annotationName)) {
					String innerTypeName = expressionComponents.get(1);
					if (!existingClashingImport(outerTypeName, innerTypeName)) {
						String toImport = outerTypeName + "." + innerTypeName;
						String replacement = String.join(".", expressionComponents.subList(1, expressionComponents.size()));
						log(valueExpression.getLineNo(), valueExpression.getColumnNo(),
								"annotation.attribute.overlyVerboseValue", attributeName, toImport, replacement);
					}
				}
			}
		}
	}

	private List<String> dotSeparatedComponents(DetailAST ast) {
		if (ast.getType() == TokenTypes.IDENT) {
			return Collections.singletonList(ast.getText());
		}
		if (ast.getType() == TokenTypes.DOT) {
			List<String> left = dotSeparatedComponents(ast.getFirstChild());
			List<String> right = dotSeparatedComponents(ast.getLastChild());
			if (left != null && right != null) {
				List<String> components = new ArrayList<>();
				components.addAll(left);
				components.addAll(right);
				return components;
			}
		}
		return null;
	}

	private boolean existingClashingImport(String outer, String inner) {
		return this.imports.stream().filter((imported) -> imported.clashesWith(outer, inner)).findFirst().isPresent();
	}

	static class ImportStatement {

		private final String imported;

		ImportStatement(List<String> components) {
			this.imported = String.join(".", components);
		}

		boolean clashesWith(String outer, String inner) {
			return this.imported.endsWith("." + inner) && !this.imported.endsWith("." + outer + "." + inner);
		}

	}

}
