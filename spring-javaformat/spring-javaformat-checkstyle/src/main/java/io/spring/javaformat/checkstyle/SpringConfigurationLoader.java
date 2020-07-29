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

package io.spring.javaformat.checkstyle;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader.IgnoredModulesOptions;
import com.puppycrawl.tools.checkstyle.PropertyResolver;
import com.puppycrawl.tools.checkstyle.api.AutomaticBean;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.api.Context;
import com.puppycrawl.tools.checkstyle.api.FileSetCheck;
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
		Configuration config = loadConfiguration(getClass().getResourceAsStream("spring-checkstyle.xml"),
				propertyResolver);
		return Arrays.stream(config.getChildren()).filter(this.moduleFactory::nonFiltered).map(this::load)
				.collect(Collectors.toList());
	}

	private Configuration loadConfiguration(InputStream inputStream, PropertyResolver propertyResolver) {
		try {
			InputSource inputSource = new InputSource(inputStream);
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
			if (module instanceof AutomaticBean) {
				initialize(configuration, (AutomaticBean) module);
			}
			return module;
		}
		catch (CheckstyleException ex) {
			throw new IllegalStateException("cannot initialize module " + name + " - " + ex.getMessage(), ex);
		}
	}

	private void initialize(Configuration configuration, AutomaticBean bean) throws CheckstyleException {
		bean.contextualize(this.context);
		bean.configure(configuration);
	}

}
