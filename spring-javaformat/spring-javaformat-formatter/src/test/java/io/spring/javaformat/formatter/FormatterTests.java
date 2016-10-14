/*
 * Copyright 2012-2015 the original author or authors.
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

package io.spring.javaformat.formatter;

import java.io.File;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Formatter}.
 */
public class FormatterTests extends AbstractFormatterTests {

	public FormatterTests(File source, File expected) {
		super(source, expected);
	}

	@Test
	public void format() throws Exception {
		String sourceContent = read(getSource());
		String expectedContent = read(getExpected());
		String formattedContent = format(sourceContent);
		if (!expectedContent.equals(formattedContent)) {
			System.out.println(
					"Formatted " + getSource() + " does not match " + getExpected());
			print("Source " + getSource(), sourceContent);
			print("Expected +" + getExpected(), expectedContent);
			print("Got", formattedContent);
			System.out.println("========================================");
			assertThat(expectedContent).isEqualTo(formattedContent)
					.describedAs("Formatted content does not match for " + getSource());
		}
	}

	private String format(String sourceContent) throws Exception {
		IDocument document = new Document(sourceContent);
		TextEdit textEdit = new Formatter().format(sourceContent);
		textEdit.apply(document);
		return document.get();
	}

}
