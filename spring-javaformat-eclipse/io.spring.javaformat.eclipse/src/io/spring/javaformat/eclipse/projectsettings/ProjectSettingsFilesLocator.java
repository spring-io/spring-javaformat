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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Locates project settings files to be applied to projects.
 *
 * @author Phillip Webb
 */
public class ProjectSettingsFilesLocator {

	private static final String[] SOURCE_FOLDERS = { "eclipse", ".eclipse" };

	private static final String[] DEFAULT_FILES = { "org.eclipse.jdt.core.prefs", "org.eclipse.jdt.ui.prefs" };

	private final File[] searchFolders;

	public ProjectSettingsFilesLocator(File... searchFolders) {
		this.searchFolders = searchFolders;
	}

	public ProjectSettingsFilesLocator(Collection<File> searchFolders) {
		this.searchFolders = searchFolders.toArray(new File[0]);
	}

	public ProjectSettingsFiles locateSettingsFiles() throws IOException {
		ProjectProperties projectProperties = new ProjectProperties();
		Map<String, ProjectSettingsFile> files = new LinkedHashMap<>();
		for (File searchFolder : this.searchFolders) {
			for (String sourceFolder : SOURCE_FOLDERS) {
				add(projectProperties, files, new File(searchFolder, sourceFolder));
			}
		}
		for (String file : DEFAULT_FILES) {
			putIfAbsent(files, ProjectSettingsFile.fromClasspath(getClass(), file));
		}
		return new ProjectSettingsFiles(files.values(), projectProperties);
	}

	private void add(ProjectProperties projectProperties, Map<String, ProjectSettingsFile> files, File folder)
			throws IOException {
		if (folder.exists() && folder.isDirectory()) {
			for (File file : folder.listFiles(this::isPrefsFile)) {
				putIfAbsent(files, ProjectSettingsFile.fromFile(file));
			}
			projectProperties.addFromFolder(folder);
		}
	}

	private boolean isPrefsFile(File file) {
		return file.getName().toLowerCase().endsWith(".prefs");
	}

	private void putIfAbsent(Map<String, ProjectSettingsFile> files, ProjectSettingsFile candidate) {
		files.putIfAbsent(candidate.getName(), candidate);
	}

}
