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

package io.spring.javaformat.formatter;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link StreamsFormatter}.
 *
 * @author Phillip Webb
 */
public class StreamsFormatterTests extends AbstractFormatterTests {

	@ParameterizedTest
	@MethodSource("items")
	void formatInputStreamFormatsFile(Item item) throws Exception {
		try (InputStream inputStream = new FileInputStream(item.getSource())) {
			StreamsEdit edit = new StreamsFormatter(item.getConfig()).format(inputStream);
			assertThat(edit.getFormattedContent()).isEqualTo(read(item.getExpected()));
		}
	}

	@ParameterizedTest
	@MethodSource("items")
	void formatInputStreamWithCharsetFormatsFile(Item item) throws Exception {
		try (InputStream inputStream = new FileInputStream(item.getSource())) {
			StreamsEdit edit = new StreamsFormatter(item.getConfig()).format(inputStream, StandardCharsets.UTF_8);
			assertThat(edit.getFormattedContent()).isEqualTo(read(item.getExpected()));
		}
	}

	@ParameterizedTest
	@MethodSource("items")
	void formatReaderFormatsFile(Item item) throws Exception {
		try (Reader reader = new InputStreamReader(new FileInputStream(item.getSource()))) {
			StreamsEdit edit = new StreamsFormatter(item.getConfig()).format(reader);
			assertThat(edit.getFormattedContent()).isEqualTo(read(item.getExpected()));
		}
	}

	static Item[] items() {
		return items(null);
	}

}
