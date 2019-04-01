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

package io.spring.javaformat.checkstyle.check;

import com.puppycrawl.tools.checkstyle.checks.imports.ImportOrderCheck;

/**
 * Checks that the order of imports follow Spring conventions.
 *
 * @author Vedran Pavic
 */
public class SpringImportOrderCheck extends ImportOrderCheck {

	/**
	 * The default root package.
	 */
	public static final String DEFAULT_PROJECT_ROOT_PACKAGE = "org.springframework";

	public SpringImportOrderCheck() {
		setProjectRootPackage(DEFAULT_PROJECT_ROOT_PACKAGE);
		setOrdered(true);
		setSeparated(true);
		setOption("bottom");
		setSortStaticImportsAlphabetically(true);
	}

	public void setProjectRootPackage(String projectRootPackage) {
		setGroups("java", "/^javax?\\./", "*", projectRootPackage);
	}

}
