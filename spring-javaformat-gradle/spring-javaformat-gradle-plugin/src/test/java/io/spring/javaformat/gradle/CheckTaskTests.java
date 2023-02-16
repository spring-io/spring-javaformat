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

package io.spring.javaformat.gradle;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.stream.Stream;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import io.spring.javaformat.gradle.tasks.CheckFormat;
import io.spring.javaformat.gradle.testkit.GradleBuild;
import io.spring.javaformat.gradle.testkit.GradleBuildExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CheckFormat}.
 *
 * @author Phillip Webb
 */
@ExtendWith(GradleBuildExtension.class)
public class CheckTaskTests {

	private final GradleBuild gradleBuild = new GradleBuild();

	@TempDir
	public File temp;

	@Test
	void checkOk() throws IOException {
		BuildResult result = this.gradleBuild.source("src/test/resources/check-ok").build("check");
		assertThat(result.task(":checkFormatMain").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
	}

	@Test
	void whenFirstInvocationSucceedsThenSecondInvocationIsUpToDate() throws IOException {
		GradleBuild gradleBuild = this.gradleBuild.source("src/test/resources/check-ok");
		BuildResult result = gradleBuild.build("check");
		assertThat(result.task(":checkFormatMain").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		result = gradleBuild.build("check");
		assertThat(result.task(":checkFormatMain").getOutcome()).isEqualTo(TaskOutcome.UP_TO_DATE);
	}

	@Test
	void whenFirstInvocationSucceedsAndSourceIsModifiedThenSecondInvocationSucceeds() throws IOException {
		copyNormalizedFolder(new File("src/test/resources/check-ok").toPath(), this.temp.toPath());
		GradleBuild gradleBuild = this.gradleBuild.source(this.temp);
		BuildResult result = gradleBuild.build("check");
		assertThat(result.task(":checkFormatMain").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		Files.write(new File(this.temp, "src/main/java/simple/Simple.java").toPath(),
				"// A change to the file\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
		result = gradleBuild.build("--debug", "check");
		assertThat(result.task(":checkFormatMain").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
	}

	@Test
	void whenFirstInvocationSucceedsAndIndentationStyleIsChangedThenSecondInvocationFails() throws IOException {
		GradleBuild gradleBuild = this.gradleBuild.source("src/test/resources/check-ok");
		BuildResult result = gradleBuild.build("check");
		assertThat(result.task(":checkFormatMain").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		Files.write(new File(this.gradleBuild.getProjectDir(), ".springjavaformatconfig").toPath(),
				Arrays.asList("indentation-style=spaces"));
		result = gradleBuild.buildAndFail("check");
		assertThat(result.task(":checkFormatMain").getOutcome()).isEqualTo(TaskOutcome.FAILED);
	}

	@Test
	void whenFirstInvocationFailsAndIndentationStyleIsChangedThenSecondInvocationSucceeds() throws IOException {
		GradleBuild gradleBuild = this.gradleBuild.source("src/test/resources/check-spaces");
		BuildResult result = gradleBuild.buildAndFail("check");
		assertThat(result.task(":checkFormatMain").getOutcome()).isEqualTo(TaskOutcome.FAILED);
		Files.write(new File(this.gradleBuild.getProjectDir(), ".springjavaformatconfig").toPath(),
				Arrays.asList("indentation-style=spaces"));
		result = gradleBuild.build("check");
		assertThat(result.task(":checkFormatMain").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
	}

	@Test
	void whenFirstInvocationSucceedsAndJavaBaselineIsChangedThenSecondInvocationSucceedsAndThirdIsUpToDate()
			throws IOException {
		GradleBuild gradleBuild = this.gradleBuild.source("src/test/resources/check-ok");
		BuildResult result = gradleBuild.build("check");
		assertThat(result.task(":checkFormatMain").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		Files.write(new File(this.gradleBuild.getProjectDir(), ".springjavaformatconfig").toPath(),
				Arrays.asList("java-baseline=8"));
		result = gradleBuild.build("check");
		assertThat(result.task(":checkFormatMain").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		result = gradleBuild.build("check");
		assertThat(result.task(":checkFormatMain").getOutcome()).isEqualTo(TaskOutcome.UP_TO_DATE);
	}

	@Test
	void checkBad() throws IOException {
		BuildResult result = this.gradleBuild.source("src/test/resources/check-bad").buildAndFail("check");
		assertThat(result.task(":checkFormatMain").getOutcome()).isEqualTo(TaskOutcome.FAILED);
	}

	@Test
	void whenFirstInvocationFailsThenSecondInvocationFails() throws IOException {
		GradleBuild gradleBuild = this.gradleBuild.source("src/test/resources/check-bad");
		BuildResult result = gradleBuild.buildAndFail("check");
		assertThat(result.task(":checkFormatMain").getOutcome()).isEqualTo(TaskOutcome.FAILED);
		result = gradleBuild.buildAndFail("check");
		assertThat(result.task(":checkFormatMain").getOutcome()).isEqualTo(TaskOutcome.FAILED);
	}

	private void copyNormalizedFolder(Path source, Path target) throws IOException {
		try (Stream<Path> stream = Files.walk(source)) {
			stream.forEach((child) -> {
				try {
					Path relative = source.relativize(child);
					Path destination = target.resolve(relative);
					if (!Files.isDirectory(child)) {
						String content = new String(Files.readAllBytes(child), StandardCharsets.UTF_8);
						String normalized = content.replace("\n\r", "\n").replace('\r', '\n');
						Files.createDirectories(destination.getParent());
						Files.write(destination, normalized.getBytes(StandardCharsets.UTF_8));
					}
				}
				catch (Exception ex) {
					throw new IllegalStateException(ex);
				}
			});
		}
	}

}
