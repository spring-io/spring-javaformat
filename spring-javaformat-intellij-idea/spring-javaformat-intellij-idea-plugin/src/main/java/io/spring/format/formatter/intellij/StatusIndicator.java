/*
 * Copyright 2012-2020 the original author or authors.
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

import java.awt.event.MouseEvent;
import java.util.concurrent.TimeUnit;

import javax.swing.Icon;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.Consumer;
import com.intellij.util.concurrency.AppExecutorUtil;

import io.spring.format.formatter.intellij.codestyle.monitor.Trigger.State;

/**
 * Indicator used to show when Spring Formatting is active.
 *
 * @author Phillip Webb
 */
class StatusIndicator {

	private final Project project;

	private Widget widget;

	StatusIndicator(Project project) {
		this.project = project;
	}

	public void update(State state) {
		WindowManager windowManager = WindowManager.getInstance();
		final StatusBar statusBar = windowManager.getStatusBar(this.project);
		if (statusBar == null) {
			AppExecutorUtil.getAppScheduledExecutorService().schedule(() -> retryUpdate(state), 1, TimeUnit.SECONDS);
			return;
		}
		if (state == State.ACTIVE) {
			show(statusBar);
		}
		else {
			hide(statusBar);
		}
	}

	private void retryUpdate(State state) {
		ApplicationManager.getApplication().invokeLater(() -> update(state));
	}

	private void show(StatusBar statusBar) {
		if (this.widget == null) {
			this.widget = new Widget();
			statusBar.addWidget(this.widget, this.project);
		}
	}

	private void hide(final StatusBar statusBar) {
		if (this.widget != null) {
			statusBar.removeWidget(this.widget.ID());
			this.widget = null;
		}
	}

	/**
	 * The {@link StatusBarWidget} component for the status.
	 */
	private static class Widget implements StatusBarWidget, StatusBarWidget.IconPresentation {

		public static final Icon ICON = IconLoader.getIcon("/spring-javaformat/formatOn.png");

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
