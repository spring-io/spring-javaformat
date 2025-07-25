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

package io.spring.format.formatter.intellij.monitor;

import java.util.Collection;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.model.DataNode;
import com.intellij.openapi.externalSystem.model.ExternalProjectInfo;
import com.intellij.openapi.externalSystem.model.task.TaskData;
import com.intellij.openapi.externalSystem.service.project.ProjectDataManager;
import com.intellij.openapi.externalSystem.service.project.manage.ProjectDataImportListener;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.gradle.util.GradleConstants;

import io.spring.format.formatter.intellij.state.State;

/**
 * {@link Monitor} that looks for a {@code spring-javaformat-gradle-plugin} declaration in
 * the build.gradle file.
 *
 * @author Phillip Webb
 */
public class GradleMonitor extends Monitor {

	private static final Logger logger = Logger.getInstance(GradleMonitor.class);

	private static final String FORMAT_TASK = "io.spring.javaformat.gradle.tasks.Format";

	public GradleMonitor(Project project, Trigger trigger) {
		super(project, trigger);
		MessageBusConnection messageBus = project.getMessageBus().connect();
		messageBus.subscribe(ProjectDataImportListener.TOPIC, new ProjectDataImportListener() {

			@Override
			public void onImportFinished(@Nullable String projectPath) {
				check();
			}

		});
	}

	private void check() {
		logger.info("Checking " + getProject().getName() + " for use of Spring Java Format");
		ProjectDataManager projectDataManager = ProjectDataManager.getInstance();
		boolean hasFormatPlugin = hasFormatPlugin(
				projectDataManager.getExternalProjectsData(getProject(), GradleConstants.SYSTEM_ID));
		getTrigger().updateState(hasFormatPlugin ? State.ACTIVE : State.NOT_ACTIVE);
	}

	private boolean hasFormatPlugin(Collection<ExternalProjectInfo> projectInfos) {
		for (ExternalProjectInfo projectInfo : projectInfos) {
			if (hasFormatPlugin(projectInfo.getExternalProjectStructure())) {
				logger.info(projectInfo + " uses Spring Java Format");
				return true;
			}
		}
		return false;
	}

	private boolean hasFormatPlugin(DataNode<?> node) {
		if (node == null) {
			return false;
		}
		Object data = node.getData();
		if (data instanceof TaskData && isFormatPlugin((TaskData) data)) {
			return true;
		}
		for (DataNode<?> child : node.getChildren()) {
			if (hasFormatPlugin(child)) {
				return true;
			}
		}
		return false;
	}

	private boolean isFormatPlugin(TaskData data) {
		return FORMAT_TASK.equals(data.getType());
	}

	public static Factory factory() {
		return (project, trigger) -> {
			return new GradleMonitor(project, trigger);
		};
	}

}
