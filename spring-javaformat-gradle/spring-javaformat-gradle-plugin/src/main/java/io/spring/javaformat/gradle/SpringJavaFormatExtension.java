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

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.quality.CheckstyleExtension;
import org.gradle.api.provider.Property;
import org.gradle.api.resources.TextResource;

/**
 * DSL extension for Spring Java Format.
 *
 * @author Andy Wilkinson
 */
public abstract class SpringJavaFormatExtension {

	private final Checkstyle checkstyle;

	@Inject
	public SpringJavaFormatExtension(Project project) {
		this.checkstyle = project.getObjects().newInstance(Checkstyle.class, project);
		this.checkstyle.getConfigureDependencies().convention(true);
	}

	public Checkstyle getCheckstyle() {
		return this.checkstyle;
	}

	public void checkstyle(Action<Checkstyle> action) {
		action.execute(this.checkstyle);
	}

	public abstract static class Checkstyle {

		private final Project project;

		@Inject
		public Checkstyle(Project project) {
			this.project = project;
		}

		/**
		 * Property that controls whether Checkstyle's dependencies should be configured to
		 * use Spring Java Format's checks.
		 * @return the property
		 */
		public abstract Property<Boolean> getConfigureDependencies();

		/**
		 * Applies Spring Java Format's default Checkstyle config, enabling all Spring checks.
		 * @see CheckstyleExtension#setConfig(TextResource)
		 */
		public void applyDefaultConfig() {
			CheckstyleExtension extension = this.project.getExtensions().getByType(CheckstyleExtension.class);
			StringWriter defaultConfig = new StringWriter();
			PrintWriter writer = new PrintWriter(defaultConfig);
			writer.println("<?xml version=\"1.0\"?>");
			writer.println("<!DOCTYPE module PUBLIC");
			writer.println("		\"-//Checkstyle//DTD Checkstyle Configuration 1.3//EN\"");
			writer.println("		\"https://checkstyle.org/dtds/configuration_1_3.dtd\">");
			writer.println("<module name=\"com.puppycrawl.tools.checkstyle.Checker\">");
			writer.println("	<module name=\"io.spring.javaformat.checkstyle.SpringChecks\">");
			writer.println("	</module>");
			writer.println("</module>");
			extension.setConfig(this.project.getResources().getText().fromString(defaultConfig.toString()));
		}

	}

}
