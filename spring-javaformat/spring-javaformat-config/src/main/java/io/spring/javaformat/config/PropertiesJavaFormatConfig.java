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

package io.spring.javaformat.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * {@link JavaFormatConfig} backed by a properties file.
 *
 * @author Phillip Webb
 */
class PropertiesJavaFormatConfig implements JavaFormatConfig {

	private final Properties properties;

	PropertiesJavaFormatConfig(Properties properties) {
		this.properties = properties;
	}

	@Override
	public JavaBaseline getJavaBaseline() {
		Object value = this.properties.get("java-baseline");
		return (value != null) ? JavaBaseline.valueOf("v" + value.toString().toUpperCase().trim())
				: DEFAULT.getJavaBaseline();
	}

	@Override
	public IndentationStyle getIndentationStyle() {
		Object value = this.properties.get("indentation-style");
		return (value != null) ? IndentationStyle.valueOf(value.toString().toUpperCase().trim())
				: DEFAULT.getIndentationStyle();
	}

	static JavaFormatConfig load(File file) throws IOException {
		try (InputStream inputStream = new FileInputStream(file)) {
			return load(inputStream);
		}
	}

	static JavaFormatConfig load(InputStream inputStream) throws IOException {
		Properties properties = new Properties();
		properties.load(inputStream);
		return new PropertiesJavaFormatConfig(properties);
	}

}
