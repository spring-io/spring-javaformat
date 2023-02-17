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

package io.spring.javaformat.checkstyle.check;

import java.io.File;

import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * Checks that test filenames end {@literal Tests.java} and not {@literal Test.java}.
 *
 * @author Phillip Webb
 */
public class SpringTestFileNameCheck extends AbstractFileSetCheck {

	@Override
	protected void processFiltered(File file, FileText fileText) throws CheckstyleException {
		String path = file.getPath().replace('\\', '/');
		if (path.contains("src/test/java") && file.getName().endsWith("Test.java")) {
			log(1, "testfilename.wrongName");
		}
	}

}
