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

package io.spring.javaformat.checkstyle;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;

import com.puppycrawl.tools.checkstyle.AstTreeStringPrinter;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader.IgnoredModulesOptions;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.ModuleFactory;
import com.puppycrawl.tools.checkstyle.PackageObjectFactory;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.ThreadModeSettings;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.api.RootModule;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.xml.sax.InputSource;

/**
 * Tests for {@link SpringChecks}.
 *
 * @author Phillip Webb
 */
public class SpringChecksTests {

	private static final boolean RUNNING_ON_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

	private static final File SOURCES_DIR = new File("src/test/resources/source");

	private static final File CHECKS_DIR = new File("src/test/resources/check");

	private static final File CONFIGS_DIR = new File("src/test/resources/config");

	private static final File DEFAULT_CONFIG = new File(CONFIGS_DIR, "default-checkstyle-configuration.xml");

	@TempDir
	public Path temp;

	@ParameterizedTest
	@MethodSource("paramaters")
	public void processHasExpectedResults(Parameter parameter) throws Exception {
		Locale previousLocale = Locale.getDefault();
		Locale.setDefault(Locale.ENGLISH);
		Configuration configuration = loadConfiguration(parameter);
		RootModule rootModule = createRootModule(configuration);
		try {
			processAndCheckResults(parameter, rootModule);
		}
		finally {
			rootModule.destroy();
			Locale.setDefault(previousLocale);
		}
	}

	private Configuration loadConfiguration(Parameter parameter) throws Exception {
		try (InputStream inputStream = new FileInputStream(parameter.getConfigFile())) {
			Configuration configuration = ConfigurationLoader.loadConfiguration(new InputSource(inputStream),
					new PropertiesExpander(new Properties()), IgnoredModulesOptions.EXECUTE,
					ThreadModeSettings.SINGLE_THREAD_MODE_INSTANCE);
			return configuration;
		}
	}

	private RootModule createRootModule(Configuration configuration) throws CheckstyleException {
		ModuleFactory factory = new PackageObjectFactory(Checker.class.getPackage().getName(),
				getClass().getClassLoader());
		RootModule rootModule = (RootModule) factory.createModule(configuration.getName());
		rootModule.setModuleClassLoader(getClass().getClassLoader());
		rootModule.configure(configuration);
		return rootModule;
	}

	private void processAndCheckResults(Parameter parameter, RootModule rootModule) throws CheckstyleException {
		rootModule.addListener(parameter.getAssertionsListener());
		if (!RUNNING_ON_WINDOWS) {
			printDebugInfo(parameter.getSourceFile());
		}
		rootModule.process(Arrays.asList(parameter.getSourceFile()));
	}

	private void printDebugInfo(File file) throws CheckstyleException {
		try {
			System.out.println(AstTreeStringPrinter.printFileAst(file, JavaParser.Options.WITHOUT_COMMENTS));
		}
		catch (IOException ex) {
		}
	}

	public static Collection<Parameter> paramaters() throws IOException {
		ArrayList<Parameter> parameters = Arrays.stream(SOURCES_DIR.listFiles(SpringChecksTests::sourceFile))
			.sorted()
			.map(Parameter::new)
			.collect(Collectors.toCollection(ArrayList::new));
		parameters.add(new Parameter(new File(SOURCES_DIR, "nopackageinfo/NoPackageInfo.java")));
		parameters.add(new Parameter(new File(SOURCES_DIR, "src/test/java/NamedTest.java")));
		parameters.add(new Parameter(new File(SOURCES_DIR, "src/test/java/NamedTests.java")));
		return parameters;
	}

	private static boolean sourceFile(File file) {
		return file.isFile() && !file.getName().startsWith(".") && !file.getName().equals("package-info.java");
	}

	private static class Parameter {

		private final String name;

		private final File sourceFile;

		private final AssertionsAuditListener assertionsListener;

		private final File configFile;

		Parameter(File sourceFile) {
			String sourceName = sourceFile.getAbsolutePath().substring(SOURCES_DIR.getAbsolutePath().length() + 1);
			this.name = sourceName.replace(".java", "");
			this.sourceFile = new File(SOURCES_DIR, sourceName);
			File configFile = new File(CONFIGS_DIR, this.name + ".xml");
			this.configFile = (configFile.exists() ? configFile : DEFAULT_CONFIG);
			this.assertionsListener = new AssertionsAuditListener(readChecks(this.name + ".txt"));
		}

		private List<String> readChecks(String name) {
			try {
				return Files.readAllLines(new File(CHECKS_DIR, name).toPath());
			}
			catch (IOException ex) {
				throw new IllegalStateException(ex);
			}
		}

		public File getSourceFile() {
			return this.sourceFile;
		}

		public File getConfigFile() {
			return this.configFile;
		}

		public AssertionsAuditListener getAssertionsListener() {
			return this.assertionsListener;
		}

		@Override
		public String toString() {
			return this.name;
		}

	}

}
