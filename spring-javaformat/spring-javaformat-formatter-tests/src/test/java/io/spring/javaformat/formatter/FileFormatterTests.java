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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FileFormatter}.
 *
 * @author Phillip Webb
 */
public class FileFormatterTests extends AbstractFormatterTests {

	private static final boolean RUNNING_ON_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

	@ParameterizedTest
	@MethodSource("items")
	public void formatFilesFromIteratorShouldFormatFile(Item item) throws Exception {
		FileEdit edit = new FileFormatter(item.getConfig())
				.formatFiles(Arrays.asList(item.getSource()), StandardCharsets.UTF_8).findFirst().get();
		assertThat(edit.getFormattedContent()).isEqualTo(read(item.getExpected()));
	}

	@ParameterizedTest
	@MethodSource("items")
	public void formatFilesFromStreamShouldFormatFile(Item item) throws Exception {
		FileEdit edit = new FileFormatter(item.getConfig())
				.formatFiles(Arrays.asList(item.getSource()).stream(), StandardCharsets.UTF_8).findFirst().get();
		assertThat(edit.getFormattedContent()).isEqualTo(read(item.getExpected()));
	}

	@ParameterizedTest
	@MethodSource("items")
	public void formatFileShouldFormatFile(Item item) throws Exception {
		File source = item.getSource();
		FileEdit edit = new FileFormatter(item.getConfig()).formatFile(source, StandardCharsets.UTF_8);
		String formattedContent = edit.getFormattedContent();
		String expected = read(item.getExpected());
		if (!RUNNING_ON_WINDOWS) {
			System.out.println(source);
			System.out.println("----- got");
			System.out.println(formattedContent);
			System.out.println("----- expected");
			System.out.println(expected);
			System.out.println("-----");
		}
		assertThat(formattedContent).isEqualTo(expected);
	}

	public static Item[] items() {
		return items(null);
	}

}
