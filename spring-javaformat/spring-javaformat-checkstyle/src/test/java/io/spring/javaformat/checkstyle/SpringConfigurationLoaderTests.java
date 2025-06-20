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

package io.spring.javaformat.checkstyle;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.puppycrawl.tools.checkstyle.DefaultContext;
import com.puppycrawl.tools.checkstyle.PackageObjectFactory;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.PropertyResolver;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.FileSetCheck;
import org.assertj.core.extractor.Extractors;
import org.junit.jupiter.api.Test;

import io.spring.javaformat.checkstyle.check.SpringHeaderCheck;
import io.spring.javaformat.checkstyle.check.SpringImportOrderCheck;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SpringConfigurationLoader}.
 *
 * @author Phillip Webb
 */
public class SpringConfigurationLoaderTests {

	@Test
	public void loadShouldLoadChecks() {
		Collection<FileSetCheck> checks = load(null);
		assertThat(checks).hasSize(5);
		TreeWalker treeWalker = (TreeWalker) checks.toArray()[4];
		Set<?> ordinaryChecks = (Set<?>) Extractors.byName("ordinaryChecks").extract(treeWalker);
		assertThat(ordinaryChecks).hasSize(61);
		Set<?> commentChecks = (Set<?>) Extractors.byName("commentChecks").extract(treeWalker);
		assertThat(commentChecks).hasSize(6);
	}

	@Test
	public void loadWithExcludeShouldExcludeChecks() {
		Set<String> excludes = new HashSet<String>(
				Arrays.asList("com.puppycrawl.tools.checkstyle.checks.whitespace.MethodParamPadCheck",
						"com.puppycrawl.tools.checkstyle.checks.annotation.MissingDeprecatedCheck"));
		Collection<FileSetCheck> checks = load(excludes);
		assertThat(checks).hasSize(5);
		TreeWalker treeWalker = (TreeWalker) checks.toArray()[4];
		Set<?> ordinaryChecks = (Set<?>) Extractors.byName("ordinaryChecks").extract(treeWalker);
		assertThat(ordinaryChecks).hasSize(60);
		Set<?> commentChecks = (Set<?>) Extractors.byName("commentChecks").extract(treeWalker);
		assertThat(commentChecks).hasSize(5);
	}

	@Test
	public void loadWithExcludeHeaderShouldExcludeChecks() {
		Set<String> excludes = Collections.singleton("io.spring.javaformat.checkstyle.check.SpringHeaderCheck");
		Object[] checks = load(excludes).stream().toArray();
		assertThat(checks).hasSize(4);
	}

	private Collection<FileSetCheck> load(Set<String> excludes) {
		DefaultContext context = new DefaultContext();
		FilteredModuleFactory filteredModuleFactory = new FilteredModuleFactory(
				new PackageObjectFactory(getClass().getPackage().getName(), getClass().getClassLoader()), excludes);
		context.add("moduleFactory", filteredModuleFactory);
		Collection<FileSetCheck> checks = new SpringConfigurationLoader(context, filteredModuleFactory)
			.load(getPropertyResolver());
		return checks;
	}

	private PropertyResolver getPropertyResolver() {
		Properties properties = new Properties();
		properties.put("headerType", SpringHeaderCheck.DEFAULT_HEADER_TYPE);
		properties.put("headerFile", "");
		properties.put("headerCopyrightPattern", SpringHeaderCheck.DEFAULT_HEADER_COPYRIGHT_PATTERN);
		properties.put("projectRootPackage", SpringImportOrderCheck.DEFAULT_PROJECT_ROOT_PACKAGE);
		properties.put("avoidStaticImportExcludes", "");
		return new PropertiesExpander(properties);
	}

}
