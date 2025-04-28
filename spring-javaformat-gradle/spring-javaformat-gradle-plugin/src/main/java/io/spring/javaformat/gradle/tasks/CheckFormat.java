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

package io.spring.javaformat.gradle.tasks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.VerificationException;

import io.spring.javaformat.formatter.FileEdit;

/**
 * {@link FormatterTask} to check formatting.
 *
 * @author Phillip Webb
 */
@CacheableTask
public class CheckFormat extends FormatterTask {

	/**
	 * The name of the task.
	 */
	public static final String NAME = "checkFormat";

	/**
	 * The description of the task.
	 */
	public static final String DESCRIPTION = "Run Spring Java formatting checks";

	private File reportLocation;

	@TaskAction
	public void checkFormatting() throws IOException, InterruptedException {
		List<File> problems = formatFiles().filter(FileEdit::hasEdits)
			.map(FileEdit::getFile)
			.collect(Collectors.toList());
		this.reportLocation.getParentFile().mkdirs();
		if (!problems.isEmpty()) {
			StringBuilder message = new StringBuilder("Formatting violations found in the following files:\n");
			problems.stream().forEach((f) -> message.append(" * " + getProject().relativePath(f) + "\n"));
			message.append("\nRun `format` to fix.");
			Files.write(this.reportLocation.toPath(), Collections.singletonList(message.toString()),
					StandardOpenOption.CREATE);
			throw new VerificationException(message.toString());
		}
		else {
			this.reportLocation.createNewFile();
		}
	}

	@OutputFile
	public File getReportLocation() {
		return this.reportLocation;
	}

	public void setReportLocation(File reportLocation) {
		this.reportLocation = reportLocation;
	}

}
