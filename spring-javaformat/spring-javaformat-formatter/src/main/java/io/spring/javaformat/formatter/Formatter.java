/*
 * Copyright 2012-2015 the original author or authors.
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;

import io.spring.javaformat.formatter.eclipse.ExtendedCodeFormatter;
import io.spring.javaformat.formatter.eclipse.Preparator;
import io.spring.javaformat.formatter.preparator.Preparators;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.TextEdit;

/**
 * A {@link CodeFormatter} that applies Spring formatting conventions.
 *
 * @author Phillip Webb
 */
public class Formatter extends CodeFormatter {

	/**
	 * The components that will be formatted by default.
	 */
	private static final int DEFAULT_COMPONENTS = CodeFormatter.K_COMPILATION_UNIT
			| CodeFormatter.F_INCLUDE_COMMENTS;

	/**
	 * The default indentation level.
	 */
	private static final int DEFAULT_INDENTATION_LEVEL = 0;

	/**
	 * The default line separator.
	 */
	private static final String DEFAULT_LINE_SEPARATOR = null;

	private final Set<FormatterOption> options;

	private CodeFormatter delegate = new DelegateCodeFormatter();

	/**
	 * Create a new formatter instance.
	 */
	public Formatter() {
		this.options = Collections.emptySet();
	}

	/**
	 * Create a new formatter instance.
	 * @param options formatter options
	 */
	public Formatter(FormatterOption... options) {
		this.options = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(options)));
	}

	/**
	 * Format the given source content.
	 * @param source the source content to format
	 * @return the text edit
	 */
	public TextEdit format(String source) {
		return format(source, 0, source.length());
	}

	/**
	 * Format a specific subsection of the given source content.
	 * @param source the source content to format
	 * @param offset the offset to start formatting
	 * @param length the length to format
	 * @return the text edit
	 */
	public TextEdit format(String source, int offset, int length) {
		return format(DEFAULT_COMPONENTS, source, offset, length,
				DEFAULT_INDENTATION_LEVEL, DEFAULT_LINE_SEPARATOR);
	}

	@Override
	public TextEdit format(int kind, String source, int offset, int length,
			int indentationLevel, String lineSeparator) {
		return nlsSafe(() -> {
			return this.delegate.format(kind, source, offset, length, indentationLevel,
					lineSeparator);
		});
	}

	/**
	 * Format specific subsections of the given source content.
	 * @param source the source content to format
	 * @param regions the regions to format
	 * @return the text edit
	 */
	public TextEdit format(String source, IRegion[] regions) {
		return format(DEFAULT_COMPONENTS, source, regions, DEFAULT_INDENTATION_LEVEL,
				DEFAULT_LINE_SEPARATOR);
	}

	@Override
	public TextEdit format(int kind, String source, IRegion[] regions,
			int indentationLevel, String lineSeparator) {
		return nlsSafe(() -> this.delegate.format(kind, source, regions, indentationLevel,
				lineSeparator));
	}

	private <T> T nlsSafe(Supplier<T> formatted) {
		if (this.options.contains(FormatterOption.SHOW_NLS_WARNINGS)) {
			return formatted.get();
		}
		String nlsWarnings = System.getProperty("osgi.nls.warnings");
		try {
			System.setProperty("osgi.nls.warnings", "ignore");
			return formatted.get();
		}
		finally {
			if (nlsWarnings != null) {
				System.setProperty("osgi.nls.warnings", nlsWarnings);
			}
		}

	}

	/**
	 * Internal delegate code formatter to apply Spring {@literal formatter.prefs} and add
	 * {@link Preparator Preparators}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static class DelegateCodeFormatter extends ExtendedCodeFormatter {

		static Map<String, String> OPTIONS;

		static {
			try {
				Properties properties = new Properties();
				try (InputStream inputStream = Formatter.class
						.getResourceAsStream("formatter.prefs")) {
					properties.load(inputStream);
					OPTIONS = (Map) Collections.unmodifiableMap(properties);
				}
			}
			catch (IOException ex) {
				throw new IllegalStateException(ex);
			}
		}

		DelegateCodeFormatter() {
			super(OPTIONS);
			Preparators.forEach(this::addPreparator);
		}

	}

}
