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

package io.spring.format.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaBasePlugin;

/**
 * Spring Format Gradle Plugin.
 *
 * @author Phillip Webb
 */
public class SpringFormatPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.afterEvaluate(this::createTasks);
	}

	private void createTasks(Project project) {
		addCheckTask(project);
		addApplyTask(project);
	}

	private void addCheckTask(Project project) {
		ValidateTask checkTask = project.getTasks().create(ValidateTask.NAME,
				ValidateTask.class);
		project.getTasks().matching(this::isJavaCheckTask)
				.all(task -> task.dependsOn(checkTask));
	}

	private boolean isJavaCheckTask(Task task) {
		return task.getName().equals(JavaBasePlugin.CHECK_TASK_NAME);
	}

	private void addApplyTask(Project project) {
		ApplyTask applyTask = project.getTasks().create(ApplyTask.NAME, ApplyTask.class);
		applyTask.setDescription(ApplyTask.DESCRIPTION);
	}

}
