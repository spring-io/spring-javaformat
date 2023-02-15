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

package io.spring.javaformat.formatter;

import java.util.regex.Pattern;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

/**
 * Base class for edits that can be applied to content.
 *
 * @author Phillip Webb
 */
public abstract class Edit {

	private static final Pattern TRAILING_WHITESPACE = Pattern.compile(" +$", Pattern.MULTILINE);

	private final String originalContent;

	private final TextEdit textEdit;

	protected Edit(String originalContent, TextEdit textEdit) {
		super();
		this.originalContent = originalContent;
		this.textEdit = textEdit;
	}

	public boolean hasEdits() {
		return (this.textEdit.hasChildren() || this.textEdit.getLength() > 0);
	}

	protected String getFormattedContent() throws Exception {
		IDocument document = new Document(this.originalContent);
		this.textEdit.apply(document);
		String formattedContent = document.get();
		return trimTrailingWhitespace(formattedContent);
	}

	private String trimTrailingWhitespace(String content) {
		return TRAILING_WHITESPACE.matcher(content).replaceAll("");
	}

}
