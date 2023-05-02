/*
 * Copyright 2017-2021 the original author or authors.
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

import com.puppycrawl.tools.checkstyle.checks.imports.IllegalImportCheck;

/**
 * Checks for illegal import packages and classes.
 *
 * @author Sushant Kumar Singh
 *
 */
public class SpringIllegalImportCheck extends IllegalImportCheck {

	/**
	 * Illegal packages.
	 */
	private static final String[] ILLEGAL_PKGS = {"sun", "lombok"};

	/**
	 * Illegal classes.
	 */
	private static final String[] ILLEGAL_CLASSES = {};

	public SpringIllegalImportCheck() {
		setIllegalPkgs(ILLEGAL_PKGS);
		setIllegalClasses(ILLEGAL_CLASSES);
	}
}
