/*
 * Copyright 2017-2025 the original author or authors.
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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.spring.javaformat.config.JavaBaseline;
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
		List<Item> items = new ArrayList<>();
		File sourceDir = new File("src/test/resources/source");
		File expectedDir = new File("src/test/resources/expected");
		File configDir = new File("src/test/resources/config");
		File expectedOverrideDir = new File("src/test/resources/" + expectedOverride);
		for (File source : sourceDir.listFiles((dir, name) -> !name.startsWith("."))) {
			File config = new File(configDir, source.getName());
			for (JavaBaseline javaBaseline : JavaBaseline.values()) {
				File expected = getExpected(source, javaBaseline, expectedOverrideDir, expectedDir);
				addItem(items, javaBaseline, source, expected, config);
			}
		}
		items.sort(Comparator.comparing(Item::getName));
		return items.toArray(new Item[0]);
	}

	private static File getExpected(File source, JavaBaseline javaBaseline, File... expectedDirs) {
		for (File expectedDir : expectedDirs) {
			File versionSpecificExpectedDir = new File(expectedDir, javaBaseline.toString().toLowerCase());
			File expected = new File(versionSpecificExpectedDir, source.getName());
			expected = (!expected.exists()) ? new File(expectedDir, source.getName()) : expected;
			if (expected.exists()) {
				return expected;
			}
		}
		throw new IllegalStateException("Unable to find expected file");
	}

	private static void addItem(List<Item> items, JavaBaseline javaBaseline, File source, File expected, File config) {
		if (source.getName().contains("lineendings")) {
			items.add(new Item(javaBaseline, copy(source, LineEnding.CR), copy(expected, LineEnding.CR), config));
			items.add(new Item(javaBaseline, copy(source, LineEnding.LF), copy(expected, LineEnding.LF), config));
			items.add(new Item(javaBaseline, copy(source, LineEnding.CRLF), copy(expected, LineEnding.CRLF), config));
		}
		else {
			items.add(new Item(javaBaseline, source, expected, config));
		}
	}

	private static File copy(File file, LineEnding lineEnding) {
		try {
			String[] name = file.getName().split("\\.");
			File result = File.createTempFile(name[0] + "_" + lineEnding + "_", "." + name[1]);
			String content = Files.readString(file.toPath());
			content = content.replace("\r\n", "\n").replace('\r', '\n').replace("\n", lineEnding.ending());
			Files.writeString(result.toPath(), content);
			return result;
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	enum LineEnding {

		CR("\r"), LF("\n"), CRLF("\r\n");

		private final String ending;

		LineEnding(String ending) {
			this.ending = ending;
		}

		String ending() {
			return this.ending;
		}

	};

	static class Item {

		private final JavaBaseline javaBaseline;

		private final File source;

		private final File expected;

		private final JavaFormatConfig config;

		Item(JavaBaseline javaBaseline, File source, File expected, File config) {
			this.javaBaseline = javaBaseline;
			this.source = source;
			this.expected = expected;
			this.config = loadConfig(javaBaseline, config);
		}

		private JavaFormatConfig loadConfig(JavaBaseline javaBaseline, File configFile) {
			JavaFormatConfig config = (!configFile.exists()) ? JavaFormatConfig.DEFAULT
					: JavaFormatConfig.load(configFile);
			return JavaFormatConfig.of(javaBaseline, config.getIndentationStyle());
		}

		String getName() {
			return this.source.getName();
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
			return this.javaBaseline + " " + this.source.getName();
		}

	}

}
