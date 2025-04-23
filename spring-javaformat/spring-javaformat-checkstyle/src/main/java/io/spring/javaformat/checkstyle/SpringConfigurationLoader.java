/*
 * Copyright 2017-2025 the original author or authors.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader.IgnoredModulesOptions;
import com.puppycrawl.tools.checkstyle.PropertyResolver;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configurable;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.api.Context;
import com.puppycrawl.tools.checkstyle.api.Contextualizable;
import com.puppycrawl.tools.checkstyle.api.FileSetCheck;
import com.puppycrawl.tools.checkstyle.api.Scope;
import com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocVariableCheck;
import org.xml.sax.InputSource;

/**
 * Loads {@link FileSetCheck FileSetChecks} from the {@code spring-checkstyle.xml}
 * configuration.
 *
 * @author Phillip Webb
 */
class SpringConfigurationLoader {

	private final Context context;

	private final FilteredModuleFactory moduleFactory;

	SpringConfigurationLoader(Context context, FilteredModuleFactory moduleFactory) {
		this.context = context;
		this.moduleFactory = moduleFactory;
	}

	public Collection<FileSetCheck> load(PropertyResolver propertyResolver) {
		Configuration config = loadConfiguration(loadConfigurationSource(), propertyResolver);
		return Arrays.stream(config.getChildren())
			.filter(this.moduleFactory::nonFiltered)
			.map(this::load)
			.collect(Collectors.toList());
	}

	private String loadConfigurationSource() {
		try (InputStream stream = getClass().getResourceAsStream("spring-checkstyle.xml")) {
			StringBuilder builder = new StringBuilder();
			byte[] buffer = new byte[4096];
			int read;
			while ((read = stream.read(buffer)) > 0) {
				builder.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
			}
			return preprocessConfigurationSource(builder.toString());
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private String preprocessConfigurationSource(String source) {
		return source.replace("{{javadocVariableCheckScopeProperty}}", javadocVariableCheckScopeProperty());
	}

	private String javadocVariableCheckScopeProperty() {
		try {
			JavadocVariableCheck.class.getMethod("setScope", Scope.class);
			return "scope";
		}
		catch (NoSuchMethodException ex) {
			return "accessModifiers";
		}
	}

	private Configuration loadConfiguration(String source, PropertyResolver propertyResolver) {
		try {
			InputSource inputSource = new InputSource(new StringReader(source));
			return ConfigurationLoader.loadConfiguration(inputSource, propertyResolver, IgnoredModulesOptions.EXECUTE);
		}
		catch (CheckstyleException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private FileSetCheck load(Configuration configuration) {
		Object module = createModule(configuration);
		if (!(module instanceof FileSetCheck)) {
			throw new IllegalStateException(configuration.getName() + " is not allowed");
		}
		return (FileSetCheck) module;
	}

	private Object createModule(Configuration configuration) {
		String name = configuration.getName();
		try {
			Object module = this.moduleFactory.createModule(name);
			initialize(configuration, module);
			return module;
		}
		catch (CheckstyleException ex) {
			throw new IllegalStateException("cannot initialize module " + name + " - " + ex.getMessage(), ex);
		}
	}

	private void initialize(Configuration configuration, Object module) throws CheckstyleException {
		if (module instanceof Contextualizable) {
			((Contextualizable) module).contextualize(this.context);
		}
		if (module instanceof Configurable) {
			((Configurable) module).configure(configuration);
		}
	}

}
