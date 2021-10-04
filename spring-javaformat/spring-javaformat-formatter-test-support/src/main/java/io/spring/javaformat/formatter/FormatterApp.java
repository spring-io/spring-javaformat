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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

import io.spring.javaformat.config.IndentationStyle;
import io.spring.javaformat.config.JavaBaseline;
import io.spring.javaformat.config.JavaFormatConfig;

/**
 * Test app used to format something in a test container.
 *
 * @author Phillip Webb
 */
public final class FormatterApp {

	private FormatterApp() {
	}

	public static void main(String[] args) throws Exception, BadLocationException {
		JavaBaseline javaBaseline = JavaBaseline.valueOf(args[0]);
		Formatter formatter = new Formatter(JavaFormatConfig.of(javaBaseline, IndentationStyle.TABS));
		String source = "public class Test {}";
		TextEdit edit = formatter.format(source);
		IDocument document = new Document(source);
		edit.apply(document);
		String formatted = document.get();
		System.out.println(formatted);
	}

}
