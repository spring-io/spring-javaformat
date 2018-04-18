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

package io.spring.javaformat.eclipse.projectsettings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A project settings file that can be copied to the project {@code .settings} folder.
 *
 * @author Phillip Webb
 */
final class ProjectSettingsFile {

	private final String name;

	private final ContentSupplier contentSupplier;

	private ProjectSettingsFile(String name, ContentSupplier contentSupplier) {
		this.name = name;
		this.contentSupplier = contentSupplier;
	}

	/**
	 * Return the name of the settings file not include any path elements.
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Return a new {@link InputStream} that can be used to access the content of the
	 * file.
	 * @return the file contents
	 * @throws IOException if the file cannot be opened
	 */
	public InputStream getContent() throws IOException {
		return this.contentSupplier.getContent();
	}

	/**
	 * Create a new {@link ProjectSettingsFile} instance from the given {@code File}.
	 * @param file the source file
	 * @return a new {@link ProjectSettingsFile}
	 */
	public static ProjectSettingsFile fromFile(File file) {
		return new ProjectSettingsFile(file.getName(), () -> new FileInputStream(file));
	}

	/**
	 * Create a new {@link ProjectSettingsFile} instance from a classpath resource.
	 * @param sourceClass the source class used to load the resource
	 * @param name the name of the resource to load
	 * @return a new {@link ProjectSettingsFile}
	 */
	public static ProjectSettingsFile fromClasspath(Class<?> sourceClass,
			String name) {
		return new ProjectSettingsFile(name, () -> sourceClass.getResourceAsStream(name));
	}

	@FunctionalInterface
	private interface ContentSupplier {

		InputStream getContent() throws IOException;

	}

}
