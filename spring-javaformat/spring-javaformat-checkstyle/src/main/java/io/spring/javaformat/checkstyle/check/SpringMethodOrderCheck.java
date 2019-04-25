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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Checks that methods are defined in the correct order.
 *
 * @author Phillip Webb
 */
public class SpringMethodOrderCheck extends AbstractSpringCheck {

	private static final List<String> EXPECTED_ORDER = Collections
			.unmodifiableList(Arrays.asList("equals", "hashCode", "toString"));

	@Override
	public int[] getAcceptableTokens() {
		return new int[] { TokenTypes.CLASS_DEF, TokenTypes.INTERFACE_DEF };
	}

	@Override
	public void visitToken(DetailAST ast) {
		DetailAST block = ast.findFirstToken(TokenTypes.OBJBLOCK);
		DetailAST candidate = block.getFirstChild();
		List<DetailAST> methods = new ArrayList<>();
		while (candidate != null) {
			candidate = candidate.getNextSibling();
			if (candidate != null && candidate.getType() == TokenTypes.METHOD_DEF) {
				DetailAST ident = candidate.findFirstToken(TokenTypes.IDENT);
				DetailAST parameters = candidate.findFirstToken(TokenTypes.PARAMETERS);
				if (isOrderedMethod(ident, parameters)) {
					methods.add(ident);
				}
			}
		}
		checkOrder(methods);
	}

	private void checkOrder(List<DetailAST> methods) {
		List<String> methodsNames = methods.stream().map(DetailAST::getText).collect(Collectors.toList());
		List<String> expected = new ArrayList<>(EXPECTED_ORDER);
		expected.retainAll(methodsNames);
		for (int i = 0; i < methods.size(); i++) {
			DetailAST method = methods.get(i);
			if (!method.getText().equals(expected.get(i))) {
				log(method.getLineNo(), method.getColumnNo(), "methodorder.outOfOrder", method.getText(), expected);
			}
		}
	}

	private boolean isOrderedMethod(DetailAST ident, DetailAST parameters) {
		if ("equals".equals(ident.getText()) && parameters.getChildCount() == 1) {
			return true;
		}
		if ("hashCode".equals(ident.getText()) && parameters.getChildCount() == 0) {
			return true;
		}
		if ("toString".equals(ident.getText()) && parameters.getChildCount() == 0) {
			return true;
		}
		return false;
	}

}
