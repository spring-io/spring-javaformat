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

package io.spring.format.tools;

import java.util.regex.Pattern;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

import io.spring.javaformat.formatter.Formatter;

/**
 * . Format specified java content
 *
 * @author Howard Zuo
 */
final public class FormatContent {

	private static final Pattern TRAILING_WHITESPACE = Pattern.compile(" +$", Pattern.MULTILINE);

	private final static Formatter formatter = new Formatter();

	private FormatContent() {

	}

	public static String formatContent(String source) {
		if (source == null || "".equals(source)) {
			return "";
		}

		try {
			TextEdit textEdit = formatter.format(source);
			IDocument document = new Document(source);
			textEdit.apply(document);
			String formattedContent = document.get();
			return trimTrailingWhitespace(formattedContent);
		}
		catch (Exception e) {
			return "";
		}
	}

	private static String trimTrailingWhitespace(String content) {
		return TRAILING_WHITESPACE.matcher(content).replaceAll("");
	}

}
