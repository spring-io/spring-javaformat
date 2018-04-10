/*
 * Copyright 2012-2016 the original author or authors.
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
 * Checks that lambda definitions follow Spring conventions. Single argument lambda
 * parameters should have parentheses. Single statement implementations should not use
 * curly braces.
 *
 * @author Phillip Webb
 */
public class LambdaParenthesesCheck extends AbstractCheck {

	@Override
	public int[] getDefaultTokens() {
		return getAcceptableTokens();
	}

	@Override
	public int[] getAcceptableTokens() {
		return new int[] { TokenTypes.LAMBDA };

	}

	@Override
	public int[] getRequiredTokens() {
		return CommonUtils.EMPTY_INT_ARRAY;
	}

	@Override
	public void visitToken(DetailAST ast) {
		if (ast.getType() == TokenTypes.LAMBDA) {
			visitLambda(ast);
		}
	}

	private void visitLambda(DetailAST lambda) {
		if (!hasToken(lambda, TokenTypes.LPAREN)) {
			log(lambda.getLineNo(), lambda.getColumnNo(), "lambda.missingParen");
		}
		DetailAST block = lambda.getLastChild();
		if (block.getType() == TokenTypes.SLIST
				&& block.getChildCount(TokenTypes.SEMI) == 0) {
			log(block.getLineNo(), block.getColumnNo(), "lambda.unnecessaryBlock");
		}
	}

	private boolean hasToken(DetailAST ast, int type) {
		return ast.findFirstToken(type) != null;
	}

}
