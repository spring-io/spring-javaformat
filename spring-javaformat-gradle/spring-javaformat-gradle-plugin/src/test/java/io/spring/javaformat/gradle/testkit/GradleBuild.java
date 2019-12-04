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

package io.spring.javaformat.gradle.testkit;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.gradle.internal.impldep.com.google.common.base.Charsets;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.xml.sax.InputSource;

import io.spring.javaformat.formatter.Formatter;
import io.spring.javaformat.formatter.eclipse.Preparator;
import io.spring.javaformat.org.eclipse.jdt.core.formatter.CodeFormatter;

/**
 * A {@link TestRule} for running a Gradle build using {@link GradleRunner}.
 *
 * @author Andy Wilkinson
 * @author Phillip Webb
 */
public class GradleBuild implements TestRule {

	private final TemporaryFolder temp = new TemporaryFolder();

	private File source;

	private File projectDir;

	private String gradleVersion;

	@Override
	public Statement apply(Statement base, Description description) {
		return this.temp.apply(new Statement() {

			@Override
			public void evaluate() throws Throwable {
				before();
				try {
					base.evaluate();
				}
				finally {
					after();
				}
			}

		}, description);
	}

	private void before() throws IOException {
		this.projectDir = this.temp.newFolder();
	}

	private void after() {
		GradleBuild.this.source = null;
	}

	private String getPluginClasspath() {
		return absolutePath("build/classes/java/main") + "," + absolutePath("build/resources/main") + ","
				+ pathOfJarContaining(Formatter.class) + "," + pathOfJarContaining(Preparator.class) + ","
				+ pathOfJarContaining(CodeFormatter.class);
	}

	private String absolutePath(String path) {
		return new File(path).getAbsolutePath();
	}

	private String pathOfJarContaining(Class<?> type) {
		return type.getProtectionDomain().getCodeSource().getLocation().getPath();
	}

	public GradleBuild source(String source) {
		return source(new File(source));
	}

	public GradleBuild source(File source) {
		if (source == null || !source.exists() || !source.isDirectory()) {
			throw new IllegalArgumentException("Invalid source " + source);
		}
		this.source = source;
		return this;
	}

	public BuildResult build(String... arguments) {
		try {
			return prepareRunner(arguments).build();
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public BuildResult buildAndFail(String... arguments) {
		try {
			return prepareRunner(arguments).buildAndFail();
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public GradleRunner prepareRunner(String... arguments) throws IOException {
		copyFolder(this.source.getAbsoluteFile().toPath(), this.projectDir.toPath());
		File buildFile = new File(this.projectDir, "build.gradle");
		String scriptContent = new String(Files.readAllBytes(buildFile.toPath())).replace("{version}",
				getSpringFormatVersion());
		Files.write(buildFile.toPath(), scriptContent.getBytes(Charsets.UTF_8));
		GradleRunner gradleRunner = GradleRunner.create().withProjectDir(this.projectDir).withDebug(true);
		if (this.gradleVersion != null) {
			gradleRunner.withGradleVersion(this.gradleVersion);
		}
		List<String> allArguments = new ArrayList<>();
		allArguments.add("-PpluginClasspath=" + getPluginClasspath());
		allArguments.add("-PspringFormatVersion=" + getSpringFormatVersion());
		allArguments.add("--stacktrace");
		allArguments.addAll(Arrays.asList(arguments));
		return gradleRunner.withArguments(allArguments);
	}

	private void copyFolder(Path source, Path target) throws IOException {
		try (Stream<Path> stream = Files.walk(source)) {
			stream.forEach((child) -> {
				try {
					Path relative = source.relativize(child);
					Path destination = target.resolve(relative);
					if (!destination.toFile().isDirectory()) {
						Files.copy(child, destination, StandardCopyOption.REPLACE_EXISTING);
					}
				}
				catch (Exception ex) {
					throw new IllegalStateException(ex);
				}
			});
		}
	}

	public File getProjectDir() {
		return this.projectDir;
	}

	public void setProjectDir(File projectDir) {
		this.projectDir = projectDir;
	}

	public GradleBuild gradleVersion(String version) {
		this.gradleVersion = version;
		return this;
	}

	public String getGradleVersion() {
		return this.gradleVersion;
	}

	private String getSpringFormatVersion() {
		return evaluateExpression(
				"/*[local-name()='project']/*[local-name()='parent']/*[local-name()='version']" + "/text()");
	}

	private String evaluateExpression(String expression) {
		try {
			XPathFactory xPathFactory = XPathFactory.newInstance();
			XPath xpath = xPathFactory.newXPath();
			XPathExpression expr = xpath.compile(expression);
			String version = expr.evaluate(new InputSource(new FileReader("pom.xml")));
			return version;
		}
		catch (Exception ex) {
			throw new IllegalStateException("Failed to evaluate expression", ex);
		}
	}

}
