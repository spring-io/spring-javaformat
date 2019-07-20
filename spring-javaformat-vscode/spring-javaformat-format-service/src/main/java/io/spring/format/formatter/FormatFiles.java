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

package io.spring.format.formatter;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.spring.javaformat.formatter.FileFormatter;

/**
 * . Format specified .java files
 *
 * @author Howard Zuo
 */
final public class FormatFiles {

	private final static FileFormatter formatter = new FileFormatter();

	private FormatFiles() {

	}

	public static List<String> formatFiles(List<String> filePaths) {
		List<File> files = filePaths.stream().map(a -> new File(a)).filter(f -> f.exists())
				.collect(Collectors.toList());

		if (files.size() == 0) {
			return Collections.emptyList();
		}

		return formatter.formatFiles(files, Charset.forName("UTF-8")).map(editor -> {
			try {
				return editor.getFormattedContent();
			}
			catch (Exception e) {
				return "";
			}
		}).filter(content -> !"".equals(content) && content != null).collect(Collectors.toList());
	}

}
