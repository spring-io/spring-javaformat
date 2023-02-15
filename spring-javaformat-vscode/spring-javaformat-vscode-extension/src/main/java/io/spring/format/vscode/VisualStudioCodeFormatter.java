/*
 * Copyright 2017-2023 the original author or authors.
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

package io.spring.format.vscode;

import java.io.File;

import io.spring.javaformat.config.JavaFormatConfig;
import io.spring.javaformat.formatter.StreamsFormatter;

/**
 * Called from the Visual Studio Code extension to format source code.
 *
 * @author Howard Zuo
 * @author Phillip Webb
 */
public final class VisualStudioCodeFormatter {

	private VisualStudioCodeFormatter() {
	}

	private void run(String[] args) throws Exception {
		File location = new File(".").getAbsoluteFile();
		log(String.format("Loading formatter from location '%s'", location));
		JavaFormatConfig config = JavaFormatConfig.findFrom(location);
		StreamsFormatter formatter = new StreamsFormatter(config);
		formatter.format(System.in).writeTo((Appendable) System.out);
	}

	private void log(String message) {
		System.err.println(message);
	}

	public static void main(String[] args) {
		try {
			new VisualStudioCodeFormatter().run(args);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}

}
