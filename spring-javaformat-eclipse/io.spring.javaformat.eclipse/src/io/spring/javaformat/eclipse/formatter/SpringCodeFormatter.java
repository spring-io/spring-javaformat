/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.javaformat.eclipse.formatter;

import io.spring.javaformat.formatter.Formatter;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.TextEdit;

/**
 * Eclipse {@link CodeFormatter} for Spring formatting.
 *
 * @author Phillip Webb
 */
public class SpringCodeFormatter extends CodeFormatter {

	private Formatter delegate = new Formatter();

	@Override
	public TextEdit format(int kind, String source, int offset, int length,
			int indentationLevel, String lineSeparator) {
		return this.delegate.format(kind, source, offset, length, indentationLevel,
				lineSeparator);
	}

	@Override
	public TextEdit format(int kind, String source, IRegion[] regions,
			int indentationLevel, String lineSeparator) {
		return this.delegate.format(kind, source, regions, indentationLevel,
				lineSeparator);
	}

}
