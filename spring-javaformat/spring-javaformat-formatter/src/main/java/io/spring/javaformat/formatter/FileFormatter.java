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

package io.spring.javaformat.formatter;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.text.edits.TextEdit;

import io.spring.javaformat.config.JavaFormatConfig;

/**
 * A code formatter designed to work with {@link File Files}.
 *
 * @author Phillip Webb
 * @see Formatter
 */
public class FileFormatter {

	private final Formatter formatter;

	public FileFormatter() {
		this(new Formatter());
	}

	public FileFormatter(JavaFormatConfig javaFormatConfig) {
		this(new Formatter(javaFormatConfig));
	}

	public FileFormatter(Formatter formatter) {
		Optional.ofNullable(formatter).orElseThrow(() -> new IllegalArgumentException("Formatter must not be null"));
		this.formatter = formatter;
	}

	/**
	 * Format the given source files and provide a {@link Stream} of {@link FileEdit}
	 * instances.
	 * @param files the files to format
	 * @param encoding the source encoding
	 * @return a stream of file edits
	 */
	public Stream<FileEdit> formatFiles(Iterable<File> files, Charset encoding) {
		return formatFiles(files, encoding, Formatter.DEFAULT_LINE_SEPARATOR);
	}

	/**
	 * Format the given source files and provide a {@link Stream} of {@link FileEdit}
	 * instances.
	 * @param files the files to format
	 * @param encoding the source encoding
	 * @param lineSeparator the line separator
	 * @return a stream of file edits
	 */
	public Stream<FileEdit> formatFiles(Iterable<File> files, Charset encoding, String lineSeparator) {
		return formatFiles(StreamSupport.stream(files.spliterator(), false), encoding, lineSeparator);
	}

	/**
	 * Format the given source files and provide a {@link Stream} of {@link FileEdit}
	 * instances.
	 * @param files the files to format
	 * @param encoding the source encoding
	 * @return a stream of file edits
	 */
	public Stream<FileEdit> formatFiles(Stream<File> files, Charset encoding) {
		return formatFiles(files, encoding, Formatter.DEFAULT_LINE_SEPARATOR);
	}

	/**
	 * Format the given source files and provide a {@link Stream} of {@link FileEdit}
	 * instances.
	 * @param files the files to format
	 * @param encoding the source encoding
	 * @param lineSeparator the line separator
	 * @return a stream of file edits
	 */
	public Stream<FileEdit> formatFiles(Stream<File> files, Charset encoding, String lineSeparator) {
		return files.map((file) -> formatFile(file, encoding, lineSeparator));
	}

	/**
	 * Format the given source file and return a {@link FileEdit} instance.
	 * @param file the file to format
	 * @param encoding the source encoding
	 * @return a file edit
	 */
	public FileEdit formatFile(File file, Charset encoding) {
		return formatFile(file, encoding, Formatter.DEFAULT_LINE_SEPARATOR);
	}

	/**
	 * Format the given source file and return a {@link FileEdit} instance.
	 * @param file the file to format
	 * @param encoding the source encoding
	 * @param lineSeparator the line separator
	 * @return a file edit
	 */
	public FileEdit formatFile(File file, Charset encoding, String lineSeparator) {
		try {
			String content = new String(Files.readAllBytes(file.toPath()), encoding);
			TextEdit edit = this.formatter.format(content, lineSeparator);
			return new FileEdit(file, encoding, content, edit);
		}
		catch (Exception ex) {
			throw FileFormatterException.wrap(file, ex);
		}
	}

}
