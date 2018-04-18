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

package io.spring.javaformat.eclipse;

import org.eclipse.osgi.util.NLS;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Messages}.
 *
 * @author Phillip Webb
 */
public class MessagesTests {

	@Test
	public void bindHasCorrectMessage() {
		String message = NLS.bind(Messages.springFormatSettingsImportError, "reason");
		assertThat(message)
				.isEqualTo("Error importing project specific settings: reason");
	}

}
