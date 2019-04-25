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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Checks that certain fields are never referenced using {@code 'this.'}.
 *
 * @author Phillip Webb
 */
public class SpringNoThisCheck extends AbstractSpringCheck {

	private Set<String> names = Collections.emptySet();

	private boolean allowAssignement = true;

	@Override
	public int[] getAcceptableTokens() {
		return new int[] { TokenTypes.IDENT };
	}

	@Override
	public void visitToken(DetailAST ast) {
		if (ast.getType() == TokenTypes.IDENT) {
			visitIdent(ast);
		}
	}

	private void visitIdent(DetailAST ast) {
		String name = ast.getText();
		if (this.names.contains(name)) {
			DetailAST sibling = ast.getPreviousSibling();
			if (sibling != null && sibling.getType() == TokenTypes.LITERAL_THIS) {
				DetailAST parent = getFirstNonDotParent(ast);
				if (!(this.allowAssignement && parent != null && parent.getType() == TokenTypes.ASSIGN)) {
					log(ast.getLineNo(), ast.getColumnNo(), "nothis.unexpected", name);
				}
			}
		}
	}

	private DetailAST getFirstNonDotParent(DetailAST ast) {
		DetailAST result = (ast != null ? ast.getParent() : null);
		while (result != null && result.getType() == TokenTypes.DOT) {
			result = result.getParent();
		}
		return result;
	}

	public void setNames(String... names) {
		this.names = new HashSet<>(Arrays.asList(names));
	}

	public void setAllowAssignement(boolean allowAssignement) {
		this.allowAssignement = allowAssignement;
	}

}
