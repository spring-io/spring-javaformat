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

package io.spring.javaformat.gradle.tasks;

import java.nio.charset.Charset;
import java.util.stream.Stream;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.SourceTask;

import io.spring.javaformat.config.IndentationStyle;
import io.spring.javaformat.config.JavaBaseline;
import io.spring.javaformat.config.JavaFormatConfig;
import io.spring.javaformat.formatter.FileEdit;
import io.spring.javaformat.formatter.FileFormatter;

/**
 * Abstract base class for formatter tasks.
 *
 * @author Phillip Webb
 */
public abstract class FormatterTask extends SourceTask {

	private String encoding;

	private final Property<JavaBaseline> javaBaseline;

	private final Property<IndentationStyle> indentationStyle;

	FormatterTask() {
		this.javaBaseline = getProject().getObjects().property(JavaBaseline.class);
		this.indentationStyle = getProject().getObjects().property(IndentationStyle.class);
	}

	/**
	 * Get the file encoding in use.
	 * @return the encoding the file encoding
	 */
	@Input
	@Optional
	public String getEncoding() {
		return this.encoding;
	}

	/**
	 * Set the file encoding in use.
	 * @param encoding the encoding to set
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * The java baseline used for the internal {@link JavaFormatConfig}.
	 * @return the java baseline property
	 * @see JavaFormatConfig#getJavaBaseline()
	 */
	@Input
	public Property<JavaBaseline> getJavaBaseline() {
		return this.javaBaseline;
	}

	/**
	 * The java indentation style for the internal {@link JavaFormatConfig}.
	 * @return the indentation style property
	 * @see JavaFormatConfig#getIndentationStyle()
	 */
	@Input
	public Property<IndentationStyle> getIndentationStyle() {
		return this.indentationStyle;
	}

	/**
	 * Format the source files and provide a {@link Stream} of {@link FileEdit} instances.
	 * @return the file edits
	 */
	protected final Stream<FileEdit> formatFiles() {
		JavaFormatConfig javaFormatConfig = JavaFormatConfig.of(this.javaBaseline.get(), this.indentationStyle.get());
		FileFormatter formatter = new FileFormatter(javaFormatConfig);
		Charset encoding = (getEncoding() != null ? Charset.forName(getEncoding()) : Charset.defaultCharset());
		return formatter.formatFiles(getSource().getFiles(), encoding);
	}

}
