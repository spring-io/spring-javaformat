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

package io.spring.format.gradle;

import io.spring.javaformat.formatter.FileEdit;
import io.spring.javaformat.formatter.FileFormatter;
import io.spring.javaformat.formatter.FileFormatterException;
import org.gradle.api.GradleException;

/**
 * {@link FormatterTask} to apply formatting.
 *
 * @author Phillip Webb
 */
public class ApplyTask extends FormatterTask {

	static final String NAME = "springFormatApply";

	static final String DESCRIPTION = "Formats Java source code using Spring conventions";

	@Override
	public void run() throws Exception {
		try {
			FileFormatter formatter = new FileFormatter();
			formatter.formatFiles(this.files, this.encoding).filter(FileEdit::hasEdits)
					.forEach(FileEdit::save);
		}
		catch (FileFormatterException ex) {
			throw new GradleException("Unable to format file " + ex.getFile(), ex);
		}
	}

}
