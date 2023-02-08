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

package io.spring.format.formatter.intellij.formatting;

import java.util.Collections;

import com.intellij.formatting.FormattingContext;
import com.intellij.formatting.service.FormattingService.Feature;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.junit.jupiter.api.Test;

import io.spring.format.formatter.intellij.state.State;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link SpringJavaFormatFormattingService}.
 *
 * @author Phillip Webb
 */
class SpringJavaFormatFormattingServiceTests {

	private SpringJavaFormatFormattingService service = new SpringJavaFormatFormattingService(
			(project, runnable) -> runnable.run());

	@Test
	void getFeaturesReturnsFormatFragments() {
		assertThat(this.service.getFeatures()).containsExactly(Feature.FORMAT_FRAGMENTS);
	}

	@Test
	void canFormatWhenNotJavaReturnsFalse() {
		FileType fileType = PlainTextFileType.INSTANCE;
		PsiFile file = mockFile(fileType, State.ACTIVE);
		assertThat(this.service.canFormat(file)).isFalse();
	}

	@Test
	void canFormatWhenJavaFileAndNotActiveReturnsFalse() {
		FileType fileType = FileTypeManager.getInstance().getStdFileType("JAVA");
		PsiFile file = mockFile(fileType, State.NOT_ACTIVE);
		assertThat(this.service.canFormat(file)).isFalse();
	}

	@Test
	void canFormatWhenJavaFileAndActiveReturnsTrue() {
		FileType fileType = FileTypeManager.getInstance().getStdFileType("JAVA");
		PsiFile file = mockFile(fileType, State.ACTIVE);
		assertThat(this.service.canFormat(file)).isTrue();
	}

	@Test
	void formatDocumentAppliesFormatting() {
		Document document = mock(Document.class);
		String text = "public class Hello {}";
		given(document.getText()).willReturn(text);
		FormattingContext formattingContext = mock(FormattingContext.class);
		this.service.formatDocument(document, Collections.emptyList(), formattingContext, false, false);
		verify(document).replaceString(20, 20, "\n\n");
	}

	private PsiFile mockFile(FileType fileType, State state) {
		PsiFile file = mock(PsiFile.class);
		given(file.getFileType()).willReturn(fileType);
		Project project = mock(Project.class);
		given(project.getUserData(any())).willReturn(state);
		given(file.getProject()).willReturn(project);
		return file;
	}

}
