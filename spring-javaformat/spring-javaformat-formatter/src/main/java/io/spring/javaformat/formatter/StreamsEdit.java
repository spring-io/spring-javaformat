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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.eclipse.text.edits.TextEdit;

/**
 * An {@link Edit} that can be applied to IO Streams.
 *
 * @author Phillip Webb
 */
public class StreamsEdit extends Edit {

	StreamsEdit(String originalContent, TextEdit textEdit) {
		super(originalContent, textEdit);
	}

	/**
	 * Write the edited content to the given {@link OutputStream}.
	 * @param outputStream the output stream where formatted content should be written
	 */
	public void writeTo(OutputStream outputStream) {
		writeTo(outputStream, StandardCharsets.UTF_8);
	}

	/**
	 * Write the edited content to the given {@link OutputStream}.
	 * @param outputStream the output stream where formatted content should be written
	 * @param encoding the source encoding
	 */
	public void writeTo(OutputStream outputStream, Charset encoding) {
		try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, encoding)) {
			writeTo(writer);
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Write the edited content to the given {@link Appendable}.
	 * @param appendable the appendable where formatted content should be written
	 */
	public void writeTo(Appendable appendable) {
		try {
			appendable.append(getFormattedContent());
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
