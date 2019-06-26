/*
 * Copyright 2017-2019 the original author or authors.
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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.event.Event;
import org.eclipse.buildship.core.internal.event.EventListener;
import org.eclipse.buildship.core.internal.util.collections.AdapterFunction;
import org.eclipse.buildship.core.internal.workspace.GradleNatureAddedEvent;
import org.eclipse.buildship.core.internal.workspace.ProjectCreatedEvent;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;

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

		private ThreadLocal<ExecutionEvent> event = new ThreadLocal<ExecutionEvent>();

		@Override
		public void preExecute(String commandId, ExecutionEvent event) {
			this.event.set(event);
		}

		@Override
		public void postExecuteSuccess(String commandId, Object returnValue) {
			Set<IProject> projects = getProjects(this.event.get());
			this.event.set(null);
			new RefreshProjectsSettingsJob(projects).schedule();
		}

		private Set<IProject> getProjects(ExecutionEvent event) {
			if (event == null) {
				return null;
			}
			ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
			if (currentSelection instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) currentSelection;
				return collectGradleProjects(selection.toList());
			}
			IEditorInput editorInput = HandlerUtil.getActiveEditorInput(event);
			if (editorInput instanceof FileEditorInput) {
				IFile file = ((FileEditorInput) editorInput).getFile();
				return collectGradleProjects(Collections.singleton(file));
			}
			return null;
		}

		private Set<IProject> collectGradleProjects(Collection<?> candidates) {
			Set<IProject> projects = new LinkedHashSet<>(candidates.size());
			AdapterFunction<IResource> adapter = AdapterFunction.forType(IResource.class);
			for (Object candidate : candidates) {
				IResource resource = adapter.apply(candidate);
				if (resource != null) {
					projects.add(resource.getProject());
				}
			}
			return projects;
		}

		@Override
		public void postExecuteFailure(String commandId, ExecutionException exception) {
		}

		@Override
		public void notHandled(String commandId, NotHandledException exception) {
		}

		static void attach() {
			if (PlatformUI.isWorkbenchRunning()) {
				ICommandService commandService = PlatformUI.getWorkbench().getAdapter(ICommandService.class);
				Command command = commandService.getCommand(COMMAND_NAME);
				if (command != null) {
					command.addExecutionListener(new CommandListener());
				}
			}
		}

	}

	/**
	 * Event Listener to trigger an update after a project import or gradle nature change.
	 */
	private static class ProjectListener implements EventListener {

		@Override
		public void onEvent(Event event) {
			if (event instanceof ProjectCreatedEvent || event instanceof GradleNatureAddedEvent) {
				new RefreshProjectsSettingsJob().schedule();
			}
		}

		static void attach() {
			CorePlugin.listenerRegistry().addEventListener(new ProjectListener());
		}

	}

}
