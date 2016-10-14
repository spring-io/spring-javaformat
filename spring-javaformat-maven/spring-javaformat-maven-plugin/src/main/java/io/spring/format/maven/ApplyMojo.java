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

package io.spring.format.maven;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import io.spring.javaformat.formatter.FileEdit;
import io.spring.javaformat.formatter.FileFormatter;
import io.spring.javaformat.formatter.FileFormatterException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * FIXME.
 *
 * @author Phillip Webb
 */
@Mojo(name = "apply", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class ApplyMojo extends FormatMojo {

	@Override
	protected void execute(List<File> files, Charset encoding)
			throws MojoExecutionException, MojoFailureException {
		try {
			FileFormatter formatter = new FileFormatter();
			formatter.formatFiles(files, encoding).filter(FileEdit::hasEdits)
					.forEach(this::save);
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
