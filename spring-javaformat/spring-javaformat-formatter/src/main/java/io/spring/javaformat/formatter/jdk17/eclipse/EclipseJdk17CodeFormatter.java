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

package io.spring.javaformat.formatter.jdk17.eclipse;

import java.util.Map;

import io.spring.javaformat.config.JavaFormatConfig;
import io.spring.javaformat.eclipse.jdt.jdk17.internal.formatter.ExtendedCodeFormatter;
import io.spring.javaformat.eclipse.jdt.jdk17.internal.formatter.Preparator;
import io.spring.javaformat.formatter.eclipse.EclipseCodeFormatter;
import io.spring.javaformat.formatter.eclipse.Options;

/**
 * Internal delegate JDK 17 baseline {@link EclipseCodeFormatter} to apply Spring
 * {@literal formatter.prefs} and add {@link Preparator Preparators}.
 *
 * @author Phillip Webb
 */
public class EclipseJdk17CodeFormatter extends ExtendedCodeFormatter implements EclipseCodeFormatter {

	private final Map<String, String> appliedOptions;

	public EclipseJdk17CodeFormatter(JavaFormatConfig javaFormatConfig) {
		this(new Options("io.spring.javaformat.eclipse.jdt.jdk17").load(javaFormatConfig));
	}

	EclipseJdk17CodeFormatter(Map<String, String> options) {
		super(options);
		this.appliedOptions = options;
		addPreparator(new JavadocLineBreakPreparator());
		addPreparator(new CodeLineBreakPreparator());
		addPreparator(new JSpecifyPreparator());
	}

	@Override
	public void setOptions(Map<String, String> options) {
		super.setOptions(this.appliedOptions);
	}

}
