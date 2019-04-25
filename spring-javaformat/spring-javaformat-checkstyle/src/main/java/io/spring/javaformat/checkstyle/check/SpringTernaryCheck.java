/*
 * Copyright 2017-2019 the original author or authors.
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

import java.util.Locale;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Checks that ternary operations follow Spring conventions. All ternary tests should have
 * parentheses. Not equals to should be used rather than equals as the test for nulls.
 *
 * @author Phillip Webb
 */
public class SpringTernaryCheck extends AbstractSpringCheck {

	private EqualsTest equalsTest = EqualsTest.NEVER_FOR_NULLS;

	@Override
	public int[] getAcceptableTokens() {
		return new int[] { TokenTypes.QUESTION };
	}

	@Override
	public void visitToken(DetailAST ast) {
		if (ast.getType() == TokenTypes.QUESTION) {
			visitQuestion(ast);
		}
	}

	private void visitQuestion(DetailAST ast) {
		DetailAST expression = ast.getFirstChild();
		if (!hasType(expression, TokenTypes.LPAREN)) {
			if (expression != null && requiresParens(expression)) {
				log(ast.getLineNo(), ast.getColumnNo(), "ternary.missingParen");
			}
		}
		while (hasType(expression, TokenTypes.LPAREN)) {
			expression = expression.getNextSibling();
		}
		if (isSimpleEqualsExpression(expression) && !isEqualsTestAllowed(ast)) {
			log(ast.getLineNo(), ast.getColumnNo(), "ternary.equalOperator");
		}
	}

	private boolean requiresParens(DetailAST expression) {
		if (expression != null && expression.getChildCount() > 1) {
			switch (expression.getType()) {
			case TokenTypes.METHOD_CALL:
			case TokenTypes.DOT:
				return false;
			}
			return true;
		}
		return false;
	}

	private boolean isSimpleEqualsExpression(DetailAST expression) {
		if (expression == null || expression.getType() != TokenTypes.EQUAL) {
			return false;
		}
		DetailAST child = expression.getFirstChild();
		while (child != null) {
			if (child.getChildCount() > 0) {
				return false;
			}
			child = child.getNextSibling();
		}
		return true;
	}

	private boolean isEqualsTestAllowed(DetailAST ast) {
		switch (this.equalsTest) {
		case ANY:
			return true;
		case NEVER:
			return false;
		case NEVER_FOR_NULLS:
			DetailAST equal = ast.findFirstToken(TokenTypes.EQUAL);
			return equal.findFirstToken(TokenTypes.LITERAL_NULL) == null;
		}
		throw new IllegalStateException("Unsupported equals test " + this.equalsTest);
	}

	private boolean hasType(DetailAST ast, int type) {
		return (ast != null && ast.getType() == type);
	}

	public void setEqualsTest(String equalsTest) {
		try {
			this.equalsTest = Enum.valueOf(EqualsTest.class, equalsTest.trim().toUpperCase(Locale.ENGLISH));
		}
		catch (final IllegalArgumentException ex) {
			throw new IllegalArgumentException("unable to parse " + equalsTest, ex);
		}
	}

	/**
	 * Type of equals operators allowed in the test condition.
	 */
	public enum EqualsTest {

		/**
		 * Equals checks can be used for any test.
		 */
		ANY,

		/**
		 * Equals tests can never be used.
		 */
		NEVER,

		/**
		 * Equals tests can never be used for {@code null} checks.
		 */
		NEVER_FOR_NULLS

	}

}
