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

package io.spring.javaformat.eclipse.gradle;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.workspace.InternalGradleBuild;
import org.eclipse.buildship.core.internal.workspace.InternalGradleWorkspace;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.model.GradleTask;
import org.gradle.tooling.model.eclipse.EclipseProject;

import io.spring.javaformat.eclipse.Executor;
import io.spring.javaformat.eclipse.Messages;
import io.spring.javaformat.eclipse.projectsettings.ProjectSettingsFilesLocator;

/**
 * Job to trigger refresh of project specific settings when the gradle plugin is used.
 *
 * @author Phillip Webb
 */
@SuppressWarnings("restriction")
public class RefreshProjectsSettingsJob extends Job {

	private static final Object TASK_NAME = "checkFormatMain";

	private final CancellationTokenSource tokenSource;

	public RefreshProjectsSettingsJob() {
		super("Refresh spring-javaformat project settings");
		this.tokenSource = GradleConnector.newCancellationTokenSource();
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			new Executor(Messages.springFormatSettingsImportError).run(() -> {
				configureProjects(monitor);
			});
		}
		catch (CoreException ex) {
			return ex.getStatus();
		}
		return Status.OK_STATUS;
	}

	private void configureProjects(IProgressMonitor monitor) throws CoreException, IOException {
		InternalGradleWorkspace workspace = CorePlugin.internalGradleWorkspace();
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			Optional<GradleBuild> build = workspace.getBuild(project);
			if (build.isPresent()) {
				configureProject(project, (InternalGradleBuild) build.get(), monitor);
			}
		}
	}

	private void configureProject(IProject project, InternalGradleBuild build, IProgressMonitor monitor)
			throws CoreException, IOException {
		Collection<EclipseProject> projects = build.getModelProvider()
				.fetchEclipseProjectAndRunSyncTasks(this.tokenSource, monitor);
		if (hasSpringFormatPlugin(projects)) {
			ProjectSettingsFilesLocator locator = new ProjectSettingsFilesLocator(getSearchFolders(projects));
			locator.locateSettingsFiles().applyToProject(project, monitor);
		}
	}

	private boolean hasSpringFormatPlugin(Collection<EclipseProject> projects) {
		for (EclipseProject project : projects) {
			for (GradleTask task : project.getGradleProject().getTasks()) {
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

}
