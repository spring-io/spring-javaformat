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

package io.spring.javaformat.eclipse.projectsettings;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.spring.javaformat.config.JavaFormatConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ProjectSettingsFile}.
 *
 * @author Phillip Webb
 */
public class ProjectSettingsFileTests {

	@TempDir
	public File temp;

	@Test
	void fromFileAdaptsFile() throws Exception {
		File file = new File(this.temp, "file");
		writeText(file, "test");
		ProjectSettingsFile projectSettingsFile = ProjectSettingsFile.fromFile(file);
		assertThat(projectSettingsFile.getName()).isEqualTo(file.getName());
		assertThat(projectSettingsFile.getContent(JavaFormatConfig.DEFAULT))
			.hasSameContentAs(new ByteArrayInputStream("test".getBytes()));
	}

	@Test
	void fromClasspathResourceAdaptsResource() throws Exception {
		ProjectSettingsFile projectSettingsFile = ProjectSettingsFile.fromClasspath(getClass(), "test.txt");
		assertThat(projectSettingsFile.getName()).isEqualTo("test.txt");
		assertThat(projectSettingsFile.getContent(JavaFormatConfig.DEFAULT))
			.hasSameContentAs(new ByteArrayInputStream("test".getBytes()));
	}

	private void writeText(File file, String s) throws FileNotFoundException {
		try (PrintWriter writer = new PrintWriter(file)) {
			writer.write(s);
		}
	}

}
