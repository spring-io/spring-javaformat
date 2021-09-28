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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;

import io.spring.javaformat.config.JavaFormatConfig;

/**
 * Base class for formatter tests.
 *
 * @author Phillip Webb
 */
public abstract class AbstractFormatterTests {

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

	protected static Item[] items(String expectedOverride) {
		Collection<Item> files = new ArrayList<>();
		File sourceDir = new File("src/test/resources/source");
		File expectedDir = new File("src/test/resources/expected");
		File configDir = new File("src/test/resources/config");
		File expectedOverrideDir = new File("src/test/resources/" + expectedOverride);
		for (File source : sourceDir.listFiles((dir, name) -> !name.startsWith("."))) {
			File expected = new File(expectedOverrideDir, source.getName());
			if (!expected.exists()) {
				expected = new File(expectedDir, source.getName());
			}
			File config = new File(configDir, source.getName());
			files.add(new Item(source, expected, config));
		}
		return files.toArray(new Item[0]);
	}

	static class Item {

		private final File source;

		private final File expected;

		private final JavaFormatConfig config;

		Item(File source, File expected, File config) {
			this.source = source;
			this.expected = expected;
			this.config = (!config.exists()) ? JavaFormatConfig.DEFAULT : JavaFormatConfig.load(config);
		}

		public File getSource() {
			return this.source;
		}

		public File getExpected() {
			return this.expected;
		}

		public JavaFormatConfig getConfig() {
			return this.config;
		}

		@Override
		public String toString() {
			return this.source.getName();
		}

	}

}
