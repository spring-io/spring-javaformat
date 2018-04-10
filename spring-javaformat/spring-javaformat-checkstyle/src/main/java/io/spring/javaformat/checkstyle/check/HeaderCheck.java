/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.javaformat.checkstyle.check;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.checks.header.RegexpHeaderCheck;

/**
 * Checks that the source headers follow Spring conventions.
 *
 * @author Phillip Webb
 */
public class HeaderCheck extends AbstractFileSetCheck {

	private static final String UNCHECKED = "unchecked";

	/**
	 * The default header type.
	 */
	public static final String DEFAULT_HEADER_TYPE = "apache2";

	/**
	 * The default header copyright pattern.
	 */
	public static final String DEFAULT_HEADER_COPYRIGHT_PATTERN = "20\\d\\d-20\\d\\d";

	private String headerType = DEFAULT_HEADER_TYPE;

	private String headerCopyrightPattern = DEFAULT_HEADER_COPYRIGHT_PATTERN;

	private List<Pattern> headerLines;

	@Override
	protected void finishLocalSetup() throws CheckstyleException {
		try {
			this.headerLines = loadHeaderLines();
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private List<Pattern> loadHeaderLines() throws IOException {
		if (UNCHECKED.equals(this.headerType)) {
			return null;
		}
		String name = "header-" + this.headerType + ".txt";
		InputStream inputStream = getClass().getResourceAsStream(name);
		if (inputStream == null) {
			throw new IllegalStateException("Unknown header type " + this.headerType);
		}
		inputStream = new BufferedInputStream(inputStream);
		try (Reader reader = new InputStreamReader(inputStream, "UTF-8")) {
			LineNumberReader lineReader = new LineNumberReader(reader);
			List<Pattern> lines = new ArrayList<>();
			while (true) {
				String line = lineReader.readLine();
				if (line == null) {
					return lines;
				}
				lines.add(loadHeaderLine(line));
			}
		}
	}

	private Pattern loadHeaderLine(String line) {
		line = line.replace("${copyright-pattern}",
				"\\E" + this.headerCopyrightPattern + "\\Q");
		line = "^\\Q" + line + "\\E$";
		return Pattern.compile(line);
	}

	@Override
	protected void processFiltered(File file, FileText fileText)
			throws CheckstyleException {
		if (UNCHECKED.equals(this.headerType)) {
			return;
		}
		if (this.headerLines.size() > fileText.size()) {
			log(1, RegexpHeaderCheck.MSG_HEADER_MISSING);
			return;
		}
		for (int i = 0; i < this.headerLines.size(); i++) {
			String fileLine = fileText.get(i);
			Pattern headerLine = this.headerLines.get(i);
			if (!headerLine.matcher(fileLine).matches()) {
				log(i + 1, RegexpHeaderCheck.MSG_HEADER_MISMATCH, headerLine);
				return;
			}
		}
	}

	public void setHeaderType(String headerType) {
		this.headerType = headerType;
	}

	public void setHeaderCopyrightPattern(String headerCopyrightPattern) {
		this.headerCopyrightPattern = headerCopyrightPattern;
	}

}
