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

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.internal.file.UnionFileCollection;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;

/**
 * Base class for formatter tasks.
 *
 * @author Phillip Webb
 */
public abstract class FormatterTask extends DefaultTask {

	/**
	 * The files that will be processed.
	 */
	public Iterable<File> files;

	/**
	 * The file encoding.
	 */
	public Charset encoding = StandardCharsets.UTF_8;

	public FormatterTask() {
		if (this.files == null) {
			JavaPluginConvention javaPlugin = getProject().getConvention()
					.findPlugin(JavaPluginConvention.class);
			if (javaPlugin == null) {
				throw new GradleException(
						"You must apply the java plugin before the spring-format plugin.");
			}
			UnionFileCollection union = new UnionFileCollection();
			for (SourceSet sourceSet : javaPlugin.getSourceSets()) {
				union.add(sourceSet.getJava());
			}
			this.files = union;
		}
	}

	@TaskAction
	public abstract void run() throws Exception;

}
