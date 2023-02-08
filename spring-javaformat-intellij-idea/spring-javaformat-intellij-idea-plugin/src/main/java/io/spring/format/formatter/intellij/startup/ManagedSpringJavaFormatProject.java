/*
 * Copyright 2017-2023 the original author or authors.
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

package io.spring.format.formatter.intellij.startup;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

import io.spring.format.formatter.intellij.monitor.FileMonitor;
import io.spring.format.formatter.intellij.monitor.GradleMonitor;
import io.spring.format.formatter.intellij.monitor.MavenMonitor;
import io.spring.format.formatter.intellij.monitor.Monitors;
import io.spring.format.formatter.intellij.state.State;
import io.spring.format.formatter.intellij.ui.StatusIndicator;

/**
 * Spring Java Format IntelliJ support added to a {@link Project}.
 *
 * @author Phillip Webb
 */
class ManagedSpringJavaFormatProject {

	private static final String ACTIVE_PROPERTY = ManagedSpringJavaFormatProject.class.getName() + ".ACTIVE";

	private static final Logger logger = Logger.getInstance(ManagedSpringJavaFormatProject.class);

	private final Project project;

	private final StatusIndicator statusIndicator;

	private final Lock lock = new ReentrantLock();

	private Monitors monitors;

	private PropertiesComponent properties;

	protected ManagedSpringJavaFormatProject(Project project) {
		logger.info("Initializing Spring Format for project " + project.getName());
		this.project = project;
		this.statusIndicator = new StatusIndicator(project);
		this.properties = PropertiesComponent.getInstance(project);
		if (this.properties.getBoolean(ACTIVE_PROPERTY, false)) {
			update(State.ACTIVE);
		}
		this.monitors = new Monitors(this.project, this::update, FileMonitor.factory(), MavenMonitor.factory(),
				GradleMonitor.factory());
		Disposer.register(project, this::dispose);
	}

	private void dispose() {
		if (this.monitors != null) {
			logger.info("Stopping monitors for " + this.project.getName());
			this.monitors.stop();
			this.monitors = null;
		}
	}

	private void update(State state) {
		logger.info("Updating state of " + this.project.getName() + " to " + state);
		this.lock.lock();
		try {
			state.put(this.project);
			ApplicationManager.getApplication().invokeLater(() -> this.statusIndicator.update(state));
		}
		finally {
			this.lock.unlock();
		}
	}

}
