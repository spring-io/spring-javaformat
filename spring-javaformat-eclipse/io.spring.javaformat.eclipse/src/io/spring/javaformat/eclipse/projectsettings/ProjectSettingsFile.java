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

package io.spring.javaformat.eclipse.projectsettings;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;

import io.spring.javaformat.config.JavaFormatConfig;

/**
 * A project settings file that can be copied to the project {@code .settings} folder.
 *
 * @author Phillip Webb
 */
final class ProjectSettingsFile {

	private final String name;

	private final ContentSupplier contentSupplier;

	ProjectSettingsFile(String name, ContentSupplier contentSupplier) {
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
	 * @param javaFormatConfig the java format config to apply
	 * @return the file contents
	 * @throws IOException if the file cannot be opened
	 */
	public InputStream getContent(JavaFormatConfig javaFormatConfig) throws IOException {
		return this.contentSupplier.getContent(javaFormatConfig);
	}

	/**
	 * Return a new {@link ProjectSettingsFile} where the original content is updated by
	 * the given operation.
	 * @param operation the operation to update the content
	 * @return a new {@link ProjectSettingsFile} instance
	 */
	public ProjectSettingsFile withUpdatedContent(BiFunction<JavaFormatConfig, String, String> operation) {
		return new ProjectSettingsFile(this.name, (javaFormatConfig) -> {
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(this.contentSupplier.getContent(javaFormatConfig)))) {
				StringWriter writer = new StringWriter();
				char[] buffer = new char[4096];
				int read = 0;
				while ((read = reader.read(buffer)) >= 0) {
					writer.write(buffer, 0, read);
				}
				String content = operation.apply(javaFormatConfig, writer.toString());
				return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
			}
		});
	}

	/**
	 * Create a new {@link ProjectSettingsFile} instance from the given {@code File}.
	 * @param file the source file
	 * @return a new {@link ProjectSettingsFile}
	 */
	public static ProjectSettingsFile fromFile(File file) {
		return new ProjectSettingsFile(file.getName(), (javaFormatConfig) -> new FileInputStream(file));
	}

	/**
	 * Create a new {@link ProjectSettingsFile} instance from a classpath resource.
	 * @param sourceClass the source class used to load the resource
	 * @param name the name of the resource to load
	 * @return a new {@link ProjectSettingsFile}
	 */
	public static ProjectSettingsFile fromClasspath(Class<?> sourceClass, String name) {
		return new ProjectSettingsFile(name, (javaFormatConfig) -> sourceClass.getResourceAsStream(name));
	}

	@FunctionalInterface
	interface ContentSupplier {

		InputStream getContent(JavaFormatConfig javaFormatConfig) throws IOException;

	}

}
