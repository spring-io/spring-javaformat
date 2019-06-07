/*
 * Copyright 2012-2019 the original author or authors.
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

package io.spring.javaformat.eclipse.projectsettings;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * Eclipse properties used to modify setting files content.
 *
 * @author Phillip Webb
 */
class ProjectProperties {

	private static final String COPYRIGHT_YEAR = "copyright-year";

	private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");

	private static final Map<String, Supplier<String>> DEFAULTS;
	static {
		Map<String, Supplier<String>> defaults = new HashMap<>();
		defaults.put(COPYRIGHT_YEAR, () -> LocalDateTime.now().format(YEAR_FORMATTER));
		DEFAULTS = Collections.unmodifiableMap(defaults);
	}

	private Map<String, String> properties = new HashMap<>();

	public void addFromFolder(File folder) throws IOException {
		File file = new File(folder, "eclipse.properties");
		if (file.exists()) {
			addFromFile(file);
		}
	}

	private void addFromFile(File file) throws IOException {
		try (FileInputStream inputStream = new FileInputStream(file)) {
			Properties properties = new Properties();
			properties.load(inputStream);
			addFromProperties(properties);
		}
	}

	private void addFromProperties(Properties properties) {
		properties.forEach((key, value) -> {
			this.properties.putIfAbsent(key.toString(), value.toString());
		});
	}

	public InputStream getModifiedContent(ProjectSettingsFile file) throws IOException {
		if (file.getName().equals("org.eclipse.jdt.ui.prefs")) {
			String content = loadContent(file);
			content = content.replace("Copyright the original author or authors",
					"Copyright " + get(COPYRIGHT_YEAR) + " the original author or authors");
			return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
		}
		return file.getContent();
	}

	private String loadContent(ProjectSettingsFile file) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContent()))) {
			StringWriter writer = new StringWriter();
			char[] buffer = new char[4096];
			int read = 0;
			while ((read = reader.read(buffer)) >= 0) {
				writer.write(buffer, 0, read);
			}
			return writer.toString();
		}
	}

	String get(String name) {
		String value = this.properties.get(name);
		return (value != null ? value : DEFAULTS.getOrDefault(name, () -> null).get());
	}

}
