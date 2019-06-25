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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

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
		for (ProjectSettingsFile file : this) {
			IFile destination = project.getFile(".settings/" + file.getName());
			try (InputStream content = this.projectProperties.getModifiedContent(file)) {
				if (!destination.exists()) {
					destination.create(new BufferedInputStream(content), true, monitor);
				}
				else {
					destination.setContents(new BufferedInputStream(content), IResource.FORCE, monitor);
				}
			}
		}
	}

}
