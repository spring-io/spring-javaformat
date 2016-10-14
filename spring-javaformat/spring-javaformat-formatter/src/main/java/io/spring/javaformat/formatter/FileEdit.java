/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.javaformat.formatter;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

/**
 * An Edit that can be applied to a File.
 *
 * @author Phillip Webb
 */
public class FileEdit {

	private final File file;

	private final Charset encoding;

	private final String originalContent;

	private final TextEdit edit;

	FileEdit(File file, Charset encoding, String originalContent, TextEdit edit) {
		this.file = file;
		this.encoding = encoding;
		this.originalContent = originalContent;
		this.edit = edit;
	}

	public File getFile() {
		return this.file;
	}

	public boolean hasEdits() {
		return (this.edit.hasChildren() || this.edit.getLength() > 0);
	}

	public void save() {
		try {
			String formattedContent = getFormattedContent();
			byte[] bytes = formattedContent.getBytes(this.encoding);
			Files.write(this.file.toPath(), bytes, StandardOpenOption.TRUNCATE_EXISTING);
		}
		catch (Exception ex) {
			throw FileFormatterException.wrap(this.file, ex);
		}
	}

	public String getFormattedContent() throws Exception {
		try {
			IDocument document = new Document(this.originalContent);
			this.edit.apply(document);
			String formattedContent = document.get();
			return formattedContent;
		}
		catch (Exception ex) {
			throw FileFormatterException.wrap(this.file, ex);
		}
	}

}
