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

package io.spring.javaformat.gradle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.stream.Stream;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.spring.javaformat.gradle.testkit.GradleBuild;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CheckTask}.
 *
 * @author Phillip Webb
 */
public class CheckTaskTests {

	@Rule
	public final GradleBuild gradleBuild = new GradleBuild();

	@Rule
	public final TemporaryFolder temp = new TemporaryFolder();

	@Test
	public void checkOk() throws IOException {
		BuildResult result = this.gradleBuild.source("src/test/resources/check-ok").build("check");
		assertThat(result.task(":checkFormatMain").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
	}

	@Test
	public void whenFirstInvocationSucceedsThenSecondInvocationIsUpToDate() throws IOException {
		GradleBuild gradleBuild = this.gradleBuild.source("src/test/resources/check-ok");
		BuildResult result = gradleBuild.build("check");
		assertThat(result.task(":checkFormatMain").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		result = gradleBuild.build("check");
		assertThat(result.task(":checkFormatMain").getOutcome()).isEqualTo(TaskOutcome.UP_TO_DATE);
	}

	@Test
	public void whenFirstInvocationSucceedsAndSourceIsModifiedThenSecondInvocationSucceeds() throws IOException {
		copyFolder(new File("src/test/resources/check-ok").toPath(), this.temp.getRoot().toPath());
		GradleBuild gradleBuild = this.gradleBuild.source(this.temp.getRoot());
		BuildResult result = gradleBuild.build("check");
		assertThat(result.task(":checkFormatMain").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		Files.write(new File(this.temp.getRoot(), "src/main/java/simple/Simple.java").toPath(),
				Collections.singletonList("// A change to the file"), StandardOpenOption.APPEND);
		result = gradleBuild.build("--debug", "check");
		assertThat(result.task(":checkFormatMain").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
	}

	@Test
	public void checkBad() throws IOException {
		BuildResult result = this.gradleBuild.source("src/test/resources/check-bad").buildAndFail("check");
		assertThat(result.task(":checkFormatMain").getOutcome()).isEqualTo(TaskOutcome.FAILED);
	}

	@Test
	public void whenFirstInvocationFailsThenSecondInvocationFails() throws IOException {
		GradleBuild gradleBuild = this.gradleBuild.source("src/test/resources/check-bad");
		BuildResult result = gradleBuild.buildAndFail("check");
		assertThat(result.task(":checkFormatMain").getOutcome()).isEqualTo(TaskOutcome.FAILED);
		result = gradleBuild.buildAndFail("check");
		assertThat(result.task(":checkFormatMain").getOutcome()).isEqualTo(TaskOutcome.FAILED);
	}

	private void copyFolder(Path source, Path target) throws IOException {
		try (Stream<Path> stream = Files.walk(source)) {
			stream.forEach((child) -> {
				try {
					Path relative = source.relativize(child);
					Path destination = target.resolve(relative);
					if (!destination.toFile().isDirectory()) {
						Files.copy(child, destination, StandardCopyOption.REPLACE_EXISTING);
					}
				}
				catch (Exception ex) {
					throw new IllegalStateException(ex);
				}
			});
		}
	}

}
