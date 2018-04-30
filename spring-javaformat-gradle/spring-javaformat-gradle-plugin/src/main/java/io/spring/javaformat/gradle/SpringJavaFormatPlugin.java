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

package io.spring.javaformat.gradle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.ConventionMapping;
import org.gradle.api.internal.IConventionAware;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;

/**
 * Spring Format Gradle Plugin.
 *
 * @author Phillip Webb
 */
public class SpringJavaFormatPlugin implements Plugin<Project> {

	private Project project;

	private SpringJavaFormatExtension extension;

	@Override
	public void apply(Project project) {
		this.project = project;
		this.extension = createExtension();
		project.getTasks().withType(FormatterTask.class, this::configureTaskDefault);
		addSourceTasks();
		configureCheckDependents();
		addApplyTask();
	}

	private SpringJavaFormatExtension createExtension() {
		SpringJavaFormatExtension extension = this.project.getExtensions().create(
				"springJavaFormat", SpringJavaFormatExtension.class, this.project);
		ConventionMapping mapping = ((IConventionAware) extension).getConventionMapping();
		configureExtensionConections(mapping);
		return extension;
	}

	private void configureExtensionConections(ConventionMapping mapping) {
		mapping.map("sourceSets", ArrayList::new);
		this.project.getPlugins().withType(JavaBasePlugin.class, (plugin) -> {
			mapping.map("sourceSets", getJavaPluginConvention()::getSourceSets);
		});
		mapping.map("encoding",
				() -> this.project.getTasks().withType(JavaCompile.class).stream()
						.findFirst().map(JavaCompile::getOptions)
						.map(CompileOptions::getEncoding).orElse(null));
	}

	private void configureTaskDefault(FormatterTask formatTask) {
		ConventionMapping mapping = formatTask.getConventionMapping();
		mapping.map("encoding", () -> this.extension.getEncoding());
	}

	private void addSourceTasks() {
		this.project.getPlugins().withType(JavaBasePlugin.class, (plugin) -> {
			getJavaPluginConvention().getSourceSets().all(this::addSourceTasks);
		});
	}

	private void addSourceTasks(SourceSet sourceSet) {
		addSourceTask(sourceSet, CheckTask.class, CheckTask.NAME, CheckTask.DESCRIPTION);
		addSourceTask(sourceSet, FormatTask.class, FormatTask.NAME, FormatTask.DESCRIPTION);
	}

	private void addSourceTask(SourceSet sourceSet, Class<? extends FormatterTask> taskType,
			String name, String desc) {
		String taskName = sourceSet.getTaskName(name, null);
		SourceTask task = this.project.getTasks().create(taskName, taskType);
		task.setDescription(desc + " for " + sourceSet.getName());
		task.setSource(sourceSet.getAllJava());
	}

	private void configureCheckDependents() {
		this.project.getPlugins().withType(JavaBasePlugin.class, (plugin) -> {
			this.project.getTasks().getByName(JavaBasePlugin.CHECK_TASK_NAME)
					.dependsOn(getDependsOnSourcesTask(CheckTask.NAME));
		});
	}

	private void addApplyTask() {
		Task task = this.project.task(FormatTask.NAME);
		task.setDescription(FormatTask.DESCRIPTION);
		task.dependsOn(getDependsOnSourcesTask(FormatTask.NAME));
	}

	private Callable<List<String>> getDependsOnSourcesTask(String taskName) {
		return () -> this.extension.getSourceSets().stream()
				.map((sourceSet) -> sourceSet.getTaskName(taskName, null))
				.collect(Collectors.toList());
	}

	private JavaPluginConvention getJavaPluginConvention() {
		return this.project.getConvention().getPlugin(JavaPluginConvention.class);
	}

}
