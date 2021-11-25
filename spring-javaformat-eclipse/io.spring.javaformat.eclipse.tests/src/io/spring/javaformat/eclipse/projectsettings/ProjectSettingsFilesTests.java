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

package io.spring.javaformat.eclipse.projectsettings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.will;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link ProjectSettingsFiles}.
 *
 * @author Phillip Webb
 */
public class ProjectSettingsFilesTests {

	@TempDir
	public File temp;

	@Test
	void iteratorIteratesFiles() throws Exception {
		ProjectSettingsFile file = ProjectSettingsFile.fromFile(new File(this.temp, "file.prefs"));
		ProjectSettingsFiles files = new ProjectSettingsFiles(Collections.singleton(file), new ProjectProperties());
		assertThat(files).containsOnly(file);
	}

	@Test
	void applyToProjectWithoutFileCopiesToDotSettings() throws Exception {
		ProjectSettingsFile file = createPrefsFile();
		ProjectSettingsFiles files = new ProjectSettingsFiles(Collections.singleton(file), new ProjectProperties());
		IProject project = mock(IProject.class);
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		IFile projectFile = mock(IFile.class);
		given(project.getFile(".settings/foo.prefs")).willReturn(projectFile);
		given(projectFile.exists()).willReturn(false);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		will((invocation) -> {
			invocation.getArgument(0, InputStream.class).transferTo(out);
			return null;
		}).given(projectFile).create(any(), anyBoolean(), any());
		files.applyToProject(project, monitor);
		verify(projectFile).create(any(), eq(true), any());
		assertThat(out.toString(StandardCharsets.UTF_8)).isEqualTo("y=z\n");
	}

	@Test
	void applyToProjectWithFileMergesToDotSettings() throws Exception {
		ProjectSettingsFile file = createPrefsFile();
		ProjectSettingsFiles files = new ProjectSettingsFiles(Collections.singleton(file), new ProjectProperties());
		IProject project = mock(IProject.class);
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		IFile projectFile = mock(IFile.class);
		given(project.getFile(".settings/foo.prefs")).willReturn(projectFile);
		given(projectFile.exists()).willReturn(true);
		given(projectFile.getContents(true))
			.willReturn(new ByteArrayInputStream("a=b\n".getBytes(StandardCharsets.UTF_8)));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		will((invocation) -> {
			invocation.getArgument(0, InputStream.class).transferTo(out);
			return null;
		}).given(projectFile).setContents((InputStream) any(), anyInt(), any());
		files.applyToProject(project, monitor);
		verify(projectFile).setContents((InputStream) any(), eq(1), eq(monitor));
		assertThat(out.toString(StandardCharsets.UTF_8))
				.isEqualToNormalizingNewlines("a=b\ny=z\n");
	}

	private ProjectSettingsFile createPrefsFile() throws IOException {
		File prefsFile = new File(this.temp, "foo.prefs");
		Files.copy(new ByteArrayInputStream("y=z\n".getBytes(StandardCharsets.UTF_8)), prefsFile.toPath());
		ProjectSettingsFile file = ProjectSettingsFile.fromFile(prefsFile);
		return file;
	}

}
