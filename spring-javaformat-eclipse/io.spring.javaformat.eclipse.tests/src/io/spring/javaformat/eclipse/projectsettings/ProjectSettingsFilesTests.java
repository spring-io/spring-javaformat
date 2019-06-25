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

package io.spring.javaformat.eclipse.projectsettings;

import java.io.InputStream;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link ProjectSettingsFiles}.
 *
 * @author Phillip Webb
 */
public class ProjectSettingsFilesTests {

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	@Test
	public void iteratorIteratesFiles() throws Exception {
		ProjectSettingsFile file = ProjectSettingsFile.fromFile(this.temp.newFile());
		ProjectSettingsFiles files = new ProjectSettingsFiles(Collections.singleton(file), new ProjectProperties());
		assertThat(files).containsOnly(file);
	}

	@Test
	public void applyToProjectCopiesToDotSettings() throws Exception {
		ProjectSettingsFile file = ProjectSettingsFile.fromFile(this.temp.newFile("foo.prefs"));
		ProjectSettingsFiles files = new ProjectSettingsFiles(Collections.singleton(file), new ProjectProperties());
		IProject project = mock(IProject.class);
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		IFile projectFile = mock(IFile.class);
		given(project.getFile(".settings/foo.prefs")).willReturn(projectFile);
		given(projectFile.exists()).willReturn(true);
		files.applyToProject(project, monitor);
		verify(projectFile).setContents((InputStream) any(), eq(1), eq(monitor));
	}

}
