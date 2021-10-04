/*
 * Copyright 2017-2021 the original author or authors.
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

import io.spring.javaformat.config.JavaFormatConfig;

/**
 * Locates project settings files to be applied to projects.
 *
 * @author Phillip Webb
 */
public class ProjectSettingsFilesLocator {

	private static final String JDT_CORE_PREFS = "org.eclipse.jdt.core.prefs";

	private static final String[] SOURCE_FOLDERS = { "eclipse", ".eclipse" };

	private static final String[] DEFAULT_FILES = { JDT_CORE_PREFS, "org.eclipse.jdt.ui.prefs" };

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
			putIfAbsent(files, getDefaultSettingsFile(file));
		}
		return new ProjectSettingsFiles(files.values(), projectProperties);
	}

	private ProjectSettingsFile getDefaultSettingsFile(String file) {
		ProjectSettingsFile settingsFile = ProjectSettingsFile.fromClasspath(getClass(), file);
		if (settingsFile.getName().equals(JDT_CORE_PREFS)) {
			settingsFile = settingsFile.withUpdatedContent(this::updateFormatter);
		}
		return settingsFile;
	}

	private String updateFormatter(JavaFormatConfig javaFormatConfig, String content) {
		String formatterId = getFormatterId(javaFormatConfig);
		if (formatterId != null) {
			return content.replace(
					"org.eclipse.jdt.core.javaFormatter=io.spring.javaformat.eclipse.formatter.jdk11.tabs",
					"org.eclipse.jdt.core.javaFormatter=" + formatterId);
		}
		return content;
	}

	private String getFormatterId(JavaFormatConfig config) {
		String jdk = config.getJavaBaseline().name().substring(1);
		String indentation = config.getIndentationStyle().name().toLowerCase();
		return "io.spring.javaformat.eclipse.formatter.jdk" + jdk + "." + indentation;
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
