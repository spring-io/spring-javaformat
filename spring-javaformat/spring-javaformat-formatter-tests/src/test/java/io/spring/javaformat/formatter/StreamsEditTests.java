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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.eclipse.text.edits.TextEdit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link StreamsEdit}.
 *
 * @author Phillip Webb
 */
public class StreamsEditTests {

	private static final Charset UTF_8 = StandardCharsets.UTF_8;

	@TempDir
	public File temp;

	private File source;

	private File expected;

	private TextEdit textEdit;

	private StreamsEdit streamsEdit;

	@BeforeEach
	void setup() throws IOException {
		this.source = new File(this.temp, "source.txt");
		this.expected = new File(this.temp, "expected.txt");
		Files.copy(new File("src/test/resources/source/javadoc-top.txt").toPath(), this.source.toPath(),
				StandardCopyOption.REPLACE_EXISTING);
		Files.copy(new File("src/test/resources/expected/javadoc-top.txt").toPath(), this.expected.toPath(),
				StandardCopyOption.REPLACE_EXISTING);
		String content = read(this.source);
		this.textEdit = new Formatter().format(content);
		this.streamsEdit = new StreamsEdit(content, this.textEdit);
	}

	@Test
	void hasEditsWhenHasEditsReturnsTrue() throws Exception {
		assertThat(this.streamsEdit.hasEdits()).isTrue();
	}

	@Test
	void hasEditsWhenHasNoEditsReturnsFalse() throws Exception {
		String content = read(this.expected);
		this.textEdit = new Formatter().format(content);
		this.streamsEdit = new StreamsEdit(content, this.textEdit);
		assertThat(this.streamsEdit.hasEdits()).isFalse();
	}

	@Test
	void writeToOutputStreamWritesContent() throws Exception {
		String expected = read(this.expected);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		this.streamsEdit.writeTo(outputStream);
		assertThat(outputStream.toByteArray()).isEqualTo(expected.getBytes(UTF_8));
	}

	@Test
	void writeToOutputStreamWithCharsetWritesContent() throws IOException {
		String expected = read(this.expected);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		this.streamsEdit.writeTo(outputStream, UTF_8);
		assertThat(outputStream.toByteArray()).isEqualTo(expected.getBytes(UTF_8));
	}

	@Test
	void writeToAppendableWritesContent() throws IOException {
		String expected = read(this.expected);
		StringBuilder output = new StringBuilder();
		this.streamsEdit.writeTo(output);
		assertThat(output.toString()).isEqualTo(expected);
	}

	@Test
	void getFormattedContentReturnsFormattedContent() throws Exception {
		String expected = read(this.expected);
		assertThat(this.streamsEdit.getFormattedContent()).isEqualTo(expected);
	}

	private String read(File file) throws IOException {
		return new String(Files.readAllBytes(file.toPath()), UTF_8);
	}

}
