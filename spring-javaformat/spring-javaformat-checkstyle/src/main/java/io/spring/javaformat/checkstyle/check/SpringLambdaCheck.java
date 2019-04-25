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

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Checks that lambda definitions follow Spring conventions. Single argument lambda
 * parameters should have parentheses. Single statement implementations should not use
 * curly braces.
 *
 * @author Phillip Webb
 */
public class SpringLambdaCheck extends AbstractSpringCheck {

	private boolean singleArgumentParentheses = true;

	@Override
	public int[] getAcceptableTokens() {
		return new int[] { TokenTypes.LAMBDA };

	}

	@Override
	public void visitToken(DetailAST ast) {
		if (ast.getType() == TokenTypes.LAMBDA) {
			visitLambda(ast);
		}
	}

	private void visitLambda(DetailAST lambda) {
		if (hasSingleParameter(lambda)) {
			boolean hasParentheses = hasToken(lambda, TokenTypes.LPAREN);
			if (this.singleArgumentParentheses && !hasParentheses) {
				log(lambda.getLineNo(), lambda.getColumnNo(), "lambda.missingParen");
			}
			else if (!this.singleArgumentParentheses && hasParentheses) {
				if (!isUsingParametersToDefineType(lambda)) {
					log(lambda.getLineNo(), lambda.getColumnNo(), "lambda.unnecessaryParen");
				}
			}
		}
		DetailAST block = lambda.getLastChild();
		int statements = countDescendantsOfType(block, TokenTypes.SEMI);
		int requireBlock = countDescendantsOfType(block, TokenTypes.LCURLY, TokenTypes.LITERAL_THROW, TokenTypes.SLIST);
		if (statements == 1 && requireBlock == 0) {
			log(block.getLineNo(), block.getColumnNo(), "lambda.unnecessaryBlock");
		}
	}

	private int countDescendantsOfType(DetailAST ast, int... types) {
		int count = 0;
		for (int type : types) {
			count += ast.getChildCount(type);
		}
		DetailAST child = ast.getFirstChild();
		while (child != null) {
			count += countDescendantsOfType(child, types);
			child = child.getNextSibling();
		}
		return count;
	}

	private boolean hasSingleParameter(DetailAST lambda) {
		DetailAST parameters = lambda.findFirstToken(TokenTypes.PARAMETERS);
		return (parameters == null) || (parameters.getChildCount(TokenTypes.PARAMETER_DEF) == 1);
	}

	private boolean isUsingParametersToDefineType(DetailAST lambda) {
		DetailAST ast = lambda.findFirstToken(TokenTypes.PARAMETERS);
		ast = (ast != null ? ast.findFirstToken(TokenTypes.PARAMETER_DEF) : null);
		ast = (ast != null ? ast.findFirstToken(TokenTypes.TYPE) : null);
		ast = (ast != null ? ast.findFirstToken(TokenTypes.IDENT) : null);
		return ast != null;
	}

	private boolean hasToken(DetailAST ast, int type) {
		return ast.findFirstToken(type) != null;
	}

	public void setSingleArgumentParentheses(boolean singleArgumentParentheses) {
		this.singleArgumentParentheses = singleArgumentParentheses;
	}

}
