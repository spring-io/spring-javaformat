/*
 * Copyright 2017-2018 the original author or authors.
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

package io.spring.format.formatter.intellij;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.intellij.designer.designSurface.ScalableComponent;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.CodeStyleManager;
import org.picocontainer.MutablePicoContainer;

import io.spring.format.formatter.intellij.codestyle.SpringCodeStyleManager;
import io.spring.format.formatter.intellij.codestyle.monitor.FileMonitor;
import io.spring.format.formatter.intellij.codestyle.monitor.GradleMonitor;
import io.spring.format.formatter.intellij.codestyle.monitor.MavenMonitor;
import io.spring.format.formatter.intellij.codestyle.monitor.Monitors;
import io.spring.format.formatter.intellij.codestyle.monitor.Trigger.State;

/**
 * {@link ProjectComponent} to add Spring Java Format IntelliJ support.
 *
 * @author Phillip Webb
 */
public class SpringFormatComponent extends AbstractProjectComponent {

	private static final String CODE_STYLE_MANAGER_KEY = CodeStyleManager.class.getName();

	private static final String ACTIVE_PROPERTY = ScalableComponent.class.getName()
			+ ".ACTIVE";

	private final StatusIndicator statusIndicator;

	private final Lock lock = new ReentrantLock();

	private Monitors monitors;

	private static final Logger logger = Logger.getInstance(SpringFormatComponent.class);

	private PropertiesComponent properties;

	protected SpringFormatComponent(Project project) {
		super(project);
		this.statusIndicator = new StatusIndicator(project);
		this.properties = PropertiesComponent.getInstance(project);
	}

	@Override
	public void initComponent() {
		if (this.properties.getBoolean(ACTIVE_PROPERTY, false)) {
			update(State.ACTIVE);
		}
		this.monitors = new Monitors(this.myProject, this::update, FileMonitor.factory(),
				MavenMonitor.factory(), GradleMonitor.factory());
	}

	@Override
	public void disposeComponent() {
		if (this.monitors != null) {
			this.monitors.stop();
			this.monitors = null;
		}
	}

	private void update(State state) {
		this.lock.lock();
		try {
			CodeStyleManager manager = CodeStyleManager.getInstance(this.myProject);
			if (manager == null) {
				logger.warn("Unable to find exiting CodeStyleManager");
				return;
			}
			if (state == State.ACTIVE && !(manager instanceof SpringCodeStyleManager)) {
				logger.debug("Enabling SpringCodeStyleManager");
				reregisterComponent(new SpringCodeStyleManager(manager));
				this.properties.setValue(ACTIVE_PROPERTY, true);
			}
			if (state == State.NOT_ACTIVE
					&& (manager instanceof SpringCodeStyleManager)) {
				logger.debug("Disabling SpringCodeStyleManager");
				reregisterComponent(((SpringCodeStyleManager) manager).getDelegate());
				this.properties.setValue(ACTIVE_PROPERTY, false);
			}
			ApplicationManager.getApplication()
					.invokeLater(() -> this.statusIndicator.update(state));
		}
		finally {
			this.lock.unlock();
		}
	}

	private void reregisterComponent(CodeStyleManager manager) {
		MutablePicoContainer container = (MutablePicoContainer) this.myProject
				.getPicoContainer();
		container.unregisterComponent(CODE_STYLE_MANAGER_KEY);
		container.registerComponentInstance(CODE_STYLE_MANAGER_KEY, manager);
	}

}
