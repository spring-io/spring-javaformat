/*
 * Copyright 2017-2020 the original author or authors.
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

package io.spring.javaformat.checkstyle.check;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
public class SpringHeaderCheck extends AbstractFileSetCheck {

	/**
	 * Value used when the header is not checked.
	 */
	private static final String UNCHECKED = "unchecked";

	/**
	 * Value used to enforce that there must not be a header.
	 */
	private static final String NONE = "none";

	/**
	 * The default header type.
	 */
	public static final String DEFAULT_HEADER_TYPE = "apache2";

	/**
	 * The default header copyright pattern.
	 */
	public static final String DEFAULT_HEADER_COPYRIGHT_PATTERN = "20\\d\\d(-20\\d\\d)?";

	private static final String DEFAULT_CHARSET = System.getProperty("file.encoding", StandardCharsets.UTF_8.name());

	private String charset = DEFAULT_CHARSET;

	private String headerType = DEFAULT_HEADER_TYPE;

	private URI headerFile;

	private String headerCopyrightPattern = DEFAULT_HEADER_COPYRIGHT_PATTERN;

	private String packageInfoHeaderType;

	private URI packageInfoHeaderFile;

	private boolean blankLineAfter = true;

	private HeaderCheck check;

	private HeaderCheck packageInfoCheck;

	@Override
	protected void finishLocalSetup() throws CheckstyleException {
		try {
			this.check = createCheck(this.headerType, this.headerFile);
			String packageInfoHeaderType = this.packageInfoHeaderType != null ? this.packageInfoHeaderType
					: this.headerType;
			URI packageInfoHeaderFile = this.packageInfoHeaderFile != null ? this.packageInfoHeaderFile
					: this.headerFile;
			this.packageInfoCheck = createCheck(packageInfoHeaderType, packageInfoHeaderFile);
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private HeaderCheck createCheck(String headerType, URI headerFile) throws IOException {
		if (UNCHECKED.equals(headerType)) {
			return HeaderCheck.NONE;
		}
		if (NONE.equals(headerType)) {
			return new NoHeaderCheck();
		}
		return new RegexHeaderCheck(headerType, headerFile);
	}

	@Override
	protected void processFiltered(File file, FileText fileText) throws CheckstyleException {
		getCheck(file).run(fileText, this.blankLineAfter);
	}

	private HeaderCheck getCheck(File file) {
		if (file.getName().startsWith("package-info")) {
			return this.packageInfoCheck;
		}
		return this.check;
	}

	public void setCharset(String charset) throws UnsupportedEncodingException {
		if (!Charset.isSupported(charset)) {
			throw new UnsupportedEncodingException("unsupported charset: '" + charset + "'");
		}
		this.charset = charset;
	}

	public void setHeaderType(String headerType) {
		this.headerType = headerType;
	}

	public void setHeaderFile(URI headerFile) throws CheckstyleException {
		this.headerFile = headerFile;
	}

	public void setHeaderCopyrightPattern(String headerCopyrightPattern) {
		this.headerCopyrightPattern = headerCopyrightPattern;
	}

	public void setPackageInfoHeaderType(String packageInfoHeaderType) {
		this.packageInfoHeaderType = packageInfoHeaderType;
	}

	public void setPackageInfoHeaderFile(URI packageInfoHeaderFile) {
		this.packageInfoHeaderFile = packageInfoHeaderFile;
	}

	public void setBlankLineAfter(boolean blankLineAfter) {
		this.blankLineAfter = blankLineAfter;
	}

	/**
	 * Interface used to check for a header.
	 */
	private interface HeaderCheck {

		/**
		 * Don't run any checks.
		 */
		HeaderCheck NONE = (fileText, blankLineAfter) -> true;

		/**
		 * Run the check.
		 * @param fileText the text to check
		 * @param blankLineAfter if a blank line should be after the header
		 * @return {@code true} if the header is valid
		 */
		boolean run(FileText fileText, boolean blankLineAfter);

	}

	/**
	 * {@link HeaderCheck} based on regular expressions.
	 */
	private class RegexHeaderCheck implements HeaderCheck {

		private final List<Pattern> lines;

		RegexHeaderCheck(String type, URI file) throws IOException {
			this.lines = loadLines(openInputStream(type, file));
		}

		private InputStream openInputStream(String type, URI file) throws IOException {
			if (file != null) {
				return file.toURL().openStream();
			}
			String name = "header-" + type + ".txt";
			InputStream inputStream = SpringHeaderCheck.class.getResourceAsStream(name);
			if (inputStream == null) {
				throw new IllegalStateException("Unknown header type " + type);
			}
			return inputStream;
		}

		private List<Pattern> loadLines(InputStream inputStream) throws IOException {
			inputStream = new BufferedInputStream(inputStream);
			try (Reader reader = new InputStreamReader(inputStream, SpringHeaderCheck.this.charset)) {
				LineNumberReader lineReader = new LineNumberReader(reader);
				List<Pattern> lines = new ArrayList<>();
				while (true) {
					String line = lineReader.readLine();
					if (line == null) {
						return lines;
					}
					lines.add(loadLine(line, SpringHeaderCheck.this.headerCopyrightPattern));
				}
			}
		}

		private Pattern loadLine(String line, String copyrightPattern) {
			line = line.replace("${copyright-pattern}", "\\E" + copyrightPattern + "\\Q");
			line = "^\\Q" + line + "\\E$";
			return Pattern.compile(line);
		}

		@Override
		public boolean run(FileText fileText, boolean blankLineAfter) {
			if (this.lines.size() > fileText.size()) {
				log(1, RegexpHeaderCheck.MSG_HEADER_MISSING);
				return false;
			}
			for (int i = 0; i < this.lines.size(); i++) {
				String fileLine = fileText.get(i);
				Pattern pattern = this.lines.get(i);
				if (!pattern.matcher(fileLine).matches()) {
					log(i + 1, RegexpHeaderCheck.MSG_HEADER_MISMATCH, pattern);
					return false;
				}
			}
			if (blankLineAfter) {
				if (fileText.size() <= this.lines.size() || !"".equals(fileText.get(this.lines.size()))) {
					log(this.lines.size() + 1, "header.blankLine");
				}
			}
			return true;
		}

	}

	/**
	 * {@link HeaderCheck} to enforce that there is no header.
	 */
	private class NoHeaderCheck implements HeaderCheck {

		@Override
		public boolean run(FileText fileText, boolean blankLineAfter) {
			for (int i = 0; i < fileText.size(); i++) {
				String fileLine = fileText.get(i);
				if (fileLine.trim().isEmpty()) {
					continue;
				}
				if (isHeaderComment(fileLine)) {
					log(i, "header.unexpected");
					return false;
				}
				return true;
			}
			return true;
		}

		private boolean isHeaderComment(String fileLine) {
			return (fileLine.contains("/*") || fileLine.contains("//")) && !fileLine.contains("/**");
		}

	}

}
