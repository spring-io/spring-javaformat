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

import java.util.Set;

import com.puppycrawl.tools.checkstyle.ModuleFactory;
import com.puppycrawl.tools.checkstyle.TreeWalkerAuditEvent;
import com.puppycrawl.tools.checkstyle.TreeWalkerFilter;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;

class FilteredModuleFactory implements ModuleFactory {

	static final TreeWalkerFilter FILTERED = new TreeWalkerFilter() {

		@Override
		public boolean accept(TreeWalkerAuditEvent treeWalkerAuditEvent) {
			return true;
		}

	};

	private final ModuleFactory moduleFactory;

	private final Set<String> excludes;

	FilteredModuleFactory(ModuleFactory moduleFactory, Set<String> excludes) {
		this.moduleFactory = moduleFactory;
		this.excludes = excludes;
	}

	@Override
	public Object createModule(String name) throws CheckstyleException {
		Object module = this.moduleFactory.createModule(name);
		if (isFiltered(module)) {
			if (module instanceof AbstractCheck) {
				return FILTERED;
			}
			throw new IllegalStateException("Unable to filter module " + module.getClass().getName());
		}
		return module;
	}

	boolean nonFiltered(Configuration configuration) {
		return !isFiltered(configuration.getName());
	}

	private boolean isFiltered(Object module) {
		return isFiltered(module.getClass().getName());
	}

	private boolean isFiltered(String name) {
		return this.excludes != null && this.excludes.contains(name);
	}

}
