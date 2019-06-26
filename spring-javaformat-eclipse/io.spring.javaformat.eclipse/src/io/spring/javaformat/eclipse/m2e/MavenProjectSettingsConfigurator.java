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

package io.spring.javaformat.eclipse.m2e;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.project.MavenProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;

import io.spring.javaformat.eclipse.Executor;
import io.spring.javaformat.eclipse.Messages;
import io.spring.javaformat.eclipse.projectsettings.ProjectSettingsFiles;
import io.spring.javaformat.eclipse.projectsettings.ProjectSettingsFilesLocator;

/**
 * Configurator to apply project-specific settings to Maven projects.
 *
 * @author Phillip Webb
 */
public class MavenProjectSettingsConfigurator extends AbstractProjectConfigurator {

	@Override
	public void configure(ProjectConfigurationRequest request, IProgressMonitor monitor) throws CoreException {
		new Executor(Messages.springFormatSettingsImportError).run(() -> {
			List<File> searchFolders = getSearchFolders(request);
			ProjectSettingsFiles settingsFiles = new ProjectSettingsFilesLocator(searchFolders).locateSettingsFiles();
			settingsFiles.applyToProject(request.getProject(), monitor);
		});
	}

	private List<File> getSearchFolders(ProjectConfigurationRequest request) {
		List<File> files = new ArrayList<>();
		MavenProject project = request.getMavenProject();
		while (project != null && project.getBasedir() != null) {
			files.add(project.getBasedir());
			project = project.getParent();
		}
		return files;
	}

}
