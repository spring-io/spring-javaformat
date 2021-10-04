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

package io.spring.javaformat.eclipse.formatter;

import org.eclipse.jdt.core.formatter.CodeFormatter;

import io.spring.javaformat.config.IndentationStyle;
import io.spring.javaformat.config.JavaBaseline;
import io.spring.javaformat.config.JavaFormatConfig;

/**
 * Eclipse {@link CodeFormatter} for Spring formatting with tabs.
 *
 * @author Phillip Webb
 */
public class SpringCodeFormatterJdk11Tabs extends SpringCodeFormatter {

	public SpringCodeFormatterJdk11Tabs() {
		super(JavaFormatConfig.of(JavaBaseline.V11, IndentationStyle.SPACES));
	}

}
