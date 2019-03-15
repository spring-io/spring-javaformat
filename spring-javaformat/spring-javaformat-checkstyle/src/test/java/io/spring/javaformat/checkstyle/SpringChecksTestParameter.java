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

package io.spring.javaformat.checkstyle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.puppycrawl.tools.checkstyle.api.AuditListener;

/**
 * Checks results by reading a simple text file. Lines can begin with {@code +} if the
 * text must be present or {@code -} if the text must not be present.
 *
 * @author Phillip Webb
 */
public class SpringChecksTestParameter {

	private final List<String> checks;

	public SpringChecksTestParameter(File file) throws IOException {
		this.checks = Collections.unmodifiableList(Files.readAllLines(file.toPath()));
	}

	public void attach(Consumer<AuditListener> consumer) {
		consumer.accept(new AssertionsAuditListener(this.checks));
	}

}
