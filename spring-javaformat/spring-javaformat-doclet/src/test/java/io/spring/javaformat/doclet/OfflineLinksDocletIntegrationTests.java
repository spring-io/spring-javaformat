/*
 * Copyright 2017-present the original author or authors.
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

package io.spring.javaformat.doclet;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.DocumentationTool;
import javax.tools.DocumentationTool.DocumentationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link OfflineLinksDoclet}.
 *
 * @author Phillip Webb
 */
class OfflineLinksDocletIntegrationTests {

	@TempDir
	private Path temp;

	@Test
	void whenHasImportLinkWithNoOfflineLinkPrintsWarning() {
		List<Diagnostic<?>> diagnostics = javadoc("io.spring.javaformat.doclet.example.importlink");
		assertThat(diagnostics.stream().map(Object::toString)).anySatisfy((message) -> assertThat(message)
			.contains("warning: No external link found for 'org.junit.jupiter.api.Test'"));
	}

	@Test
	void whenHasQualifiedLinkWithNoOfflineLinkPrintsWarning() {
		List<Diagnostic<?>> diagnostics = javadoc("io.spring.javaformat.doclet.example.qualifiedlink");
		assertThat(diagnostics.stream().map(Object::toString)).anySatisfy((message) -> assertThat(message)
			.contains("warning: No external link found for 'org.junit.jupiter.api.Test'"));
	}

	@Test
	void whenHasImportLinkWithOfflineLinkElementListPrintsNoWarnings() {
		List<Diagnostic<?>> diagnostics = javadoc("io.spring.javaformat.doclet.example.importlink", "-linkoffline",
				"https://example.com", "./src/test/resources/elementlist");
		assertThat(diagnostics).isEmpty();
	}

	@Test
	void whenHasImportLinkWithOfflineLinkPackageListPrintsNoWarnings() {
		List<Diagnostic<?>> diagnostics = javadoc("io.spring.javaformat.doclet.example.importlink", "-linkoffline",
				"https://example.com", "./src/test/resources/packagelist");
		assertThat(diagnostics).isEmpty();
	}

	@Test
	void whenHasQualifiedLinkWithOfflineLinkElementListPrintsNoWarnings() {
		List<Diagnostic<?>> diagnostics = javadoc("io.spring.javaformat.doclet.example.qualifiedlink", "-linkoffline",
				"https://example.com", "./src/test/resources/elementlist");
		assertThat(diagnostics).isEmpty();
	}

	@Test
	void whenHasLinkWithNoOfflineLinkAndLinkCheckExcludePrintsNoWarnings() {
		List<Diagnostic<?>> diagnostics = javadoc("io.spring.javaformat.doclet.example.importlink",
				"-offlinelinks-ignore-packages", "org.junit.jupiter.api");
		assertThat(diagnostics).isEmpty();
	}

	@Test
	void whenHasNonCheckedLinksPrintsNoWarnings() {
		List<Diagnostic<?>> diagnostics = javadoc("io.spring.javaformat.doclet.example.nonchecked",
				"-offlinelinks-ignore-packages", "org.junit.jupiter.api");
		assertThat(diagnostics).isEmpty();
	}

	@Test
	void whenHasMulitplePackagesAndMultipleSourceLocationsAtSameUrl() {
		List<Diagnostic<?>> diagnostics = javadoc("io.spring.javaformat.doclet.example.multisource",
				"-offlinelinks-source", "./src/test/resources/multisource/the@name@jar", "-linkoffline",
				"https://example.com", "first,second");
		assertThat(diagnostics).isEmpty();
		assertThat(this.temp.resolve("io/spring/javaformat/doclet/example/multisource/Example.html"))
			.content(StandardCharsets.UTF_8)
			.contains("https://example.com/org/junit/jupiter/api/Test.html")
			.contains("https://example.com/org/junit/jupiter/api/extension/AfterAllCallback.html");
	}

	@Test
	void whenHasMulitplePackagesAndMultipleSources() {
		List<Diagnostic<?>> diagnostics = javadoc("io.spring.javaformat.doclet.example.multisource",
				"-offlinelinks-source", "./src/test/resources/multisource/the@name@jar", "-linkoffline",
				"https://example.com/a", "first", "-linkoffline", "https://example.com/b", "second");
		assertThat(diagnostics).isEmpty();
		assertThat(this.temp.resolve("io/spring/javaformat/doclet/example/multisource/Example.html"))
			.content(StandardCharsets.UTF_8)
			.contains("https://example.com/a/org/junit/jupiter/api/Test.html")
			.contains("https://example.com/b/org/junit/jupiter/api/extension/AfterAllCallback.html");
	}

	List<Diagnostic<?>> javadoc(String subpackage, String... args) {
		List<Diagnostic<?>> diagnostics = new ArrayList<>();
		DocumentationTool tool = ToolProvider.getSystemDocumentationTool();
		StandardJavaFileManager fileManager = tool.getStandardFileManager(
				(DiagnosticListener<? super JavaFileObject>) diagnostics::add, Locale.getDefault(),
				StandardCharsets.UTF_8);
		StringWriter out = new StringWriter();
		List<String> options = new ArrayList<>();
		options.addAll(Arrays.asList("-d", this.temp.toAbsolutePath().toString(), "-sourcepath", "./src/test/java",
				"-subpackages", subpackage));
		options.addAll(Arrays.asList(args));
		DocumentationTask task = tool.getTask(out, fileManager, diagnostics::add, OfflineLinksDoclet.class, options,
				null);
		task.call();
		return diagnostics;
	}

}
