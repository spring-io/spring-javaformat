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

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.checks.whitespace.NoWhitespaceBeforeCheck;

/**
 * Spring-specific customization of {@link NoWhitespaceBeforeCheck} that permits
 * whitespace before {@code ...} when it is a separator after an annotation, for
 * example {@code int @Nullable ...}.
 *
 * @author Andy Wilkinson
 */
public class SpringNoWhitespaceBeforeCheck extends NoWhitespaceBeforeCheck {

	@Override
	public void visitToken(DetailAST ast) {
		if (ast.getType() != TokenTypes.ELLIPSIS) {
			super.visitToken(ast);
		}
		else {
			visitEllipsis(ast);
		}
	}

	private void visitEllipsis(DetailAST ellipsis) {
		DetailAST previousSibling = ellipsis.getPreviousSibling();
		if (previousSibling.getType() == TokenTypes.TYPE &&
				previousSibling.getChildCount() == 2 &&
				previousSibling.getLastChild().getType() == TokenTypes.ANNOTATIONS) {
			return;
		}
		super.visitToken(ellipsis);
	}

}
