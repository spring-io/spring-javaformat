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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.eclipse.text.edits.TextEdit;

import io.spring.javaformat.config.JavaFormatConfig;

/**
 * A code formatter designed to work with IO streams.
 *
 * @author Phillip Webb
 * @see Formatter
 */
public class StreamsFormatter {

	private final Formatter formatter;

	public StreamsFormatter() {
		this(new Formatter());
	}

	public StreamsFormatter(JavaFormatConfig javaFormatConfig) {
		this(new Formatter(javaFormatConfig));
	}

	public StreamsFormatter(Formatter formatter) {
		Optional.ofNullable(formatter).orElseThrow(() -> new IllegalArgumentException("Formatter must not be null"));
		this.formatter = formatter;
	}

	/**
	 * Format content from the given source {@link InputStream} and return a
	 * {@link StreamsEdit} instance.
	 * @param inputStream the source input stream
	 * @return a streams edit
	 */
	public StreamsEdit format(InputStream inputStream) {
		return format(inputStream, StandardCharsets.UTF_8);
	}

	/**
	 * Format content from the given source {@link InputStream} and return a
	 * {@link StreamsEdit} instance.
	 * @param inputStream the source input stream
	 * @param encoding the source encoding
	 * @return a streams edit
	 */
	public StreamsEdit format(InputStream inputStream, Charset encoding) {
		return format(inputStream, encoding, Formatter.DEFAULT_LINE_SEPARATOR);
	}

	/**
	 * Format content from the given source {@link InputStream} and return a
	 * {@link StreamsEdit} instance.
	 * @param inputStream the source input stream
	 * @param encoding the source encoding
	 * @param lineSeparator the line separator
	 * @return a streams edit
	 */
	public StreamsEdit format(InputStream inputStream, Charset encoding, String lineSeparator) {
		return format(new InputStreamReader(inputStream, encoding), lineSeparator);
	}

	/**
	 * Format content from the given source {@link Reader} and return a
	 * {@link StreamsEdit} instance.
	 * @param reader the source reader
	 * @return a streams edit
	 */
	public StreamsEdit format(Reader reader) {
		return format(reader, Formatter.DEFAULT_LINE_SEPARATOR);
	}

	/**
	 * Format content from the given source {@link Reader} and return a
	 * {@link StreamsEdit} instance.
	 * @param reader the source reader
	 * @param lineSeparator the line separator
	 * @return a streams edit
	 */
	public StreamsEdit format(Reader reader, String lineSeparator) {
		try {
			String content = readContent(reader);
			TextEdit edit = this.formatter.format(content, lineSeparator);
			return new StreamsEdit(content, edit);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private String readContent(Reader reader) throws IOException {
		StringBuilder result = new StringBuilder();
		char[] buffer = new char[2048];
		int numChars;
		while ((numChars = reader.read(buffer)) >= 0) {
			result.append(buffer, 0, numChars);
		}
		return result.toString();
	}
}
