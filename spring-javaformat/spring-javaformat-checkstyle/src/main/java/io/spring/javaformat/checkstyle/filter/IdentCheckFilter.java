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

package io.spring.javaformat.checkstyle.filter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.checks.coding.RequireThisCheck;

/**
 * {@link CheckFilter} that can be used to skip "ident" tokens. Commonly used to filter
 * {@link RequireThisCheck} for logger references.
 *
 * @author Phillip Webb
 */
public class IdentCheckFilter extends CheckFilter {

	private Set<String> names = Collections.emptySet();

	@Override
	public void visitToken(DetailAST ast) {
		if (ast.getType() == TokenTypes.IDENT && isFiltered(ast)) {
			return;
		}
		super.visitToken(ast);
	}

	private boolean isFiltered(DetailAST ast) {
		String name = ast.getText();
		if (this.names.contains(name)) {
			return true;
		}
		return false;
	}

	public void setNames(String... names) {
		this.names = new HashSet<>(Arrays.asList(names));
	}

}
