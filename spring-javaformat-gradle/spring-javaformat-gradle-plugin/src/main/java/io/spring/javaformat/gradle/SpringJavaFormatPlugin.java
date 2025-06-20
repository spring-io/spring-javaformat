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

package io.spring.javaformat.gradle;

import java.io.File;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import io.spring.javaformat.config.JavaFormatConfig;
import io.spring.javaformat.gradle.tasks.CheckFormat;
import io.spring.javaformat.gradle.tasks.Format;
import io.spring.javaformat.gradle.tasks.FormatterTask;

/**
 * Spring Format Gradle Plugin.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
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
			TaskContainer tasks = this.project.getTasks();
			TaskProvider<Task> formatAllProvider = tasks.register(Format.NAME);
			formatAllProvider.configure((formatAll) -> formatAll.setDescription(Format.DESCRIPTION));
			TaskProvider<Task> checkAllProvider = tasks.register(CheckFormat.NAME);
			checkAllProvider.configure((checkAll) -> checkAll.setDescription(CheckFormat.DESCRIPTION));
			tasks.named(JavaBasePlugin.CHECK_TASK_NAME).configure((check) -> check.dependsOn(checkAllProvider));
			this.project.getExtensions()
				.getByType(JavaPluginExtension.class)
				.getSourceSets()
				.all((sourceSet) -> addSourceTasks(sourceSet, checkAllProvider, formatAllProvider));
		});
	}

	private void addSourceTasks(SourceSet sourceSet, TaskProvider<Task> checkAllProvider,
			TaskProvider<Task> formatAllProvider) {
		TaskProvider<CheckFormat> checkTaskProvider = addFormatterTask(sourceSet, CheckFormat.class, CheckFormat.NAME,
				CheckFormat.DESCRIPTION);
		checkTaskProvider.configure((checkTask) -> checkTask.setReportLocation(
				new File(this.project.getBuildDir(), "reports/format/" + sourceSet.getName() + "/check-format.txt")));
		checkAllProvider.configure((checkAll) -> checkAll.dependsOn(checkTaskProvider));
		TaskProvider<Format> formatTaskProvider = addFormatterTask(sourceSet, Format.class, Format.NAME,
				Format.DESCRIPTION);
		formatTaskProvider.configure((format) -> format.conventionMapping("encoding", () -> "UTF-8"));
		formatAllProvider.configure((formatAll) -> formatAll.dependsOn(formatTaskProvider));
	}

	private <T extends FormatterTask> TaskProvider<T> addFormatterTask(SourceSet sourceSet, Class<T> taskType,
			String name, String desc) {
		String taskName = sourceSet.getTaskName(name, null);
		TaskProvider<T> provider = this.project.getTasks().register(taskName, taskType);
		provider.configure((task) -> {
			task.setDescription(desc + " for " + sourceSet.getName());
			task.setSource(sourceSet.getAllJava());
			JavaFormatConfig config = JavaFormatConfig.findFrom(this.project.getProjectDir());
			task.getIndentationStyle().convention(config.getIndentationStyle());
			task.getJavaBaseline().convention(config.getJavaBaseline());
		});
		return provider;
	}

}
