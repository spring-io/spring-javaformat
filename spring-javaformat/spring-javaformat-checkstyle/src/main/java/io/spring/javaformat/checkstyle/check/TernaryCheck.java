/*
 * Copyright 2017-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.javaformat.checkstyle.check;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Checks that ternary operations follow Spring conventions. All ternary operators should
 * have parentheses. Not equals to should be used rather than equals as the test.
 *
 * @author Phillip Webb
 */
public class TernaryCheck extends AbstractCheck {

	@Override
	public int[] getDefaultTokens() {
		return getAcceptableTokens();
	}

	@Override
	public int[] getAcceptableTokens() {
		return new int[] { TokenTypes.QUESTION };
	}

	@Override
	public int[] getRequiredTokens() {
		return CommonUtils.EMPTY_INT_ARRAY;
	}

	@Override
	public void visitToken(DetailAST ast) {
		if (ast.getType() == TokenTypes.QUESTION) {
			visitQuestion(ast);
		}
	}

	private void visitQuestion(DetailAST ast) {
		if (!hasType(ast.getPreviousSibling(), TokenTypes.LPAREN)
				|| !hasType(ast.getNextSibling(), TokenTypes.RPAREN)) {
			log(ast.getLineNo(), ast.getColumnNo(), "ternary.missingParen");
		}
		if (hasType(ast.getFirstChild(), TokenTypes.EQUAL)) {
			log(ast.getLineNo(), ast.getColumnNo(), "ternary.equalOperator");
		}
	}

	private boolean hasType(DetailAST ast, int type) {
		return ast != null && ast.getType() == type;
	}

}
