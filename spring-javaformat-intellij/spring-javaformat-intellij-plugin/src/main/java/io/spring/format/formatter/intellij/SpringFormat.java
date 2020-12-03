/*
 * Copyright 2017-2020 the original author or authors.
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

package io.spring.format.formatter.intellij;

import java.lang.reflect.Method;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.serviceContainer.ComponentManagerImpl;
import org.picocontainer.MutablePicoContainer;

import io.spring.format.formatter.intellij.codestyle.SpringCodeStyleManager;
import io.spring.format.formatter.intellij.codestyle.monitor.FileMonitor;
import io.spring.format.formatter.intellij.codestyle.monitor.GradleMonitor;
import io.spring.format.formatter.intellij.codestyle.monitor.MavenMonitor;
import io.spring.format.formatter.intellij.codestyle.monitor.Monitors;
import io.spring.format.formatter.intellij.codestyle.monitor.Trigger.State;

/**
 * Spring Java Format IntelliJ support added to a {@link Project}.
 *
 * @author Phillip Webb
 */
public class SpringFormat {

	private static final String CODE_STYLE_MANAGER_KEY = CodeStyleManager.class.getName();

	private static final String ACTIVE_PROPERTY = SpringFormat.class.getName() + ".ACTIVE";

	private static final Logger logger = Logger.getInstance(SpringFormat.class);

	private final Project project;

	private final StatusIndicator statusIndicator;

	private final Lock lock = new ReentrantLock();

	private Monitors monitors;

	private PropertiesComponent properties;

	protected SpringFormat(Project project) {
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
			this.monitors.stop();
			this.monitors = null;
		}
	}

	private void update(State state) {
		this.lock.lock();
		try {
			CodeStyleManager manager = CodeStyleManager.getInstance(this.project);
			if (manager == null) {
				logger.warn("Unable to find exiting CodeStyleManager");
				return;
			}
			if (state == State.ACTIVE && !(manager instanceof SpringCodeStyleManager)) {
				logger.debug("Enabling SpringCodeStyleManager");
				registerCodeStyleManager(new SpringCodeStyleManager(manager));
				this.properties.setValue(ACTIVE_PROPERTY, true);
			}
			if (state == State.NOT_ACTIVE && (manager instanceof SpringCodeStyleManager)) {
				logger.debug("Disabling SpringCodeStyleManager");
				registerCodeStyleManager(((SpringCodeStyleManager) manager).getDelegate());
				this.properties.setValue(ACTIVE_PROPERTY, false);
			}
			ApplicationManager.getApplication().invokeLater(() -> this.statusIndicator.update(state));
		}
		finally {
			this.lock.unlock();
		}
	}

	private void registerCodeStyleManager(CodeStyleManager manager) {
		if (ApplicationInfo.getInstance().getBuild().getBaselineVersion() >= 193) {
			IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(PluginId.getId("spring-javaformat"));
			try {
				((ComponentManagerImpl) this.project).registerServiceInstance(CodeStyleManager.class, manager, plugin);
			}
			catch (NoSuchMethodError ex) {
				Method method = findRegisterServiceInstanceMethod(this.project.getClass());
				invokeRegisterServiceInstanceMethod(manager, plugin, method);
			}
		}
		else {
			MutablePicoContainer container = (MutablePicoContainer) this.project.getPicoContainer();
			container.unregisterComponent(CODE_STYLE_MANAGER_KEY);
			container.registerComponentInstance(CODE_STYLE_MANAGER_KEY, manager);
		}
	}

	private Method findRegisterServiceInstanceMethod(Class<?> projectClass) {
		if (projectClass != null) {
			Method[] methods = projectClass.getDeclaredMethods();
			for (Method method : methods) {
				if (method.getName().equals("registerServiceInstance") && method.getParameterCount() == 3) {
					if (PluginDescriptor.class.isAssignableFrom(method.getParameterTypes()[2])) {
						return method;
					}
				}
			}
			return findRegisterServiceInstanceMethod(projectClass.getSuperclass());
		}
		return null;
	}

	private void invokeRegisterServiceInstanceMethod(CodeStyleManager manager, IdeaPluginDescriptor plugin,
			Method method) {
		if (method == null) {
			throw new IllegalStateException("Unsupported IntelliJ IDEA version");
		}
		method.setAccessible(true);
		try {
			method.invoke(this.project, manager, plugin);
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

}
