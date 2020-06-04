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

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.buildship.core.ProjectConfigurator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata;
import org.eclipse.m2e.core.project.IMavenProjectChangedListener;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.MojoExecutionKey;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;

import io.spring.javaformat.eclipse.preferences.PreferenceSetter;

/**
 * {@link ProjectConfigurator} to apply project-specific settings to Gradle
 * projects.
 *
 * @author Andy Wilkinson
 * @author Phillip Webb
 */
@SuppressWarnings("restriction")
public class GradleProjectSettingsConfigurator extends AbstractProjectConfigurator {

	private final static IMavenProjectChangedListener projectChangedListener = new IMavenProjectChangedListener() {
		@Override
		public void mavenProjectChanged(final MavenProjectChangedEvent[] events, final IProgressMonitor monitor) {
			for (final MavenProjectChangedEvent event : events) {
				updateProjectSettings(event, monitor);
			}
		}
	};

	@Override
	public void setProjectManager(final IMavenProjectRegistry projectManager) {
		projectManager.addMavenProjectChangedListener(this.projectChangedListener);
		super.setProjectManager(projectManager);
	}

	public static void updateProjectSettings(final MavenProjectChangedEvent event, final IProgressMonitor monitor) {
		switch (event.getKind()) {
		case MavenProjectChangedEvent.KIND_ADDED:
			// opening project
			break;
		case MavenProjectChangedEvent.KIND_CHANGED:
			final PreferenceSetter setter = new PreferenceSetter(event.getMavenProject().getProject());
			setter.reset();
			if (thePluginIsInThePom(event)) {
				setter.set();
			}
			break;
		case MavenProjectChangedEvent.KIND_REMOVED:
			// closing project
			break;
		}
	}

	private static boolean thePluginIsInThePom(final MavenProjectChangedEvent event) {
		if (event.getMavenProject() != null) {
			for (final Entry<MojoExecutionKey, List<IPluginExecutionMetadata>> m : event.getMavenProject()
					.getMojoExecutionMapping().entrySet()) {
				if ("amiga-javaformat-maven-plugin".equals(m.getKey().getArtifactId())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void configure(final ProjectConfigurationRequest request, final IProgressMonitor monitor)
			throws CoreException {
		// Nothing. The listener acts in configuration like a normal modification
	}
}
