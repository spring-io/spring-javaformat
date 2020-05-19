/*
 * Copyright 2017-2020 the original author or authors.
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

package io.spring.javaformat.eclipse.gradle;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.InitializationContext;
import org.eclipse.buildship.core.ProjectConfigurator;
import org.eclipse.buildship.core.ProjectContext;
import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.workspace.FetchStrategy;
import org.eclipse.buildship.core.internal.workspace.InternalGradleBuild;
import org.eclipse.buildship.core.internal.workspace.InternalGradleWorkspace;
import org.eclipse.buildship.core.internal.workspace.ModelProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.model.GradleTask;
import org.gradle.tooling.model.eclipse.EclipseProject;

import io.spring.javaformat.eclipse.projectsettings.ProjectSettingsFilesLocator;

/**
 * {@link ProjectConfigurator} to apply project-specific settings to Gradle projects.
 *
 * @author Andy Wilkinson
 * @author Phillip Webb
 */
@SuppressWarnings("restriction")
public class GradleProjectSettingsConfigurator implements ProjectConfigurator {

	private static final Object TASK_NAME = "checkFormatMain";

	private CancellationTokenSource tokenSource;

	@Override
	public void init(InitializationContext context, IProgressMonitor monitor) {
		this.tokenSource = GradleConnector.newCancellationTokenSource();

	}

	@Override
	public void configure(ProjectContext context, IProgressMonitor monitor) {
		try {
			configureProject(context.getProject(), monitor);
		}
		catch (Exception ex) {
			context.error("Failed to apply project settings", ex);
		}
	}

	private void configureProject(IProject project, IProgressMonitor monitor) throws CoreException, IOException {
		InternalGradleWorkspace workspace = CorePlugin.internalGradleWorkspace();
		Optional<GradleBuild> build = workspace.getBuild(project);
		if (build.isPresent()) {
			ModelProvider modelProvider = ((InternalGradleBuild) build.get()).getModelProvider();
			Collection<EclipseProject> rootProjects = modelProvider.fetchModels(EclipseProject.class,
					FetchStrategy.FORCE_RELOAD, this.tokenSource, monitor);
			EclipseProject eclipseProject = findProjectByName(rootProjects, project.getName());
			if (hasSpringFormatPlugin(eclipseProject)) {
				ProjectSettingsFilesLocator locator = new ProjectSettingsFilesLocator(getSearchFolders(rootProjects));
				locator.locateSettingsFiles().applyToProject(project, monitor);
			}
		}
	}

	private EclipseProject findProjectByName(Iterable<? extends EclipseProject> candidates, String name) {
		for (EclipseProject candidate : candidates) {
			if (name.equals(candidate.getName())) {
				return candidate;
			}
			EclipseProject childResult = findProjectByName(candidate.getChildren(), name);
			if (childResult != null) {
				return childResult;
			}
		}
		return null;
	}

	private boolean hasSpringFormatPlugin(EclipseProject eclipseProject) {
		if (eclipseProject != null) {
			for (GradleTask task : eclipseProject.getGradleProject().getTasks()) {
				if (isSpringFormatPlugin(task)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isSpringFormatPlugin(GradleTask task) {
		return TASK_NAME.equals(task.getName());
	}

	private Set<File> getSearchFolders(Collection<EclipseProject> projects) {
		Set<File> searchFolders = new LinkedHashSet<>();
		for (EclipseProject project : projects) {
			while (project != null) {
				searchFolders.add(project.getProjectDirectory());
				project = project.getParent();
			}
		}
		return searchFolders;
	}

	@Override
	public void unconfigure(ProjectContext context, IProgressMonitor monitor) {
	}

}
