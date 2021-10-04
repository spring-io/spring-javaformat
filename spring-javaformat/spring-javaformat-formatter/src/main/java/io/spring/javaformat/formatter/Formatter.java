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

package io.spring.javaformat.formatter;

import java.util.Map;

import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.TextEdit;

import io.spring.javaformat.config.JavaBaseline;
import io.spring.javaformat.config.JavaFormatConfig;
import io.spring.javaformat.formatter.eclipse.EclipseCodeFormatter;
import io.spring.javaformat.formatter.jdk11.eclipse.EclipseJdk11CodeFormatter;
import io.spring.javaformat.formatter.jdk8.eclipse.EclipseJdk8CodeFormatter;

/**
 * A code formatter that applies Spring formatting conventions.
 *
 * @author Phillip Webb
 */
public class Formatter {

	/**
	 * Kind used to format a compilation unit. See Eclipse {@code CodeFormatter}
	 * constants.
	 */
	private static final int K_COMPILATION_UNIT = 0x08;

	/**
	 * Flag used to include the comments during the formatting of the code snippet. See
	 * Eclipse {@code CodeFormatter} constants.
	 */
	private static final int F_INCLUDE_COMMENTS = 0x1000;

	/**
	 * The components that will be formatted by default.
	 */
	private static final int DEFAULT_COMPONENTS = K_COMPILATION_UNIT | F_INCLUDE_COMMENTS;

	/**
	 * The default indentation level.
	 */
	private static final int DEFAULT_INDENTATION_LEVEL = 0;

	/**
	 * The default line separator.
	 */
	public static final String DEFAULT_LINE_SEPARATOR = null;

	private final EclipseCodeFormatter delegate;

	/**
	 * Create a new formatter instance.
	 */
	public Formatter() {
		this(JavaFormatConfig.DEFAULT);
	}

	/**
	 * Create a new formatter instance.
	 * @param javaFormatConfig the java format config to use
	 */
	public Formatter(JavaFormatConfig javaFormatConfig) {
		this.delegate = javaFormatConfig.getJavaBaseline() == JavaBaseline.V8
				? new EclipseJdk8CodeFormatter(javaFormatConfig) : new EclipseJdk11CodeFormatter(javaFormatConfig);
	}

	/**
	 * Format the given source content.
	 * @param source the source content to format
	 * @return the text edit
	 */
	public TextEdit format(String source) {
		return format(source, DEFAULT_LINE_SEPARATOR);
	}

	/**
	 * Format the given source content.
	 * @param source the source content to format
	 * @param lineSeparator the line separator
	 * @return the text edit
	 */
	public TextEdit format(String source, String lineSeparator) {
		return format(source, 0, source.length(), lineSeparator);
	}

	/**
	 * Format a specific subsection of the given source content.
	 * @param source the source content to format
	 * @param offset the offset to start formatting
	 * @param length the length to format
	 * @return the text edit
	 */
	public TextEdit format(String source, int offset, int length) {
		return format(source, offset, length, DEFAULT_LINE_SEPARATOR);
	}

	/**
	 * Format a specific subsection of the given source content.
	 * @param source the source content to format
	 * @param offset the offset to start formatting
	 * @param length the length to format
	 * @param lineSeparator the line separator
	 * @return the text edit
	 */
	public TextEdit format(String source, int offset, int length, String lineSeparator) {
		return format(DEFAULT_COMPONENTS, source, offset, length, DEFAULT_INDENTATION_LEVEL, lineSeparator);
	}

	public TextEdit format(int kind, String source, int offset, int length, int indentationLevel,
			String lineSeparator) {
		return this.delegate.format(kind, source, offset, length, indentationLevel, lineSeparator);
	}

	/**
	 * Format specific subsections of the given source content.
	 * @param source the source content to format
	 * @param regions the regions to format
	 * @return the text edit
	 */
	public TextEdit format(String source, IRegion[] regions) {
		return format(source, regions, DEFAULT_LINE_SEPARATOR);
	}

	/**
	 * Format specific subsections of the given source content.
	 * @param source the source content to format
	 * @param regions the regions to format
	 * @param lineSeparator the line separator
	 * @return the text edit
	 */
	public TextEdit format(String source, IRegion[] regions, String lineSeparator) {
		return format(DEFAULT_COMPONENTS, source, regions, DEFAULT_INDENTATION_LEVEL, lineSeparator);
	}

	public TextEdit format(int kind, String source, IRegion[] regions, int indentationLevel, String lineSeparator) {
		return this.delegate.format(kind, source, regions, indentationLevel, lineSeparator);
	}

	public String createIndentationString(int indentationLevel) {
		return this.delegate.createIndentationString(indentationLevel);
	}

	public void setOptions(Map<String, String> options) {
		this.delegate.setOptions(options);
	}

}
