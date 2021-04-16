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

package io.spring.javaformat.gradle;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.SourceTask;

import io.spring.javaformat.formatter.EditorConfigManager;
import io.spring.javaformat.formatter.FileEdit;
import io.spring.javaformat.formatter.FileFormatter;

/**
 * Abstract base class for formatter tasks.
 *
 * @author Phillip Webb
 * @author Tadaya Tsuyukubo
 */
abstract class FormatterTask extends SourceTask {

	private String encoding;

	private EditorConfigManager editorConfigManager = new EditorConfigManager();

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
	 * Format the source files and provide a {@link Stream} of {@link FileEdit} instances.
	 * @return the file edits
	 */
	@SuppressWarnings("unchecked")
	protected final Stream<FileEdit> formatFiles() {
		Set<File> files = getSource().getFiles();

		// group by suffix
		Map<String, List<File>> filesByType = files.stream().collect(
				Collectors.groupingBy(file -> {
					String filename = file.getName();
					int lastIndex = filename.lastIndexOf(".");
					if (lastIndex < 0) {
						return "";
					}
					return filename.substring(lastIndex + 1);
				}));

		String projectDir = getPath();
		Charset encoding = (getEncoding() != null ? Charset.forName(getEncoding()) : Charset.defaultCharset());

		List<Stream<FileEdit>> list = new ArrayList<>();
		for (List<File> filesToFormat : filesByType.values()) {
			File firstFile = filesToFormat.get(0);
			Map<String, String> options = this.editorConfigManager.getProperties(firstFile, projectDir);

			FileFormatter formatter = new FileFormatter();
			for (Entry<String, String> entry : options.entrySet()) {
				formatter.addOrReplaceOption(entry.getKey(), entry.getValue());
			}
			Stream<FileEdit> stream = formatter.formatFiles(filesToFormat, encoding);
			list.add(stream);
		}

		Stream<FileEdit>[] array = list.toArray(new Stream[0]);
		return Stream.of(array).flatMap(Function.identity());
	}

}
