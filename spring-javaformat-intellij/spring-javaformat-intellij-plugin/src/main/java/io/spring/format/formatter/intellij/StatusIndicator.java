/*
 * Copyright 2012-2018 the original author or authors.
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

import java.awt.event.MouseEvent;

import javax.swing.Icon;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.Consumer;
import io.spring.format.formatter.intellij.codestyle.monitor.Trigger.State;

/**
 * Indicator used to show when Spring Formatting is active.
 *
 * @author Phillip Webb
 */
class StatusIndicator {

	private final Project project;

	private Component component;

	StatusIndicator(Project project) {
		this.project = project;
	}

	public void update(State state) {
		final StatusBar statusBar = WindowManager.getInstance()
				.getStatusBar(this.project);
		if (state == State.ACTIVE) {
			show(statusBar);
		}
		else {
			hide(statusBar);
		}
	}

	private void show(StatusBar statusBar) {
		if (this.component == null) {
			this.component = new Component();
			statusBar.addWidget(this.component, this.project);
		}
	}

	private void hide(final StatusBar statusBar) {
		if (this.component != null) {
			statusBar.removeWidget(this.component.ID());
			this.component = null;
		}
	}

	private static class Component
			implements StatusBarWidget, StatusBarWidget.IconPresentation {

		public static final Icon ICON = IconLoader
				.getIcon("/spring-javaformat/formatOn.png");

		@Override
		public String ID() {
			return "SpringFormat";
		}

		@Override
		public void install(StatusBar statusBar) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public WidgetPresentation getPresentation(PlatformType platformType) {
			return this;
		}

		@Override
		public Consumer<MouseEvent> getClickConsumer() {
			return null;
		}

		@Override
		public Icon getIcon() {
			return ICON;
		}

		@Override
		public String getTooltipText() {
			return "Spring Formatter Active";
		}

	}

}
