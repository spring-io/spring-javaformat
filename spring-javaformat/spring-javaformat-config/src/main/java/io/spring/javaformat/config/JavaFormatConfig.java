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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Support for the {@code .springjavaformatconfig} file that can be used to apply settings
 * on a per-project basis.
 *
 * @author Phillip Webb
 */
public interface JavaFormatConfig {

	/**
	 * The default {@link JavaFormatConfig}.
	 */
	JavaFormatConfig DEFAULT = of(JavaBaseline.V11, IndentationStyle.TABS);

	/**
	 * Java JDK baseline version expected be used when formatting.
	 * @return the JDK version
	 */
	JavaBaseline getJavaBaseline();

	/**
	 * Return the indentation style that should be used with the project.
	 * @return the indentation style
	 */
	IndentationStyle getIndentationStyle();

	/**
	 * Find and load a {@code .springjavaformatconfig} by searching from the given file.
	 * @param path the file or directory to search from
	 * @return a loaded {@link JavaFormatConfig} or {@link #DEFAULT} if no
	 * {@code .springjavaformatconfig} file is found
	 */
	static JavaFormatConfig findFrom(Path path) {
		return findFrom((path != null) ? path.toFile() : (File) null);
	}

	/**
	 * Find and load a {@code .springjavaformatconfig} by searching from the given file.
	 * @param file the file or directory to search from
	 * @return a loaded {@link JavaFormatConfig} or {@link #DEFAULT} if no
	 * {@code .springjavaformatconfig} file is found
	 */
	static JavaFormatConfig findFrom(File file) {
		if (file != null && file.isFile()) {
			return findFrom(file.getParentFile());
		}
		try {
			while (file != null) {
				File candidate = new File(file, ".springjavaformatconfig");
				if (candidate.exists() && candidate.isFile()) {
					return load(candidate);
				}
				file = file.getParentFile();
			}
		}
		catch (Exception ex) {
		}
		return DEFAULT;
	}

	/**
	 * Load a {@link JavaFormatConfig} from the given file.
	 * @param file the file to load
	 * @return the loaded config
	 */
	static JavaFormatConfig load(File file) {
		try {
			return PropertiesJavaFormatConfig.load(file);
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	/**
	 * Load a {@link JavaFormatConfig} from the given input stream.
	 * @param inputStream the input stream to load
	 * @return the loaded config
	 */
	static JavaFormatConfig load(InputStream inputStream) {
		try {
			return PropertiesJavaFormatConfig.load(inputStream);
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	/**
	 * Factory method to create a {@link JavaFormatConfig} with specific settings.
	 * @param javaBaseline The baseline JDK version
	 * @param indentationStyle the indentation style
	 * @return a {@link JavaFormatConfig} instance
	 */
	static JavaFormatConfig of(JavaBaseline javaBaseline, IndentationStyle indentationStyle) {
		return new DefaultJavaFormatConfig(javaBaseline, indentationStyle);
	}

}
