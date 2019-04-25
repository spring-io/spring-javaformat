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

package io.spring.javaformat.eclipse.projectsettings;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ProjectSettingsFilesLocator}.
 *
 * @author Phillip Webb
 */
public class ProjectSettingsFilesLocatorTests {

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	@Test
	public void locateSettingsFilesWhenNoFoldersShouldReturnDefault() throws IOException {
		ProjectSettingsFiles files = new ProjectSettingsFilesLocator().locateSettingsFiles();
		assertThat(files.iterator()).extracting(ProjectSettingsFile::getName).containsOnly("org.eclipse.jdt.core.prefs",
				"org.eclipse.jdt.ui.prefs");
	}

	@Test
	public void locateSettingsFilesOnlyFindPrefs() throws Exception {
		File folder = this.temp.newFolder();
		writeFile(folder, "foo.prefs");
		writeFile(folder, "bar.notprefs");
		ProjectSettingsFiles files = new ProjectSettingsFilesLocator(folder).locateSettingsFiles();
		assertThat(files.iterator()).extracting(ProjectSettingsFile::getName).containsOnly("org.eclipse.jdt.core.prefs",
				"org.eclipse.jdt.ui.prefs", "foo.prefs");
	}

	@Test
	public void locateSettingsFilesWhenMultipleFoldersFindsInEarliest() throws Exception {
		File folder1 = this.temp.newFolder();
		writeFile(folder1, "foo.prefs", "foo1");
		File folder2 = this.temp.newFolder();
		writeFile(folder2, "foo.prefs", "foo2");
		writeFile(folder2, "org.eclipse.jdt.core.prefs", "core2");
		ProjectSettingsFiles files = new ProjectSettingsFilesLocator(folder1, folder2).locateSettingsFiles();
		Map<String, ProjectSettingsFile> found = new LinkedHashMap<>();
		files.iterator().forEachRemaining((f) -> found.put(f.getName(), f));
		assertThat(found.get("foo.prefs").getContent()).hasSameContentAs(new ByteArrayInputStream("foo1".getBytes()));
		assertThat(found.get("org.eclipse.jdt.core.prefs").getContent())
				.hasSameContentAs(new ByteArrayInputStream("core2".getBytes()));
	}

	private void writeFile(File folder, String name) throws IOException {
		writeFile(folder, name, name);
	}

	private void writeFile(File folder, String name, String content) throws IOException {
		File eclipseFolder = new File(folder, ".eclipse");
		if (!eclipseFolder.exists()) {
			eclipseFolder.mkdirs();
		}
		File file = new File(eclipseFolder, name);
		try (PrintWriter writer = new PrintWriter(file)) {
			writer.write(content);
		}
	}

}
