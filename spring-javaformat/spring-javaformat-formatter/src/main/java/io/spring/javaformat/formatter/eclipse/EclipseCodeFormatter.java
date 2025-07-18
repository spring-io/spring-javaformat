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

package io.spring.javaformat.formatter.eclipse;

import java.util.Map;

import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.TextEdit;

/**
 * Internal interface used to access an Eclipse {@code CodeFormatter} implementation.
 *
 * @author Phillip Webb
 */
public interface EclipseCodeFormatter {

	TextEdit format(int kind, String source, int offset, int length, int indentationLevel, String lineSeparator);

	TextEdit format(int kind, String source, IRegion[] regions, int indentationLevel, String lineSeparator);

	String createIndentationString(int indentationLevel);

	void setOptions(Map<String, String> options);

}
