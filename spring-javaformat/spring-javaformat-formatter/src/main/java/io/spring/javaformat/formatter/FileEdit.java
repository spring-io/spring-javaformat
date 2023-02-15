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

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.eclipse.text.edits.TextEdit;

/**
 * An {@link Edit} that can be applied to a {@link File}.
 *
 * @author Phillip Webb
 */
public class FileEdit extends Edit {

	private final File file;

	private final Charset encoding;

	FileEdit(File file, Charset encoding, String originalContent, TextEdit textEdit) {
		super(originalContent, textEdit);
		this.file = file;
		this.encoding = encoding;
	}

	public File getFile() {
		return this.file;
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

	@Override
	protected String getFormattedContent() throws Exception {
		try {
			return super.getFormattedContent();
		}
		catch (Exception ex) {
			throw FileFormatterException.wrap(this.file, ex);
		}
	}

}
