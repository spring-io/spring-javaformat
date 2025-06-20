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

package io.spring.javaformat.gradle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.spring.javaformat.gradle.tasks.Format;
import io.spring.javaformat.gradle.testkit.GradleBuild;
import io.spring.javaformat.gradle.testkit.GradleBuildExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Format}.
 *
 * @author Phillip Webb
 */
@ExtendWith(GradleBuildExtension.class)
public class FormatTaskTests {

	private final GradleBuild gradleBuild = new GradleBuild();

	@Test
	void checkOk() throws IOException {
		BuildResult result = this.gradleBuild.source("src/test/resources/format").build("format");
		assertThat(result.task(":formatMain").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		File formattedFile = new File(this.gradleBuild.getProjectDir(), "src/main/java/simple/Simple.java");
		String formattedContent = new String(Files.readAllBytes(formattedFile.toPath()));
		assertThat(formattedContent).contains("class Simple {").contains("	public static void main");
	}

	@Test
	void checkUpToDate() throws IOException {
		GradleRunner runner = this.gradleBuild.source("src/test/resources/format").prepareRunner("format");
		// Format that changes files
		assertThat(runner.build().task(":formatMain").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		// Format of already formatted files
		assertThat(runner.build().task(":formatMain").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		// Up-to-date
		assertThat(runner.build().task(":formatMain").getOutcome()).isEqualTo(TaskOutcome.UP_TO_DATE);
	}

	@Test
	void notUpToDateWhenJavaBaselineChanges() throws IOException {
		GradleRunner runner = this.gradleBuild.source("src/test/resources/format").prepareRunner("format");
		// Format that changes files
		assertThat(runner.build().task(":formatMain").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		// Format of already formatted files
		assertThat(runner.build().task(":formatMain").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		// Up-to-date
		assertThat(runner.build().task(":formatMain").getOutcome()).isEqualTo(TaskOutcome.UP_TO_DATE);
		Files.write(new File(this.gradleBuild.getProjectDir(), ".springjavaformatconfig").toPath(),
				Arrays.asList("java-baseline=8"));
		assertThat(runner.build().task(":formatMain").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
	}

	@Test
	void notUpToDateWhenIndentationStyleChanges() throws IOException {
		GradleRunner runner = this.gradleBuild.source("src/test/resources/format").prepareRunner("format");
		// Format that changes files
		assertThat(runner.build().task(":formatMain").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		// Format of already formatted files
		assertThat(runner.build().task(":formatMain").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		// Up-to-date
		assertThat(runner.build().task(":formatMain").getOutcome()).isEqualTo(TaskOutcome.UP_TO_DATE);
		Files.write(new File(this.gradleBuild.getProjectDir(), ".springjavaformatconfig").toPath(),
				Arrays.asList("indentation-style=spaces"));
		assertThat(runner.build().task(":formatMain").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
	}

	@Test
	void checkSpacesOk() throws IOException {
		BuildResult result = this.gradleBuild.source("src/test/resources/format-spaces").build("format");
		assertThat(result.task(":formatMain").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		File formattedFile = new File(this.gradleBuild.getProjectDir(), "src/main/java/simple/Simple.java");
		String formattedContent = new String(Files.readAllBytes(formattedFile.toPath()));
		assertThat(formattedContent).contains("class Simple {").contains("    public static void main");
	}

}
