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

package io.spring.format.gradle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.internal.consumer.DefaultGradleConnector;

/**
 * @author Andy Wilkinson
 */
public class ProjectCreator {

	private static final String VERSION = getVersion();

	private String gradleVersion;

	public ProjectCreator() {
		this("2.9");
	}

	public ProjectCreator(String gradleVersion) {
		this.gradleVersion = gradleVersion;
	}

	public void run(String name, String... tasks) throws IOException {
		createBuild(name, tasks).run();
	}

	public BuildLauncher createBuild(String name, String... tasks) throws IOException {
		return createProject(name).newBuild().forTasks(tasks)
				.withArguments("-PformatVersion=" + VERSION, "--stacktrace");
	}

	public ProjectConnection createProject(String name) throws IOException {
		Path source = Paths.get("src/test/resources", name);
		Path destination = Paths.get("target", name);
		deleteRecursively(destination);
		copyRecursively(source, destination);
		GradleConnector gradleConnector = GradleConnector.newConnector();
		gradleConnector.useGradleVersion(this.gradleVersion);
		((DefaultGradleConnector) gradleConnector).embedded(true);
		return gradleConnector.forProjectDirectory(destination.toFile()).connect();
	}

	private void deleteRecursively(Path destination) throws IOException {
		if (Files.exists(destination)) {
			for (Path file : Files.walk(destination).sorted(Comparator.reverseOrder())
					.collect(Collectors.toList())) {
				Files.delete(file);
			}
		}
	}

	private void copyRecursively(Path source, Path destination) throws IOException {
		List<Path> sources = Files.walk(source).collect(Collectors.toList());
		List<Path> destinations = sources.stream().map(source::relativize)
				.map(destination::resolve).collect(Collectors.toList());
		for (int i = 0; i < sources.size(); i++) {
			Files.copy(sources.get(i), destinations.get(i),
					StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private static String getVersion() {
		try {
			Pattern pattern = Pattern.compile("<version>(.*)</version>");
			List<String> lines = Files.lines(Paths.get("pom.xml"))
					.collect(Collectors.toList());
			for (String line : lines) {
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					return matcher.group(1);
				}
			}
			throw new IllegalStateException("No version");
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

}
