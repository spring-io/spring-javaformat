/*
 * Copyright 2017-2018 the original author or authors.
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

package io.spring.javaformat.eclipse.gradle;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.event.Event;
import org.eclipse.buildship.core.event.EventListener;
import org.eclipse.buildship.core.workspace.GradleNatureAddedEvent;
import org.eclipse.buildship.core.workspace.ProjectCreatedEvent;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

/**
 * Listeners used to trigger the {@link RefreshProjectsSettingsJob}.
 *
 * @author Phillip Webb
 */
@SuppressWarnings("restriction")
public final class RefreshProjectSettingsListeners {

	private RefreshProjectSettingsListeners() {
	}

	/**
	 * Attach listeners to trigger the {@link RefreshProjectsSettingsJob}.
	 */
	public static void attach() {
		try {
			ProjectListener.attach();
			CommandListener.attach();
		}
		catch (NoClassDefFoundError ex) {
		}
	}

	/**
	 * Command listener that triggers an update after the "Refresh Gradle Project".
	 */
	private static class CommandListener implements IExecutionListener {

		private static final String COMMAND_NAME = "org.eclipse.buildship.ui.commands.refreshproject"; //$NON-NLS-1$

		@Override
		public void notHandled(String commandId, NotHandledException exception) {
		}

		@Override
		public void postExecuteFailure(String commandId, ExecutionException exception) {
		}

		@Override
		public void postExecuteSuccess(String commandId, Object returnValue) {
			new RefreshProjectsSettingsJob().schedule();
		}

		@Override
		public void preExecute(String commandId, ExecutionEvent event) {
		}

		static void attach() {
			if (PlatformUI.isWorkbenchRunning()) {
				ICommandService commandService = PlatformUI.getWorkbench()
						.getAdapter(ICommandService.class);
				Command command = commandService.getCommand(COMMAND_NAME);
				if (command != null) {
					command.addExecutionListener(new CommandListener());
				}
			}
		}

	}

	/**
	 * Event Listener to triger an update after a project import or gradle nature change.
	 */
	private static class ProjectListener implements EventListener {

		@Override
		public void onEvent(Event event) {
			if (event instanceof ProjectCreatedEvent
					|| event instanceof GradleNatureAddedEvent) {
				new RefreshProjectsSettingsJob().schedule();
			}
		}

		static void attach() {
			CorePlugin.listenerRegistry().addEventListener(new ProjectListener());
		}

	}

}
