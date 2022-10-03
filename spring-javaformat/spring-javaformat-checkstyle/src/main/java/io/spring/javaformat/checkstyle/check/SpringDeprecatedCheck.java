/*
 * Copyright 2017-2022 the original author or authors.
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

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Checks that {@link Deprecated @Deprecated} annotations follow Spring conventions.
 *
 * @author Andy Wilkinson
 * @since 0.0.35
 */
public class SpringDeprecatedCheck extends AbstractSpringCheck {

	@Override
	public int[] getAcceptableTokens() {
		return new int[] { TokenTypes.ANNOTATION };
	}

	@Override
	public void visitToken(DetailAST ast) {
		if (ast.getType() == TokenTypes.ANNOTATION) {
			visitAnnotation(ast);
		}
	}

	private void visitAnnotation(DetailAST annotation) {
		String text = FullIdent.createFullIdent(annotation.findFirstToken(TokenTypes.AT).getNextSibling()).getText();
		if ("Deprecated".equals(text) || "java.lang.Deprecated".equals(text)) {
			visitDeprecated(annotation);
		}
	}

	private void visitDeprecated(DetailAST deprecated) {
		DetailAST sinceAttribute = findSinceAttribute(deprecated);
		if (sinceAttribute == null) {
			log(deprecated.getLineNo(), deprecated.getColumnNo(), "deprecated.missingSince");
		}
		else {
			DetailAST expr = sinceAttribute.findFirstToken(TokenTypes.EXPR);
			DetailAST sinceLiteral = expr.findFirstToken(TokenTypes.STRING_LITERAL);
			if ("\"\"".equals(sinceLiteral.getText())) {
				log(deprecated.getLineNo(), deprecated.getColumnNo(), "deprecated.emptySince");
			}
		}
	}

	private DetailAST findSinceAttribute(DetailAST deprecated) {
		DetailAST child = deprecated.getFirstChild();
		while (child != null) {
			if (child.getType() == TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR) {
				DetailAST attributeIdent = child.findFirstToken(TokenTypes.IDENT);
				if (attributeIdent != null && ("since".equals(attributeIdent.getText()))) {
					return child;
				}
			}
			child = child.getNextSibling();
		}
		return null;
	}

}
