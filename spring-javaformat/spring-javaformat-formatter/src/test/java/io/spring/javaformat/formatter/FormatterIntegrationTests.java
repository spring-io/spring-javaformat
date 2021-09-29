/*
 * Copyright 2017-2020 the original author or authors.
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

package io.spring.javaformat.formatter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests to ensure the formatter can run in different JVM versions.
 *
 * @author Phillip Webb
 */
@Testcontainers(disabledWithoutDocker = true)
public class FormatterIntegrationTests {

	@ParameterizedTest
	@ValueSource(strings = { "8", "11", "17" })
	void formatCode(String version) throws Exception {
		try (JavaContainer container = new JavaContainer(version)) {
			ToStringConsumer output = new ToStringConsumer();
			container.withStartupCheckStrategy(new OneShotStartupCheckStrategy().withTimeout(Duration.ofMinutes(5)));
			container.withLogConsumer(output);
			String classpath = withCopyClasspathToContainer(container);
			String applicationClassName = FormatterApp.class.getName();
			container.withCommand("java -cp " + classpath + " " + applicationClassName);
			container.start();
			assertThat(output.toUtf8String()).isEqualTo("public class Test {\n\n}\n");
		}
	}

	private String withCopyClasspathToContainer(JavaContainer container) throws IOException {
		List<String> classpath = new ArrayList<>();
		for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
			if (entry.contains("spring-javaformat")) {
				classpath.add(withCopyClasspathEntryToContainer(container, entry));
			}
		}
		return classpath.stream().collect(Collectors.joining(":"));
	}

	private String withCopyClasspathEntryToContainer(JavaContainer container, String entry) throws IOException {
		if (entry.endsWith(".jar")) {
			return withCopyClasspathJarToContainer(container, new File(entry));
		}
		return withCopyClasspathFolderToContainer(container, new File(entry));
	}

	private String withCopyClasspathJarToContainer(JavaContainer container, File jarFile) {
		container.withCopyFileToContainer(MountableFile.forHostPath(jarFile.toPath()), "/app/" + jarFile.getName());
		return "/app/" + jarFile.getName();
	}

	private String withCopyClasspathFolderToContainer(JavaContainer container, File classesFolder) throws IOException {
		String name = classesFolder.getName();
		Path source = classesFolder.toPath();
		try (Stream<Path> stream = Files.walk(source)) {
			stream.forEach((child) -> {
				try {
					Path relative = source.relativize(child);
					if (!child.toFile().isDirectory()) {
						String containerPath = "/app/" + name + "/" + relative;
						container.withCopyFileToContainer(MountableFile.forHostPath(child), containerPath);
					}
				}
				catch (Exception ex) {
					throw new IllegalStateException(ex);
				}
			});
		}
		return "/app/" + name;
	}

}
