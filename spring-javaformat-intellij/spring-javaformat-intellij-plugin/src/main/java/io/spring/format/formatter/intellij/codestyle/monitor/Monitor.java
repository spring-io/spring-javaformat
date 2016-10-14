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

import com.intellij.openapi.project.Project;

/**
 * Class used to monitor a {@link Project} to trigger when the plugin should become active
 * or non-active.
 *
 * @author Phillip Webb
 */
public class Monitor {

	private final Project project;

	private final Trigger trigger;

	/**
	 * Start monitoring the project.
	 * @param project the project to monitor.
	 * @param trigger the trigger used to update
	 */
	public Monitor(Project project, Trigger trigger) {
		this.project = project;
		this.trigger = trigger;
	}

	protected final Project getProject() {
		return this.project;
	}

	protected final Trigger getTrigger() {
		return this.trigger;
	}

	/**
	 * Stop monitoring the project.
	 */
	public void stop() {
	}

	/**
	 * Factory used to create a {@link Monitor}.
	 */
	public interface Factory {

		/**
		 * Create a new {@link Monitor}.
		 * @param project the source project
		 * @param trigger the trigger used to update
		 * @return the monitor
		 */
		Monitor createMonitor(Project project, Trigger trigger);

	}

}
