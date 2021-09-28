/*
 * Copyright 2012-2021 the original author or authors.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.Properties;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.spring.javaformat.config.JavaFormatConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ProjectProperties}.
 *
 * @author Phillip Webb
 */
public class ProjectPropertiesTests {

	@TempDir
	public File temp;

	@Test
	public void addFromFolderAddsEclipseProperties() throws IOException {
		File file = new File(this.temp, "eclipse.properties");
		writeProperties(file, "2018");
		ProjectProperties properties = new ProjectProperties();
		properties.addFromFolder(this.temp);
		assertThat(properties.get("copyright-year")).isEqualTo("2018");
	}

	@Test
	public void addFromFolderWhenAlreadySetDoesNotOverwrite() throws IOException {
		ProjectProperties properties = new ProjectProperties();
		File folder1 = new File(this.temp, "1");
		folder1.mkdirs();
		writeProperties(new File(folder1, "eclipse.properties"), "2018");
		properties.addFromFolder(folder1);
		File folder2 = new File(this.temp, "2");
		folder2.mkdirs();
		writeProperties(new File(folder2, "eclipse.properties"), "2017");
		properties.addFromFolder(folder2);
		assertThat(properties.get("copyright-year")).isEqualTo("2018");
	}

	@Test
	public void addFromEmptyFolderUsesDefaults() throws IOException {
		ProjectProperties properties = new ProjectProperties();
		properties.addFromFolder(this.temp);
		String currentYear = String.valueOf(LocalDate.now().getYear());
		assertThat(properties.get("copyright-year")).isEqualTo(currentYear);
	}

	@Test
	public void getModifiedContentReplacesCopyrightYear() throws IOException {
		String year = "2016-2020";
		File file = new File(this.temp, "eclipse.properties");
		writeProperties(file, year);
		ProjectProperties properties = new ProjectProperties();
		properties.addFromFolder(this.temp);
		ProjectSettingsFiles files = new ProjectSettingsFilesLocator().locateSettingsFiles();
		ProjectSettingsFile prefs = getFile(files, "org.eclipse.jdt.ui.prefs");
		String content = loadContent(properties.getModifiedContent(prefs).getContent(JavaFormatConfig.DEFAULT));
		assertThat(content).contains("Copyright " + year + " the original author or authors");

	}

	private ProjectSettingsFile getFile(ProjectSettingsFiles files, String name) {
		for (ProjectSettingsFile candidate : files) {
			if (candidate.getName().equals(name)) {
				return candidate;
			}
		}
		throw new IllegalStateException("No file " + name);
	}

	private void writeProperties(File file, String copyrightYear) throws IOException {
		Properties properties = new Properties();
		properties.setProperty("copyright-year", copyrightYear);
		try (FileOutputStream outputStream = new FileOutputStream(file)) {
			properties.store(outputStream, null);
		}
	}

	private String loadContent(InputStream stream) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
			return reader.lines().collect(Collectors.joining("\n"));
		}
	}

}
