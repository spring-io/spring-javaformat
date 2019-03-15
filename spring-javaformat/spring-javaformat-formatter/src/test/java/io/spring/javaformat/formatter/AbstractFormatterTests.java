/*
 * Copyright 2017-2019 the original author or authors.
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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * @author Phillip Webb
 */
@RunWith(Parameterized.class)
public abstract class AbstractFormatterTests {

	private final File source;

	private final File expected;

	public AbstractFormatterTests(File source, File expected) {
		this.source = source;
		this.expected = expected;
	}

	protected final File getSource() {
		return this.source;
	}

	protected final File getExpected() {
		return this.expected;
	}

	protected final void print(String name, String content) {
		System.out.println(name + ":");
		System.out.println();
		System.out.println("----------------------------------------");
		System.out.println(content);
		System.out.println("----------------------------------------");
		System.out.println();
		System.out.println();
	}

	protected final String read(File file) throws Exception {
		return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
	}

	protected static Collection<Object[]> files(String expectedOverride) {
		Collection<Object[]> files = new ArrayList<>();
		File sourceDir = new File("src/test/resources/source");
		File expectedDir = new File("src/test/resources/expected");
		File expectedOverrideDir = new File("src/test/resources/" + expectedOverride);
		for (File source : sourceDir.listFiles((dir, name) -> !name.startsWith("."))) {
			File expected = new File(expectedOverrideDir, source.getName());
			if (!expected.exists()) {
				expected = new File(expectedDir, source.getName());
			}
			files.add(new Object[] { source, expected });
		}
		return files;
	}

}
