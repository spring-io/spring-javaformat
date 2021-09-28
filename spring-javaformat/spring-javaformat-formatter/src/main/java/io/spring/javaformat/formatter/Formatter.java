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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.TextEdit;

import io.spring.javaformat.config.IndentationStyle;
import io.spring.javaformat.config.JavaFormatConfig;
import io.spring.javaformat.eclipse.jdt.core.formatter.CodeFormatter;
import io.spring.javaformat.eclipse.jdt.internal.formatter.ExtendedCodeFormatter;
import io.spring.javaformat.eclipse.jdt.internal.formatter.Preparator;
import io.spring.javaformat.formatter.preparator.Preparators;

/**
 * A {@link CodeFormatter} that applies Spring formatting conventions.
 *
 * @author Phillip Webb
 */
public class Formatter extends CodeFormatter {

	/**
	 * The components that will be formatted by default.
	 */
	private static final int DEFAULT_COMPONENTS = CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS;

	/**
	 * The default indentation level.
	 */
	private static final int DEFAULT_INDENTATION_LEVEL = 0;

	/**
	 * The default line separator.
	 */
	public static final String DEFAULT_LINE_SEPARATOR = null;

	private final CodeFormatter delegate;

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
		this.delegate = new DelegateCodeFormatter(javaFormatConfig);
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

	@Override
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

	@Override
	public TextEdit format(int kind, String source, IRegion[] regions, int indentationLevel, String lineSeparator) {
		return this.delegate.format(kind, source, regions, indentationLevel, lineSeparator);
	}

	@Override
	public String createIndentationString(int indentationLevel) {
		return this.delegate.createIndentationString(indentationLevel);
	}

	@Override
	public void setOptions(Map<String, String> options) {
		this.delegate.setOptions(options);
	}

	/**
	 * Internal delegate code formatter to apply Spring {@literal formatter.prefs} and add
	 * {@link Preparator Preparators}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static class DelegateCodeFormatter extends ExtendedCodeFormatter {

		private final Map<String, String> appliedOptions;

		DelegateCodeFormatter(JavaFormatConfig javaFormatConfig) {
			this(loadOptions(javaFormatConfig));
		}

		DelegateCodeFormatter(Map<String, String> options) {
			super(options);
			this.appliedOptions = options;
			Preparators.forEach(this::addPreparator);
		}

		@Override
		public void setOptions(Map<String, String> options) {
			super.setOptions(this.appliedOptions);
		}

		private static Map<String, String> loadOptions(JavaFormatConfig javaFormatConfig) {
			try {
				Properties properties = new Properties();
				try (InputStream inputStream = Formatter.class.getResourceAsStream("formatter.prefs")) {
					properties.load(inputStream);
				}
				applyConfig(properties, javaFormatConfig);
				return (Map) Collections.unmodifiableMap(properties);
			}
			catch (IOException ex) {
				throw new IllegalStateException(ex);
			}
		}

		private static void applyConfig(Properties properties, JavaFormatConfig javaFormatConfig) {
			if (javaFormatConfig.getIndentationStyle() == IndentationStyle.SPACES) {
				properties.put("io.spring.javaformat.eclipse.jdt.core.formatter.tabulation.char", "space");
			}
		}

	}

}
