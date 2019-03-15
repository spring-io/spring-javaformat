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

import io.spring.javaformat.gradle.testkit.GradleBuild;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CheckTask}.
 *
 * @author Phillip Webb
 */
public class CheckTaskTests {

	@Rule
	public final GradleBuild gradleBuild = new GradleBuild();

	@Test
	public void checkOk() throws IOException {
		BuildResult result = this.gradleBuild.source("src/test/resources/check-ok")
				.build("check");
		assertThat(result.task(":checkFormatMain").getOutcome())
				.isEqualTo(TaskOutcome.SUCCESS);
	}

	@Test
	public void checkBad() throws IOException {
		BuildResult result = this.gradleBuild.source("src/test/resources/check-bad")
				.buildAndFail("check");
		assertThat(result.task(":checkFormatMain").getOutcome())
				.isEqualTo(TaskOutcome.FAILED);
	}

}
