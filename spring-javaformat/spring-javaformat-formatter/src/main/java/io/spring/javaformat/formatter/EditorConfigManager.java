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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.editorconfig.core.EditorConfig;
import org.editorconfig.core.EditorConfig.OutPair;
import org.editorconfig.core.EditorConfigException;

/**
 * Handles properties from ".editorconfig" file.
 *
 * @author Tadaya Tsuyukubo
 */
public class EditorConfigManager {

	private static final Set<String> PROPERTIES_TO_IGNORE =
			Collections.unmodifiableSet(new HashSet<>(Arrays.asList("tab_width",
					"end_of_line", "charset", "trim_trailing_whitespace", "root")));

	private final EditorConfig editorConfig;

	public EditorConfigManager() {
		this(new EditorConfig());
	}

	public EditorConfigManager(EditorConfig editorConfig) {
		this.editorConfig = editorConfig;
	}

	public Map<String, String> getProperties(File file, String explicitRootDir) {
		List<OutPair> pairs;
		try {
			pairs = this.editorConfig.getProperties(file.getPath(), Collections.singleton(explicitRootDir));
		}
		catch (EditorConfigException ex) {
			throw FileFormatterException.wrap(file, ex);
		}

		Optional<String> tabWidth = pairs.stream().filter((pair) -> "tab_width".equals(pair.getKey()))
				.map(OutPair::getVal)
				.findFirst();

		Map<String, String> props = new LinkedHashMap<>();
		for (OutPair pair : pairs) {
			String key = pair.getKey();
			String value = pair.getVal();

			// Handle well known properties
			// https://github.com/editorconfig/editorconfig/wiki/EditorConfig-Properties
			if ("indent_style".equals(key)) {
				if ("tab".equals(value) || "space".equals(value)) {
					props.put("org.eclipse.jdt.core.formatter.tabulation.char", value);
				}
			}
			else if ("indent_size".equals(key)) {
				boolean useTabWidth = "tab".equals(value) && tabWidth.isPresent();
				String tabSize = useTabWidth ? tabWidth.get() : value;
				try {
					Integer.parseInt(tabSize);
					props.put("org.eclipse.jdt.core.formatter.tabulation.size", tabSize);
				}
				catch (NumberFormatException ex) {
				}
			}
			else if ("insert_final_newline".equals(key)) {
				String val = "true".equals(value) ? "insert" : "do not insert";
				props.put("org.eclipse.jdt.core.formatter.insert_new_line_at_end_of_file_if_missing", val);
			}
			else if (PROPERTIES_TO_IGNORE.contains(key)) {
				// ignore
			}
			else {
				props.put(key, value);
			}
		}

		return props;
	}

}
