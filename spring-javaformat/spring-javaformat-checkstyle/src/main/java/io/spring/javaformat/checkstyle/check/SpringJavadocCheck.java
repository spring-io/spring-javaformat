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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TextBlock;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Checks that the javadoc comments follow Spring conventions.
 *
 * @author Phillip Webb
 */
public class SpringJavadocCheck extends AbstractSpringCheck {

	private static final Pattern[] PATTERNS = { Pattern.compile("@param\\s+\\S+\\s+(.*)"),
			Pattern.compile("@throws\\s+\\S+\\s+(.*)"), Pattern.compile("@return\\s+(.*)") };

	@Override
	public int[] getDefaultTokens() {
		return new int[] { TokenTypes.INTERFACE_DEF, TokenTypes.CLASS_DEF, TokenTypes.ENUM_DEF,
				TokenTypes.ANNOTATION_DEF, TokenTypes.METHOD_DEF, TokenTypes.CTOR_DEF };
	}

	@Override
	public int[] getAcceptableTokens() {
		return new int[] { TokenTypes.INTERFACE_DEF, TokenTypes.CLASS_DEF, TokenTypes.ENUM_DEF,
				TokenTypes.ANNOTATION_DEF, TokenTypes.METHOD_DEF, TokenTypes.CTOR_DEF, TokenTypes.ENUM_CONSTANT_DEF,
				TokenTypes.ANNOTATION_FIELD_DEF };
	}

	@Override
	public void visitToken(DetailAST ast) {
		int lineNumber = ast.getLineNo();
		TextBlock javadoc = getFileContents().getJavadocBefore(lineNumber);
		if (javadoc != null) {
			checkParamTags(javadoc);
		}
	}

	private void checkParamTags(TextBlock javadoc) {
		String[] text = javadoc.getText();
		for (int i = 0; i < text.length; i++) {
			for (Pattern pattern : PATTERNS) {
				Matcher matcher = pattern.matcher(text[i]);
				if (matcher.find()) {
					String description = matcher.group(1).trim();
					if (startsWithUppercase(description)) {
						log(javadoc.getStartLineNo() + i, text[i].length() - description.length(), "javadoc.badCase");
					}
				}
			}
		}
	}

	private boolean startsWithUppercase(String description) {
		return description.length() > 0 && Character.isUpperCase(description.charAt(0));
	}

}
