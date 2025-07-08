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

import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.spring.javaformat.gradle.testkit.GradleBuild;
import io.spring.javaformat.gradle.testkit.GradleBuildExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(GradleBuildExtension.class)
class CheckstyleTests {

	private final GradleBuild gradleBuild = new GradleBuild();

	@Test
	void configureCheckstyle() {
		BuildResult result = this.gradleBuild.source("src/test/resources/checkstyle-configure").build("checkstyleDependencies");
		assertThat(result.getOutput()).contains("io.spring.javaformat:spring-javaformat-checkstyle:");
		assertThat(result.getOutput()).contains("com.puppycrawl.tools:checkstyle:8.45.1");
	}

	@Test
	void configureCheckstyleWithCustomToolVersion() {
		BuildResult result = this.gradleBuild.source("src/test/resources/checkstyle-configure-with-custom-tool-version").build("checkstyleDependencies");
		assertThat(result.getOutput()).contains("io.spring.javaformat:spring-javaformat-checkstyle:");
		assertThat(result.getOutput()).contains("com.puppycrawl.tools:checkstyle:10.26.1");
	}

	@Test
	void doNotConfigureCheckstyle() {
		BuildResult result = this.gradleBuild.source("src/test/resources/checkstyle-do-not-configure").build("checkstyleDependencies");
		assertThat(result.getOutput()).doesNotContain("spring-javaformat-checkstyle");
	}

	@Test
	void applyDefaultConfigToCheckstyle() {
		BuildResult result = this.gradleBuild.source("src/test/resources/checkstyle-apply-default-config").build("checkstyleConfig");
		assertThat(result.getOutput()).contains("<module name=\"io.spring.javaformat.checkstyle.SpringChecks\">");
	}

}
