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

package io.spring.javaformat.eclipse.formatter;

import java.util.Map;

import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.TextEdit;

import io.spring.javaformat.config.JavaFormatConfig;
import io.spring.javaformat.formatter.Formatter;

/**
 * Eclipse {@link CodeFormatter} base class for Spring formatting.
 *
 * @author Phillip Webb
 */
public abstract class SpringCodeFormatter extends CodeFormatter {

	private final Formatter delegate;

	public SpringCodeFormatter(JavaFormatConfig javaFormatConfig) {
		this.delegate = new Formatter(javaFormatConfig);
	}

	@Override
	public TextEdit format(int kind, String source, int offset, int length, int indentationLevel,
			String lineSeparator) {
		return this.delegate.format(kind, source, offset, length, indentationLevel, lineSeparator);
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

}
