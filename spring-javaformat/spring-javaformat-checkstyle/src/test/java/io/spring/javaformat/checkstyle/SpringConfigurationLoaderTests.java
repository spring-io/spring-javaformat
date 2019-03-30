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

package io.spring.javaformat.checkstyle;

import java.util.Collection;
import java.util.Properties;

import com.puppycrawl.tools.checkstyle.DefaultContext;
import com.puppycrawl.tools.checkstyle.ModuleFactory;
import com.puppycrawl.tools.checkstyle.PackageObjectFactory;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.PropertyResolver;
import com.puppycrawl.tools.checkstyle.api.FileSetCheck;
import io.spring.javaformat.checkstyle.check.SpringHeaderCheck;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SpringConfigurationLoader}.
 *
 * @author Phillip Webb
 */
public class SpringConfigurationLoaderTests {

	@Test
	public void loadShouldLoadChecks() {
		DefaultContext context = new DefaultContext();
		context.add("moduleFactory", new PackageObjectFactory(
				getClass().getPackage().getName(), getClass().getClassLoader()));
		ModuleFactory moduleFactory = new PackageObjectFactory(
				getClass().getPackage().getName(), getClass().getClassLoader());
		Collection<FileSetCheck> checks = new SpringConfigurationLoader(context,
				moduleFactory).load(getPropertyResolver());
		assertThat(checks).hasSize(3);
	}

	private PropertyResolver getPropertyResolver() {
		Properties properties = new Properties();
		properties.put("headerType", SpringHeaderCheck.DEFAULT_HEADER_TYPE);
		properties.put("headerFile", "");
		properties.put("headerCopyrightPattern",
				SpringHeaderCheck.DEFAULT_HEADER_COPYRIGHT_PATTERN);
		return new PropertiesExpander(properties);
	}

}
