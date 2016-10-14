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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.gradle.tooling.BuildException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@literal gradle springFormatCheck}.
 *
 * @author Phillip Webb
 */
public class ValidateIT {

	@Test
	public void checkValid() throws Exception {
		new ProjectCreator().run("validate-ok", "springFormatValidate");
	}

	@Test
	public void checkInvalid() throws Exception {
		try {
			new ProjectCreator().run("validate-bad", "springFormatValidate");
		}
		catch (BuildException ex) {
			// Expected
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ex.printStackTrace(new PrintStream(out));
			assertThat(out.toString()).contains("Formatting violations found")
					.contains("Simple.java");
		}
	}

	@Test
	public void checkInvalidBuild() throws Exception {
		try {
			new ProjectCreator().run("validate-bad", "build");
		}
		catch (BuildException ex) {
			// Expected
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ex.printStackTrace(new PrintStream(out));
			assertThat(out.toString()).contains("Formatting violations found")
					.contains("Simple.java");
		}
	}

}
