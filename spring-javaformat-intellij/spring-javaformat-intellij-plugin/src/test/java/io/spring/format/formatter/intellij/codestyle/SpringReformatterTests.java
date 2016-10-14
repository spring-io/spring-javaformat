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

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link SpringReformatter}.
 *
 * @author Phillip Webb
 */
public class SpringReformatterTests {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Mock
	private Project project;

	@Mock
	private Application application;

	@Mock
	private PsiDocumentManager documentManager;

	private SpringReformatter reformatter;

	@Mock
	private PsiFile file;

	private Collection<TextRange> ranges = Arrays.asList(new TextRange(10, 20));

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.reformatter = new TestSpringReformatter(() -> this.project,
				() -> this.application, () -> this.documentManager);
	}

	@Test
	public void reformatShouldAssertWriteAccess() throws Exception {
		given(this.file.isWritable()).willReturn(true);
		this.reformatter.reformat(this.file, this.ranges);
		verify(this.application).assertWriteAccessAllowed();
	}

	@Test
	public void reformatShouldCommitAllDocuments() throws Exception {
		given(this.file.isWritable()).willReturn(true);
		this.reformatter.reformat(this.file, this.ranges);
		verify(this.documentManager).commitAllDocuments();
	}

	@Test
	public void reformatWhenFileIsNotWriteableShouldThrow() throws Exception {
		this.thrown.expect(IncorrectOperationException.class);
		this.reformatter.reformat(this.file, this.ranges);
	}

	@Test
	public void reformatShouldReformatDocument() throws Exception {
		given(this.file.isWritable()).willReturn(true);
		Document document = mock(Document.class);
		String text = "public class Hello {}";
		given(document.getText()).willReturn(text);
		given(this.documentManager.getDocument(this.file)).willReturn(document);
		this.reformatter.reformat(this.file,
				Arrays.asList(new TextRange(0, text.length())));
		verify(document).replaceString(20, 20, "\n\n");
		verify(this.documentManager).commitDocument(document);
	}

	static class TestSpringReformatter extends SpringReformatter {

		TestSpringReformatter(Supplier<Project> project,
				Supplier<Application> application,
				Supplier<PsiDocumentManager> documentManager) {
			super(project, application, documentManager);
		}

		@Override
		protected void runWriteCommandAction(Runnable runnable) {
			runnable.run();
		}

	}

}
