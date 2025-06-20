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

package io.spring.javaformat.config;

import java.util.Properties;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PropertiesJavaFormatConfig}.
 *
 * @author Phillip Webb
 */
class PropertiesJavaFormatConfigTests {

	@Test
	void getJavaBaselineWhenNoPropertyReturnsJava11() {
		Properties properties = new Properties();
		PropertiesJavaFormatConfig config = new PropertiesJavaFormatConfig(properties);
		assertThat(config.getJavaBaseline()).isEqualTo(JavaBaseline.V17);
	}

	@Test
	void getJavaBaselineWhenPropertyReturnsPropertyValue() {
		Properties properties = new Properties();
		properties.setProperty("java-baseline", "8");
		PropertiesJavaFormatConfig config = new PropertiesJavaFormatConfig(properties);
		assertThat(config.getJavaBaseline()).isEqualTo(JavaBaseline.V8);
	}

	@Test
	void getIndentationStyleWhenNoPropertyReturnsJava11() {
		Properties properties = new Properties();
		PropertiesJavaFormatConfig config = new PropertiesJavaFormatConfig(properties);
		assertThat(config.getIndentationStyle()).isEqualTo(IndentationStyle.TABS);
	}

	@Test
	void getIndentationStyleWhenPropertyReturnsPropertyValue() {
		Properties properties = new Properties();
		properties.setProperty("indentation-style", "spaces");
		PropertiesJavaFormatConfig config = new PropertiesJavaFormatConfig(properties);
		assertThat(config.getIndentationStyle()).isEqualTo(IndentationStyle.SPACES);
	}

}
