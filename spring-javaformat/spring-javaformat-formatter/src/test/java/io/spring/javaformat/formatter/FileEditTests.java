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
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.eclipse.text.edits.TextEdit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FileEdit}.
 *
 * @author Phillip Webb
 */
public class FileEditTests {

	private static final Charset UTF_8 = StandardCharsets.UTF_8;

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	private File source;

	private File expected;

	private TextEdit textEdit;

	private FileEdit fileEdit;

	@Before
	public void setup() throws IOException {
		this.source = this.temp.newFile("source.txt");
		this.expected = this.temp.newFile("expected.txt");
		Files.copy(new File("src/test/resources/source/javadoc-top.txt").toPath(),
				this.source.toPath(), StandardCopyOption.REPLACE_EXISTING);
		Files.copy(new File("src/test/resources/expected/javadoc-top.txt").toPath(),
				this.expected.toPath(), StandardCopyOption.REPLACE_EXISTING);
		String content = read(this.source);
		this.textEdit = new Formatter().format(content);
		this.fileEdit = new FileEdit(this.source, UTF_8, content, this.textEdit);
	}

	@Test
	public void getFileShouldReturnFile() throws Exception {
		assertThat(this.fileEdit.getFile()).isEqualTo(this.source);
	}

	@Test
	public void hasEditsWhenHasEditsShouldReturnTrue() throws Exception {
		assertThat(this.fileEdit.hasEdits()).isTrue();
	}

	@Test
	public void hasEditsWhenHasNoEditsShouldReturnFalse() throws Exception {
		String content = read(this.expected);
		this.textEdit = new Formatter().format(content);
		this.fileEdit = new FileEdit(this.source, UTF_8, content, this.textEdit);
		assertThat(this.fileEdit.hasEdits()).isFalse();
	}

	@Test
	public void saveShouldSaveContent() throws Exception {
		String expected = read(this.expected);
		assertThat(read(this.source)).isNotEqualTo(expected);
		this.fileEdit.save();
		assertThat(read(this.source)).isEqualTo(expected);
	}

	@Test
	public void getFormattedContentShouldReturnFormattedContent() throws Exception {
		String expected = read(this.expected);
		assertThat(this.fileEdit.getFormattedContent()).isEqualTo(expected);
	}

	private String read(File file) throws IOException {
		return new String(Files.readAllBytes(file.toPath()), UTF_8);
	}

}
