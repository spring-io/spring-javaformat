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

import java.util.Collection;
import java.util.Collections;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.ChangedRangesInfo;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.ThrowableRunnable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link DelegatingCodeStyleManager}.
 *
 * @author Phillip Webb
 */
public class DelegatingCodeStyleManagerTests {

	@Mock
	private CodeStyleManager delegate;

	private DelegatingCodeStyleManager delegating;

	@Mock
	private PsiElement element;

	@Mock
	private PsiFile file;

	@Mock
	private TextRange range;

	private Collection<TextRange> ranges;

	@Mock
	private Document document;

	@Mock
	private FileType fileType;

	@Mock
	private ASTNode block;

	@Mock
	private ASTNode node;

	@Mock
	private ChangedRangesInfo changedRangesInfo;

	@Before
	@Test
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.delegating = new DelegatingCodeStyleManager(this.delegate);
		this.ranges = Collections.singleton(mock(TextRange.class));
	}

	@Test
	public void getDelegateShouldGetDelegate() throws Exception {
		assertThat(this.delegating.getDelegate()).isEqualTo(this.delegate);
	}

	@Test
	public void getProjectShouldCallDelegate() throws Exception {
		this.delegating.getProject();
		verify(this.delegate).getProject();
	}

	@Test
	public void reformatShouldCallDelegate() throws Exception {
		this.delegating.reformat(this.element);
		verify(this.delegate).reformat(this.element);
	}

	@Test
	public void reformatWithCanChangeWhiteSpacesOnlyShouldCallDelegate()
			throws Exception {
		this.delegating.reformat(this.element, true);
		verify(this.delegate).reformat(this.element, true);
	}

	@Test
	public void reformatRangeShouldCallDelegate() throws Exception {
		this.delegating.reformatRange(this.element, 12, 34);
		verify(this.delegate).reformatRange(this.element, 12, 34);
	}

	@Test
	public void reformatRangeWithCanChangeWhiteSpacesOnlyShouldCallDelegate()
			throws Exception {
		this.delegating.reformatRange(this.element, 12, 34, true);
		verify(this.delegate).reformatRange(this.element, 12, 34, true);
	}

	@Test
	public void reformatTextShouldCallDelegate() throws Exception {
		this.delegating.reformatText(this.file, 12, 34);
		verify(this.delegate).reformatText(this.file, 12, 34);
	}

	@Test
	public void reformatTextWithRangeCollectionShouldCallDelegate() throws Exception {
		this.delegating.reformatText(this.file, this.ranges);
		verify(this.delegate).reformatText(this.file, this.ranges);
	}

	@Test
	public void reformatTextWithContextShouldCallDelegate() throws Exception {
		this.delegating.reformatTextWithContext(this.file, this.ranges);
		verify(this.delegate).reformatTextWithContext(this.file, this.ranges);
	}

	@Test
	public void reformatTextWithContextInfoShouldCallDelegate() throws Exception {
		this.delegating.reformatTextWithContext(this.file, this.changedRangesInfo);
		verify(this.delegate).reformatTextWithContext(this.file, this.changedRangesInfo);
	}

	@Test
	public void adjustLineIndentForFileWithRangeShouldCallDelegate() throws Exception {
		this.delegating.adjustLineIndent(this.file, this.range);
		verify(this.delegate).adjustLineIndent(this.file, this.range);
	}

	@Test
	public void adjustLineIndentForFileShouldCallDelegate() throws Exception {
		this.delegating.adjustLineIndent(this.file, 123);
		verify(this.delegate).adjustLineIndent(this.file, 123);
	}

	@Test
	public void adjustLineIndentForDocumentShouldCallDelegate() throws Exception {
		this.delegating.adjustLineIndent(this.document, 123);
		verify(this.delegate).adjustLineIndent(this.document, 123);
	}

	@Test
	@Deprecated
	public void isLineToBeIndentedShouldCallDelegate() throws Exception {
		this.delegating.isLineToBeIndented(this.file, 123);
		verify(this.delegate).isLineToBeIndented(this.file, 123);
	}

	@Test
	public void getLineIndentForFileShouldCallDelegate() throws Exception {
		this.delegating.getLineIndent(this.file, 123);
		verify(this.delegate).getLineIndent(this.file, 123);
	}

	@Test
	public void getLineIndentForDocumentShouldCallDelegate() throws Exception {
		this.delegating.getLineIndent(this.document, 123);
		verify(this.delegate).getLineIndent(this.document, 123);
	}

	@Test
	@Deprecated
	public void getIndentShouldCallDelegate() throws Exception {
		this.delegating.getIndent("hello", this.fileType);
		verify(this.delegate).getIndent("hello", this.fileType);
	}

	@Test
	@Deprecated
	public void fillIndentShouldCallDelegate() throws Exception {
		com.intellij.psi.codeStyle.Indent indent = mock(
				com.intellij.psi.codeStyle.Indent.class);
		this.delegating.fillIndent(indent, this.fileType);
		verify(this.delegate).fillIndent(indent, this.fileType);
	}

	@Test
	@Deprecated
	public void zeroIndentShouldCallDelegate() throws Exception {
		this.delegating.zeroIndent();
		verify(this.delegate).zeroIndent();
	}

	@Test
	public void reformatNewlyAddedElementShouldCallDelegate() throws Exception {
		this.delegating.reformatNewlyAddedElement(this.block, this.node);
		verify(this.delegate).reformatNewlyAddedElement(this.block, this.node);
	}

	@Test
	public void isSequentialProcessingAllowedShouldCallDelegate() throws Exception {
		this.delegating.isSequentialProcessingAllowed();
		verify(this.delegate).isSequentialProcessingAllowed();
	}

	@Test
	public void performActionWithFormatterDisabledWithRunnableShouldCallDelegate()
			throws Exception {
		Runnable runnable = mock(Runnable.class);
		this.delegating.performActionWithFormatterDisabled(runnable);
		verify(this.delegate).performActionWithFormatterDisabled(runnable);
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void performActionWithFormatterDisabledWithThrowableRunnableShouldCallDelegate()
			throws Throwable {
		ThrowableRunnable runnable = mock(ThrowableRunnable.class);
		this.delegating.performActionWithFormatterDisabled(runnable);
		verify(this.delegate).performActionWithFormatterDisabled(runnable);
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void performActionWithFormatterDisabledWithComputableShouldCallDelegate()
			throws Exception {
		Computable computable = mock(Computable.class);
		this.delegating.performActionWithFormatterDisabled(computable);
		verify(this.delegate).performActionWithFormatterDisabled(computable);
	}

}
