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

package io.spring.javaformat.formatter;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.spring.javaformat.config.JavaFormatConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Formatter}.
 */
public class FormatterTests extends AbstractFormatterTests {

	@ParameterizedTest
	@MethodSource("items")
	public void format(Item item) throws Exception {
		String sourceContent = read(item.getSource());
		String expectedContent = read(item.getExpected());
		String formattedContent = format(item.getConfig(), sourceContent);
		if (!expectedContent.equals(formattedContent)) {
			System.out.println("Formatted " + item.getSource() + " does not match " + item.getExpected());
			print("Source " + item.getSource(), sourceContent);
			print("Expected +" + item.getExpected(), expectedContent);
			print("Got", formattedContent);
			System.out.println("========================================");
			assertThat(expectedContent).isEqualTo(formattedContent)
					.describedAs("Formatted content does not match for " + item.getSource());
		}
	}

	private String format(JavaFormatConfig config, String sourceContent) throws Exception {
		IDocument document = new Document(sourceContent);
		TextEdit textEdit = new Formatter(config).format(sourceContent);
		textEdit.apply(document);
		return document.get();
	}

	public static Item[] items() {
		return items("FormatterTests-expected");
	}

}
