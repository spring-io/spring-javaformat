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

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultContext;
import com.puppycrawl.tools.checkstyle.ModuleFactory;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.api.ExternalResourceHolder;
import com.puppycrawl.tools.checkstyle.api.FileSetCheck;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.LocalizedMessage;
import com.puppycrawl.tools.checkstyle.filters.SuppressFilterElement;

import io.spring.javaformat.checkstyle.check.SpringHeaderCheck;
import io.spring.javaformat.checkstyle.check.SpringImportOrderCheck;

/**
 * {@link FileSetCheck} that applies Spring checkstype rules.
 *
 * @author Phillip Webb
 */
public class SpringChecks extends AbstractFileSetCheck implements ExternalResourceHolder {

	private ClassLoader classLoader;

	private ModuleFactory moduleFactory;

	private Collection<FileSetCheck> checks;

	private String headerType = SpringHeaderCheck.DEFAULT_HEADER_TYPE;

	private String headerCopyrightPattern = SpringHeaderCheck.DEFAULT_HEADER_COPYRIGHT_PATTERN;

	private String headerFile;

	private Set<String> avoidStaticImportExcludes = Collections.emptySet();

	private String projectRootPackage = SpringImportOrderCheck.DEFAULT_PROJECT_ROOT_PACKAGE;

	private Set<String> excludes;

	/**
	 * Sets classLoader to load class.
	 * @param classLoader class loader to resolve classes with.
	 */
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * Sets the module factory for creating child modules (Checks).
	 * @param moduleFactory the factory
	 */
	public void setModuleFactory(ModuleFactory moduleFactory) {
		this.moduleFactory = moduleFactory;
	}

	@Override
	public void finishLocalSetup() {
		FilteredModuleFactory moduleFactory = new FilteredModuleFactory(this.moduleFactory, this.excludes);
		DefaultContext context = new DefaultContext();
		context.add("classLoader", this.classLoader);
		context.add("severity", getSeverity());
		context.add("tabWidth", String.valueOf(getTabWidth()));
		context.add("moduleFactory", moduleFactory);
		Properties properties = new Properties();
		put(properties, "headerType", this.headerType);
		put(properties, "headerCopyrightPattern", this.headerCopyrightPattern);
		put(properties, "headerFile", this.headerFile);
		put(properties, "projectRootPackage", this.projectRootPackage);
		put(properties, "avoidStaticImportExcludes",
				this.avoidStaticImportExcludes.stream().collect(Collectors.joining(",")));
		this.checks = new SpringConfigurationLoader(context, moduleFactory).load(new PropertiesExpander(properties));
	}

	private void put(Properties properties, String name, Object value) {
		if (value != null) {
			properties.put(name, value);
		}
	}

	@Override
	public Set<String> getExternalResourceLocations() {
		Set<String> locations = new LinkedHashSet<>();
		for (FileSetCheck check : this.checks) {
			if (check instanceof ExternalResourceHolder) {
				locations.addAll(((ExternalResourceHolder) check).getExternalResourceLocations());
			}
		}
		return locations;
	}

	@Override
	public void beginProcessing(String charset) {
		super.beginProcessing(charset);
		try {
			SuppressFilterElement filter = new SuppressFilterElement("[\\\\/]src[\\\\/]test[\\\\/]java[\\\\/]",
					"Javadoc*", null, null, null, null);
			((Checker) getMessageDispatcher()).addFilter(filter);
		}
		catch (Exception ex) {
			// Ignore and let users configure their own suppressions
		}
	}

	@Override
	protected void processFiltered(File file, FileText fileText) throws CheckstyleException {
		SortedSet<LocalizedMessage> messages = new TreeSet<>();
		for (FileSetCheck check : this.checks) {
			messages.addAll(check.process(file, fileText));
		}
		addMessages(messages);
	}

	@Override
	public void setupChild(Configuration configuration) throws CheckstyleException {
		throw new CheckstyleException("SpringChecks is not allowed as a parent of " + configuration.getName());
	}

	public void setHeaderType(String headerType) {
		this.headerType = headerType;
	}

	public void setHeaderCopyrightPattern(String headerCopyrightPattern) {
		this.headerCopyrightPattern = headerCopyrightPattern;
	}

	public void setHeaderFile(String headerFile) {
		this.headerFile = headerFile;
	}

	public void setAvoidStaticImportExcludes(String[] avoidStaticImportExcludes) {
		this.avoidStaticImportExcludes = new LinkedHashSet<>(Arrays.asList(avoidStaticImportExcludes));
	}

	public void setProjectRootPackage(String projectRootPackage) {
		this.projectRootPackage = projectRootPackage;
	}

	public void setExcludes(String... excludes) {
		this.excludes = new HashSet<>(Arrays.asList(excludes));
	}

}
