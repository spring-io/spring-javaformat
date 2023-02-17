/*
 * Copyright 2017-2023 the original author or authors.
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
import com.puppycrawl.tools.checkstyle.checks.whitespace.PadOption;
import com.puppycrawl.tools.checkstyle.checks.whitespace.ParenPadCheck;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * {@link ParenPadCheck} variant that allows whitespace after {@code (} if before
 * {@code //}.
 *
 * @author Phillip Webb
 */
public class SpringParenPadCheck extends ParenPadCheck {

	private static final char OPEN_PARENTHESIS = '(';

	private static final char CLOSE_PARENTHESIS = ')';

	private PadOption option = PadOption.NOSPACE;

	@Override
	public void setOption(String optionStr) {
		this.option = PadOption.valueOf(optionStr.trim().toUpperCase(Locale.ENGLISH));
	}

	@Override
	protected void processLeft(DetailAST ast) {
		String line = getLines()[ast.getLineNo() - 1];
		int[] codePoints = line.codePoints().toArray();
		int after = ast.getColumnNo() + 1;
		if (after < codePoints.length) {
			boolean hasWhitespaceAfter = isConsideredWhitespace(codePoints, after);
			if (this.option == PadOption.NOSPACE && hasWhitespaceAfter) {
				log(ast, MSG_WS_FOLLOWED, OPEN_PARENTHESIS);
			}
			else if (this.option == PadOption.SPACE && !hasWhitespaceAfter && line.charAt(after) != CLOSE_PARENTHESIS) {
				log(ast, MSG_WS_NOT_FOLLOWED, OPEN_PARENTHESIS);
			}
		}
	}

	private boolean isConsideredWhitespace(int[] codePoints, int after) {
		if (CommonUtil.isCodePointWhitespace(codePoints, after)) {
			return !isSlashSlash(codePoints, after + 1);
		}
		return false;
	}

	private boolean isSlashSlash(int[] codePoints, int index) {
		if (index + 1 < codePoints.length) {
			char c1 = Character.toChars(codePoints[index])[0];
			char c2 = Character.toChars(codePoints[index + 1])[0];
			return c1 == '/' && c2 == '/';
		}
		return false;
	}

}
