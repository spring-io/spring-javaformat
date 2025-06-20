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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import io.spring.javaformat.config.JavaFormatConfig;

/**
 * A collection of {@link ProjectSettingsFile project setting files}.
 *
 * @author Phillip Webb
 */
public class ProjectSettingsFiles implements Iterable<ProjectSettingsFile> {

	private final List<ProjectSettingsFile> files;

	private final ProjectProperties projectProperties;

	/**
	 * Create a new {@link ProjectSettingsFiles} instances with the specified files.
	 * @param files the project settings files
	 * @param projectProperties project properties
	 */
	public ProjectSettingsFiles(Collection<ProjectSettingsFile> files, ProjectProperties projectProperties) {
		this.files = new ArrayList<>(files);
		this.projectProperties = projectProperties;
	}

	@Override
	public Iterator<ProjectSettingsFile> iterator() {
		return this.files.iterator();
	}

	/**
	 * Apply the settings files to the given eclipse project.
	 * @param project the project to apply the settings to
	 * @param monitor a progress monitor
	 * @throws IOException on IO error
	 * @throws CoreException on eclipse file creation failure
	 */
	public void applyToProject(IProject project, IProgressMonitor monitor) throws IOException, CoreException {
		JavaFormatConfig javaFormatConfig = getJavaFormatConfig(project);
		for (ProjectSettingsFile file : this) {
			file = this.projectProperties.getModifiedContent(file);
			IFile destination = project.getFile(".settings/" + file.getName());
			try (InputStream content = file.getContent(javaFormatConfig)) {
				if (!destination.exists()) {
					destination.create(new BufferedInputStream(content), true, monitor);
				}
				else {
					Properties properties = new OrderedProperties();
					try (InputStream existingContent = destination.getContents(true)) {
						if (existingContent != null) {
							properties.load(existingContent);
						}
					}
					properties.load(content);
					destination.setContents(
							new ByteArrayInputStream(stripTimestamp(properties).getBytes(StandardCharsets.UTF_8)),
							IResource.FORCE, monitor);
				}
			}
		}
	}

	private String stripTimestamp(Properties properties) throws IOException {
		try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			properties.store(output, null);
			String string = output.toString(StandardCharsets.UTF_8);
			String separator = System.getProperty("line.separator");
			return string.substring(string.indexOf(separator) + separator.length());
		}
	}

	private JavaFormatConfig getJavaFormatConfig(IProject project) {
		try {
			IPath location = project.getLocation();
			File file = (location != null) ? location.toFile() : null;
			return JavaFormatConfig.findFrom(file);
		}
		catch (Exception ex) {
			return JavaFormatConfig.DEFAULT;
		}
	}

	static class OrderedProperties extends Properties {

		private static final long serialVersionUID = 1L;

		@Override
		public Set<Map.Entry<Object, Object>> entrySet() {
			Set<Map.Entry<Object, Object>> set = new TreeSet<Map.Entry<Object, Object>>(new MapEntryKeyComparator());
			set.addAll(super.entrySet());
			return set;
		};

	}

	private static class MapEntryKeyComparator implements Comparator<Map.Entry<Object, Object>> {

		@Override
		public int compare(Entry<Object, Object> o1, Entry<Object, Object> o2) {
			Object k1 = o1.getKey();
			Object k2 = o2.getKey();
			return String.valueOf(k1).compareTo(String.valueOf(k2));
		}

	}

}
