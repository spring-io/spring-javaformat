/*
 * Copyright 2017-2020 the original author or authors.
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

package io.spring.format.maven;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import io.spring.javaformat.formatter.FileEdit;
import io.spring.javaformat.formatter.FileFormatter;

/**
 * Validates that source formatting matches the required style.
 *
 * @author Phillip Webb
 */
@Mojo(name = "validate", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public class ValidateMojo extends FormatMojo {

	/**
	 * Skip the execution.
	 */
	@Parameter(property = "spring-javaformat.skip", defaultValue = "false")
	private boolean skip;

	@Override
	protected void execute(List<File> files, Charset encoding, String lineSeparator)
			throws MojoExecutionException, MojoFailureException {
		if (this.skip) {
			getLog().debug("skipping validation as per configuration.");
			return;
		}
		FileFormatter formatter = new FileFormatter();
		List<File> problems = formatter.formatFiles(files, encoding, lineSeparator).filter(FileEdit::hasEdits)
				.map(FileEdit::getFile).collect(Collectors.toList());
		if (!problems.isEmpty()) {
			StringBuilder message = new StringBuilder("Formatting violations found in the following files:\n");
			problems.stream().forEach((f) -> message.append(" * " + f + "\n"));
			message.append("\nRun `spring-javaformat:apply` to fix.");
			throw new MojoFailureException(message.toString());
		}
	}

}
