/*
 * Copyright 2017-2018 the original author or authors.
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

package io.spring.javaformat.gradle;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import io.spring.javaformat.formatter.FileEdit;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

/**
 * {@link FormatterTask} to check formatting.
 *
 * @author Phillip Webb
 */
public class CheckTask extends FormatterTask {

	/**
	 * The name of the task.
	 */
	public static final String NAME = "formatcheck";

	/**
	 * The description of the task.
	 */
	public static final String DESCRIPTION = "Run spring java formatting checks";

	@TaskAction
	public void checkFormatting() throws IOException, InterruptedException {
		List<File> problems = formatFiles().filter(FileEdit::hasEdits)
				.map(FileEdit::getFile).collect(Collectors.toList());
		if (!problems.isEmpty()) {
			StringBuilder message = new StringBuilder(
					"Formatting violations found in the following files:\n");
			problems.stream().forEach((f) -> message.append(" * " + f + "\n"));
			message.append("\nRun `format` to fix.");
			throw new GradleException(message.toString());
		}
	}

}
