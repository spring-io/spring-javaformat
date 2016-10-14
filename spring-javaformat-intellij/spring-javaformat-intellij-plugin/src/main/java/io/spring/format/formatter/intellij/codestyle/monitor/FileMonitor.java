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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.openapi.vfs.VirtualFilePropertyEvent;
import io.spring.format.formatter.intellij.codestyle.monitor.Trigger.State;

/**
 * {@link Monitor} that looks for a {@literal .springformat} file.
 *
 * @author Phillip Webb
 */
public class FileMonitor extends Monitor {

	private static final String TRIGGER_FILE = ".springformat";

	private final VirtualFileManager fileManager;

	private final VirtualFileListener listener;

	private State state;

	public FileMonitor(Project project, Trigger trigger, VirtualFileManager fileManager) {
		super(project, trigger);
		this.fileManager = fileManager;
		this.listener = new Listener();
		fileManager.addVirtualFileListener(this.listener);
		check();
	}

	@Override
	public void stop() {
		this.fileManager.removeVirtualFileListener(this.listener);
	}

	private void check() {
		VirtualFile baseDir = getProject().getBaseDir();
		VirtualFile triggerFile = (baseDir == null ? null
				: baseDir.findChild(TRIGGER_FILE));
		State currentState = (triggerFile == null ? State.NOT_ACTIVE : State.ACTIVE);
		if (!currentState.equals(this.state)) {
			getTrigger().updateState(currentState);
			this.state = currentState;
		}
	}

	public static Factory factory() {
		return (project, trigger) -> new FileMonitor(project, trigger,
				VirtualFileManager.getInstance());
	}

	/**
	 * Lister used to check for trigger file updates.
	 */
	private class Listener extends VirtualFileAdapter {

		@Override
		public void fileCreated(VirtualFileEvent event) {
			check();
		}

		@Override
		public void fileDeleted(VirtualFileEvent event) {
			check();
		}

		@Override
		public void fileMoved(VirtualFileMoveEvent event) {
			check();
		}

		@Override
		public void propertyChanged(VirtualFilePropertyEvent event) {
			check();
		}

	}

}
