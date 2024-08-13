/*
 * Copyright 2017-2021 the original author or authors.
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

import java.io.File;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import io.spring.javaformat.config.IndentationStyle;
import io.spring.javaformat.config.JavaFormatConfig;

/**
 * Checks that leading whitespace matches the expected indentation style.
 *
 * @author Phillip Webb
 */
public class SpringLeadingWhitespaceCheck extends AbstractSpringCheck {

	private static final Pattern PATTERN = Pattern.compile("^([\\ \\t]+)\\S");

	private static final Map<IndentationStyle, Pattern> INDENTATION_STYLE_PATTERN;
	static {
		Map<IndentationStyle, Pattern> indentationStylePatterns = new HashMap<>();
		indentationStylePatterns.put(IndentationStyle.TABS, Pattern.compile("\\t*"));
		indentationStylePatterns.put(IndentationStyle.SPACES, Pattern.compile("\\ *"));
		INDENTATION_STYLE_PATTERN = Collections.unmodifiableMap(indentationStylePatterns);
	}

	private IndentationStyle indentationStyle;

	private final Deque<TextBlockPair> textBlockPairs = new ArrayDeque<>();

	@Override
	public int[] getAcceptableTokens() {
		return new int[] { TokenTypes.TEXT_BLOCK_LITERAL_BEGIN, TokenTypes.TEXT_BLOCK_LITERAL_END };
	}

	@Override
	public void visitToken(DetailAST ast) {
		super.visitToken(ast);
		if (ast.getType() == TokenTypes.TEXT_BLOCK_LITERAL_BEGIN) {
			this.textBlockPairs.add(new TextBlockPair(ast));
		}
		else if (ast.getType() == TokenTypes.TEXT_BLOCK_LITERAL_END) {
			this.textBlockPairs.getLast().end(ast);
		}
	}

	@Override
	public void beginTree(DetailAST rootAST) {
		super.beginTree(rootAST);
		this.textBlockPairs.clear();
	}

	@Override
	public void finishTree(DetailAST rootAST) {
		FileContents fileContents = getFileContents();
		FileText fileText = fileContents.getText();
		File file = fileText.getFile();
		if (file == null) {
			return;
		}
		IndentationStyle indentationStyle = (this.indentationStyle != null) ? this.indentationStyle
				: JavaFormatConfig.findFrom(file.getParentFile()).getIndentationStyle();
		for (int i = 0; i < fileText.size(); i++) {
			int lineNo = i + 1;
			if (isInTextBlock(lineNo)) {
				continue;
			}
			String line = fileText.get(i);
			Matcher matcher = PATTERN.matcher(line);
			boolean found = matcher.find(0);
			while (found
					&& fileContents.hasIntersectionWithComment(lineNo, matcher.start(0), lineNo, matcher.end(0) - 1)) {
				found = matcher.find(matcher.end(0));
			}
			if (found && !INDENTATION_STYLE_PATTERN.get(indentationStyle).matcher(matcher.group(1)).matches()) {
				log(lineNo, "leadingwhitespace.incorrect", indentationStyle.toString().toLowerCase());
			}
		}
		super.finishTree(rootAST);
	}

	private boolean isInTextBlock(int lineNo) {
		return this.textBlockPairs.stream().anyMatch((textBlockPair) -> textBlockPair.contains(lineNo));
	}

	public void setIndentationStyle(String indentationStyle) {
		this.indentationStyle = (indentationStyle != null && !"".equals(indentationStyle))
				? IndentationStyle.valueOf(indentationStyle.toUpperCase()) : null;
	}

	private static class TextBlockPair {

		private final DetailAST begin;

		private DetailAST end;

		TextBlockPair(DetailAST begin) {
			this.begin = begin;
		}

		public boolean contains(int lineNo) {
			return (lineNo > this.begin.getLineNo()) && (lineNo <= this.end.getLineNo());
		}

		void end(DetailAST end) {
			this.end = end;
		}

	}

}
