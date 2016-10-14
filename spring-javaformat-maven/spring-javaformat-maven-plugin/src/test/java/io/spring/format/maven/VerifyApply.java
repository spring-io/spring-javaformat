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

package io.spring.format.maven;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verify the "apply" result.
 *
 * @author Phillip Webb
 */
public class VerifyApply {

	private static final String JAVA_FILE = "src/main/java/simple/Simple.java";

	public void verify(File base) throws IOException {
		String formated = new String(Files.readAllBytes(base.toPath().resolve(JAVA_FILE)),
				StandardCharsets.UTF_8);
		assertThat(formated).contains("public class Simple {");
	}

}
