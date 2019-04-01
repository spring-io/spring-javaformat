/*
 * Copyright 2017-2019 the original author or authors.
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

import java.io.IOException;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import io.spring.javaformat.formatter.FileEdit;
import io.spring.javaformat.formatter.FileFormatterException;

/**
 * {@link FormatterTask} to apply formatting.
 *
 * @author Phillip Webb
 */
public class FormatTask extends FormatterTask {

	/**
	 * The name of the task.
	 */
	public static final String NAME = "format";

	/**
	 * The description of the task.
	 */
	public static final String DESCRIPTION = "Apply Spring Java formatting";

	@TaskAction
	public void format() throws IOException, InterruptedException {
		try {
			formatFiles().forEach(FileEdit::save);
		}
		catch (FileFormatterException ex) {
			throw new GradleException("Unable to format file " + ex.getFile(), ex);
		}
	}

}
