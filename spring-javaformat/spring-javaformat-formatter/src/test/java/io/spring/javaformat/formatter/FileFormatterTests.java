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

package io.spring.javaformat.formatter;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for FileFormatter.
 *
 * @author Phillip Webb
 */
public class FileFormatterTests extends AbstractFormatterTests {

	public FileFormatterTests(File source, File expected) {
		super(source, expected);
	}

	@Test
	public void formatFilesFromIteratorShouldFormatFile() throws Exception {
		FileEdit edit = new FileFormatter()
				.formatFiles(Arrays.asList(getSource()), StandardCharsets.UTF_8)
				.findFirst().get();
		assertThat(edit.getFormattedContent()).isEqualTo(read(getExpected()));
	}

	@Test
	public void formatFilesFromStreamShouldFormatFile() throws Exception {
		FileEdit edit = new FileFormatter()
				.formatFiles(Arrays.asList(getSource()).stream(), StandardCharsets.UTF_8)
				.findFirst().get();
		assertThat(edit.getFormattedContent()).isEqualTo(read(getExpected()));
	}

	@Test
	public void formatFileShouldFormatFile() throws Exception {
		File source = getSource();
		FileEdit edit = new FileFormatter().formatFile(source, StandardCharsets.UTF_8);
		String formattedContent = edit.getFormattedContent();
		String expected = read(getExpected());
		System.out.println(source);
		System.out.println("----- got");
		System.out.println(formattedContent);
		System.out.println("----- expected");
		System.out.println(expected);
		System.out.println("-----");
		assertThat(formattedContent).isEqualTo(expected);
	}

}
