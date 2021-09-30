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

package io.spring.javaformat.gradle;

import java.io.File;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

import io.spring.javaformat.gradle.tasks.CheckFormat;
import io.spring.javaformat.gradle.tasks.Format;
import io.spring.javaformat.gradle.tasks.FormatterTask;

/**
 * Spring Format Gradle Plugin.
 *
 * @author Phillip Webb
 */
public class SpringJavaFormatPlugin implements Plugin<Project> {

	private Project project;

	@Override
	public void apply(Project project) {
		this.project = project;
		addSourceTasks();
	}

	private void addSourceTasks() {
		this.project.getPlugins().withType(JavaBasePlugin.class, (plugin) -> {
			Task formatAll = this.project.task(Format.NAME);
			formatAll.setDescription(Format.DESCRIPTION);
			Task checkAll = this.project.task(CheckFormat.NAME);
			checkAll.setDescription(CheckFormat.DESCRIPTION);
			this.project.getTasks().getByName(JavaBasePlugin.CHECK_TASK_NAME).dependsOn(checkAll);
			this.project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets()
					.all((sourceSet) -> addSourceTasks(sourceSet, checkAll, formatAll));
		});
	}

	private void addSourceTasks(SourceSet sourceSet, Task checkAll, Task formatAll) {
		CheckFormat checkTask = addFormatterTask(sourceSet, CheckFormat.class, CheckFormat.NAME,
				CheckFormat.DESCRIPTION);
		checkTask.setReportLocation(
				new File(this.project.getBuildDir(), "reports/format/" + sourceSet.getName() + "/check-format.txt"));
		checkAll.dependsOn(checkTask);
		Format formatSourceSet = addFormatterTask(sourceSet, Format.class, Format.NAME, Format.DESCRIPTION);
		formatSourceSet.conventionMapping("encoding", () -> "UTF-8");
		formatAll.dependsOn(formatSourceSet);
	}

	private <T extends FormatterTask> T addFormatterTask(SourceSet sourceSet, Class<T> taskType, String name,
			String desc) {
		String taskName = sourceSet.getTaskName(name, null);
		T task = this.project.getTasks().create(taskName, taskType);
		task.setDescription(desc + " for " + sourceSet.getName());
		task.setSource(sourceSet.getAllJava());
		return task;
	}

}
