/*
 * Copyright 2017-2021 the original author or authors.
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

package io.spring.format.maven;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import io.spring.javaformat.formatter.EditorConfigManager;
import io.spring.javaformat.formatter.FileEdit;
import io.spring.javaformat.formatter.FileFormatter;
import io.spring.javaformat.formatter.FileFormatterException;

/**
 * Applies source formatting to the codebase.
 *
 * @author Phillip Webb
 * @author Tadaya Tsuyukubo
 */
@Mojo(name = "apply", defaultPhase = LifecyclePhase.PROCESS_SOURCES, threadSafe = true)
public class ApplyMojo extends FormatMojo {

	@Override
	protected void execute(List<File> files, Charset encoding, String lineSeparator)
			throws MojoExecutionException, MojoFailureException {

		// group by suffix
		Map<String, List<File>> filesByType = files.stream().collect(
				Collectors.groupingBy(file -> {
					String filename = file.getName();
					int lastIndex = filename.lastIndexOf(".");
					if (lastIndex < 0) {
						return "";
					}
					return filename.substring(lastIndex + 1);
				}));

		String projDir = this.project.getBasedir().getPath();

		EditorConfigManager editorConfigManager = new EditorConfigManager();
		try {
			for (List<File> filesToFormat : filesByType.values()) {
				File firstFile = filesToFormat.get(0);
				Map<String, String> options = editorConfigManager.getProperties(firstFile, projDir);

				FileFormatter formatter = new FileFormatter();
				for (Entry<String, String> entry : options.entrySet()) {
					formatter.addOrReplaceOption(entry.getKey(), entry.getValue());
				}
				formatter.formatFiles(files, encoding, lineSeparator).filter(FileEdit::hasEdits).forEach(this::save);
			}
		}
		catch (FileFormatterException ex) {
			throw new MojoExecutionException("Unable to format file " + ex.getFile(), ex);
		}

	}

	private void save(FileEdit edit) {
		getLog().debug("Formatting file " + edit.getFile());
		edit.save();
	}

}
