/*
 * Copyright 2012-2016 the original author or authors.
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

package io.spring.format.gradle;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import io.spring.javaformat.formatter.FileEdit;
import io.spring.javaformat.formatter.FileFormatter;
import org.gradle.api.GradleException;

/**
 * {@link FormatterTask} to validate formatting.
 *
 * @author Phillip Webb
 */
public class ValidateTask extends FormatterTask {

	static final String NAME = "springFormatValidate";

	static final String DESCRIPTION = "Validates Java source code formatting against Spring conventions";

	@Override
	public void run() throws Exception {
		FileFormatter formatter = new FileFormatter();
		List<File> problems = formatter.formatFiles(this.files, this.encoding)
				.filter(FileEdit::hasEdits).map(FileEdit::getFile)
				.collect(Collectors.toList());
		if (!problems.isEmpty()) {
			StringBuilder message = new StringBuilder(
					"Formatting violations found in the following files:\n");
			problems.stream().forEach((f) -> message.append(" * " + f + "\n"));
			message.append("\nRun `springFormatApply` to fix.");
			throw new GradleException(message.toString());
		}
	}

}
