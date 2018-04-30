/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.format.formatter.intellij.codestyle.monitor;

import java.util.List;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import io.spring.format.formatter.intellij.codestyle.monitor.Trigger.State;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectChanges;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.project.MavenProjectsTree.Listener;
import org.jetbrains.idea.maven.server.NativeMavenProjectHolder;

/**
 * {@link Monitor} that looks for a {@code spring-javaformat-maven-plugin} declaration in
 * the POM.xml file.
 *
 * @author Phillip Webb
 */
public class MavenMonitor extends Monitor {

	private static final String PLUGIN_GROUP_ID = "io.spring.javaformat";

	private static final String PLUGIN_ARTIFACT_ID = "spring-javaformat-maven-plugin";

	private final MavenProjectsManager mavenProjectsManager;

	public MavenMonitor(Project project, Trigger trigger,
			MavenProjectsManager mavenProjectsManager) {
		super(project, trigger);
		this.mavenProjectsManager = mavenProjectsManager;
		attachListener(mavenProjectsManager);
		check();
	}

	private void attachListener(MavenProjectsManager mavenProjectsManager) {
		mavenProjectsManager.addProjectsTreeListener(new Listener() {

			@Override
			public void projectsUpdated(
					List<Pair<MavenProject, MavenProjectChanges>> updated,
					List<MavenProject> deleted) {
				check();
			}

			@Override
			public void projectResolved(
					Pair<MavenProject, MavenProjectChanges> projectWithChanges,
					NativeMavenProjectHolder nativeMavenProject) {
				check();
			}

			@Override
			public void pluginsResolved(MavenProject project) {
				check();
			}

		});
	}

	private void check() {
		check(this.mavenProjectsManager.getProjects());
	}

	private void check(List<MavenProject> projects) {
		State state = (hasSpringFormatPlugin(projects) ? State.ACTIVE : State.NOT_ACTIVE);
		getTrigger().updateState(state);
	}

	private boolean hasSpringFormatPlugin(List<MavenProject> projects) {
		for (MavenProject project : projects) {
			if (project.findPlugin(PLUGIN_GROUP_ID, PLUGIN_ARTIFACT_ID) != null) {
				return true;
			}
		}
		return false;
	}

	public static Factory factory() {
		return (project, trigger) -> {
			MavenProjectsManager mavenProjectsManager = MavenProjectsManager
					.getInstance(project);
			return (mavenProjectsManager == null ? null
					: new MavenMonitor(project, trigger, mavenProjectsManager));
		};
	}

}
