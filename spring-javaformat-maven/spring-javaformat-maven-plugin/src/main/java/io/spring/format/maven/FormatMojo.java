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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

/**
 * Base class for formatter Mojo.
 *
 * @author Phillip Webb
 */
public abstract class FormatMojo extends AbstractMojo {

	private static final String[] DEFAULT_INCLUDES = new String[] { "**/*.java" };

	/**
	 * The Maven Project Object.
	 */
	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	protected MavenProject project;

	/**
	 * Specifies the location of the source directories to use.
	 */
	@Parameter(defaultValue = "${project.compileSourceRoots}")
	private List<String> sourceDirectories;

	/**
	 * Specifies the location of the test source directories to use.
	 */
	@Parameter(defaultValue = "${project.testCompileSourceRoots}")
	private List<String> testSourceDirectories;

	/**
	 * Specifies the names filter of the source files to be excluded.
	 */
	@Parameter(property = "spring-format.excludes")
	private String[] excludes;

	/**
	 * Specifies the names filter of the source files to be included.
	 */
	@Parameter(property = "spring-format.includes")
	private String[] includes;

	/**
	 * The file encoding to use when reading the source files. If the property
	 * <code>project.build.sourceEncoding</code> is not set, the platform default encoding
	 * is used..
	 */
	@Parameter(property = "encoding", defaultValue = "${project.build.sourceEncoding}")
	private String encoding;

	@Override
	public final void execute() throws MojoExecutionException, MojoFailureException {
		List<File> directories = new ArrayList<>();
		resolve(this.sourceDirectories).forEach(directories::add);
		resolve(this.testSourceDirectories).forEach(directories::add);
		List<File> files = new ArrayList<>();
		for (File directory : directories) {
			files.addAll(scan(directory));
		}
		Charset encoding = (this.encoding == null ? StandardCharsets.UTF_8
				: Charset.forName(this.encoding));
		execute(files, encoding);
	}

	private Stream<File> resolve(List<String> directories) {
		return directories.stream().map(
				directory -> FileUtils.resolveFile(this.project.getBasedir(), directory))
				.filter(File::exists).filter(File::isDirectory);
	}

	private List<File> scan(File directory) {
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setBasedir(directory);
		scanner.setIncludes(hasLength(this.includes) ? this.includes : DEFAULT_INCLUDES);
		scanner.setExcludes(this.excludes);
		scanner.addDefaultExcludes();
		scanner.setCaseSensitive(false);
		scanner.setFollowSymlinks(false);
		scanner.scan();
		return Arrays.asList(scanner.getIncludedFiles()).stream()
				.map(name -> new File(directory, name)).collect(Collectors.toList());
	}

	private boolean hasLength(Object[] array) {
		return array != null && array.length > 0;
	}

	/**
	 * Perform the formatting build-process behavior this {@code Mojo} implements.
	 * @param files the files to process
	 * @param encoding the encoding
	 * @throws MojoExecutionException on execution error
	 * @throws MojoFailureException on failure
	 */
	protected abstract void execute(List<File> files, Charset encoding)
			throws MojoExecutionException, MojoFailureException;

}
