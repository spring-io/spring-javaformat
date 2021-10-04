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

package io.spring.javaformat.formatter.eclipse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import io.spring.javaformat.config.IndentationStyle;
import io.spring.javaformat.config.JavaFormatConfig;

/**
 * Utility class used to load formatter options.
 *
 * @author Phillip Webb
 */
public class Options {

	private final String prefix;

	public Options(String prefix) {
		this.prefix = prefix;
	}

	public Map<String, String> load(JavaFormatConfig javaFormatConfig) {
		try {
			Map<String, String> properties = loadProperties();
			applyConfig(properties, javaFormatConfig);
			return Collections.unmodifiableMap(properties);
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private Map<String, String> loadProperties() throws IOException {
		Properties properties = new Properties();
		try (InputStream inputStream = getClass().getResourceAsStream("formatter.prefs")) {
			properties.load(inputStream);
		}
		Map<String, String> prefixedProperties = new LinkedHashMap<>();
		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
			prefixedProperties.put(this.prefix + "." + entry.getKey(), (String) entry.getValue());
		}
		return prefixedProperties;
	}

	private void applyConfig(Map<String, String> properties, JavaFormatConfig javaFormatConfig) {
		if (javaFormatConfig.getIndentationStyle() == IndentationStyle.SPACES) {
			properties.put(this.prefix + ".core.formatter.tabulation.char", "space");
		}
	}

}
