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

package io.spring.format.formatter.intellij.codestyle;

import com.intellij.openapi.editor.Document;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link EclipseDocumentAdapter}.
 *
 * @author Phillip Webb
 */
public class EclipseDocumentAdapterTests {

	@Test
	public void createShouldUseDocumentText() throws Exception {
		Document intellijDocument = mock(Document.class);
		given(intellijDocument.getText()).willReturn("hello");
		EclipseDocumentAdapter adapter = new EclipseDocumentAdapter(intellijDocument);
		assertThat(adapter.get()).isEqualTo("hello");
	}

	@Test
	public void replaceShouldApplyToIntellijDocument() throws Exception {
		Document intellijDocument = mock(Document.class);
		given(intellijDocument.getText()).willReturn("hello");
		EclipseDocumentAdapter adapter = new EclipseDocumentAdapter(intellijDocument);
		adapter.replace(3, 2, "p");
		assertThat(adapter.get()).isEqualTo("help");
		verify(intellijDocument).replaceString(3, 5, "p");
	}

}
