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

package io.spring.format.formatter.intellij.codestyle;

import java.util.Collection;

import com.intellij.formatting.FormattingMode;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.ChangedRangesInfo;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.DocCommentSettings;
import com.intellij.psi.codeStyle.FormattingModeAwareIndentAdjuster;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ThrowableRunnable;

/**
 * {@link CodeStyleManager} implementation that delegates all calls.
 *
 * @author Phillip Webb
 */
public class DelegatingCodeStyleManager extends CodeStyleManager implements FormattingModeAwareIndentAdjuster {

	private final CodeStyleManager delegate;

	public DelegatingCodeStyleManager(CodeStyleManager delegate) {
		this.delegate = delegate;
	}

	public CodeStyleManager getDelegate() {
		return this.delegate;
	}

	@Override
	public int getSpacing(PsiFile file, int offset) {
		return this.delegate.getSpacing(file, offset);
	}

	@Override
	public int getMinLineFeeds(PsiFile file, int offset) {
		return this.delegate.getMinLineFeeds(file, offset);
	}

	@Override
	public void runWithDocCommentFormattingDisabled(PsiFile file, Runnable runnable) {
		this.delegate.runWithDocCommentFormattingDisabled(file, runnable);
	}

	@Override
	public DocCommentSettings getDocCommentSettings(PsiFile file) {
		return this.delegate.getDocCommentSettings(file);
	}

	@Override
	public Project getProject() {
		return this.delegate.getProject();
	}

	@Override
	public PsiElement reformat(PsiElement element) throws IncorrectOperationException {
		return this.delegate.reformat(element);
	}

	@Override
	public PsiElement reformat(PsiElement element, boolean canChangeWhiteSpacesOnly)
			throws IncorrectOperationException {
		return this.delegate.reformat(element, canChangeWhiteSpacesOnly);
	}

	@Override
	public PsiElement reformatRange(PsiElement element, int startOffset, int endOffset)
			throws IncorrectOperationException {
		return this.delegate.reformatRange(element, startOffset, endOffset);
	}

	@Override
	public PsiElement reformatRange(PsiElement element, int startOffset, int endOffset,
			boolean canChangeWhiteSpacesOnly) throws IncorrectOperationException {
		return this.delegate.reformatRange(element, startOffset, endOffset, canChangeWhiteSpacesOnly);
	}

	@Override
	public void reformatText(PsiFile file, int startOffset, int endOffset) throws IncorrectOperationException {
		this.delegate.reformatText(file, startOffset, endOffset);
	}

	@Override
	public void reformatText(PsiFile file, Collection<TextRange> ranges) throws IncorrectOperationException {
		this.delegate.reformatText(file, ranges);
	}

	@Override
	public void reformatTextWithContext(PsiFile file, Collection<TextRange> ranges) throws IncorrectOperationException {
		this.delegate.reformatTextWithContext(file, ranges);
	}

	@Override
	public void reformatTextWithContext(PsiFile file, ChangedRangesInfo info) throws IncorrectOperationException {
		this.delegate.reformatTextWithContext(file, info);
	}

	@Override
	public void adjustLineIndent(PsiFile file, TextRange rangeToAdjust) throws IncorrectOperationException {
		this.delegate.adjustLineIndent(file, rangeToAdjust);
	}

	@Override
	public int adjustLineIndent(PsiFile file, int offset) throws IncorrectOperationException {
		return this.delegate.adjustLineIndent(file, offset);
	}

	@Override
	public int adjustLineIndent(Document document, int offset) {
		return this.delegate.adjustLineIndent(document, offset);
	}

	@Override
	@Deprecated
	public boolean isLineToBeIndented(PsiFile file, int offset) {
		return this.delegate.isLineToBeIndented(file, offset);
	}

	@Override
	public String getLineIndent(PsiFile file, int offset) {
		return this.delegate.getLineIndent(file, offset);
	}

	@Override
	public String getLineIndent(Document document, int offset) {
		return this.delegate.getLineIndent(document, offset);
	}

	@Override
	public String getLineIndent(PsiFile file, int offset, FormattingMode mode) {
		return this.delegate.getLineIndent(file, offset, mode);
	}

	@Override
	@Deprecated
	public com.intellij.psi.codeStyle.Indent getIndent(String text, FileType fileType) {
		return this.delegate.getIndent(text, fileType);
	}

	@Override
	@Deprecated
	public String fillIndent(com.intellij.psi.codeStyle.Indent indent, FileType fileType) {
		return this.delegate.fillIndent(indent, fileType);
	}

	@Override
	@Deprecated
	public com.intellij.psi.codeStyle.Indent zeroIndent() {
		return this.delegate.zeroIndent();
	}

	@Override
	public void reformatNewlyAddedElement(ASTNode block, ASTNode addedElement) throws IncorrectOperationException {
		this.delegate.reformatNewlyAddedElement(block, addedElement);
	}

	@Override
	public boolean isSequentialProcessingAllowed() {
		return this.delegate.isSequentialProcessingAllowed();
	}

	@Override
	public void performActionWithFormatterDisabled(Runnable r) {
		this.delegate.performActionWithFormatterDisabled(r);
	}

	@Override
	public <T extends Throwable> void performActionWithFormatterDisabled(ThrowableRunnable<T> r) throws T {
		this.delegate.performActionWithFormatterDisabled(r);
	}

	@Override
	public <T> T performActionWithFormatterDisabled(Computable<T> r) {
		return this.delegate.performActionWithFormatterDisabled(r);
	}

	@Override
	public int adjustLineIndent(Document document, int offset, FormattingMode mode) {
		if (this.delegate instanceof FormattingModeAwareIndentAdjuster) {
			return ((FormattingModeAwareIndentAdjuster) this.delegate).adjustLineIndent(document, offset, mode);
		}
		return offset;
	}

	@Override
	public FormattingMode getCurrentFormattingMode() {
		if (this.delegate instanceof FormattingModeAwareIndentAdjuster) {
			return ((FormattingModeAwareIndentAdjuster) this.delegate).getCurrentFormattingMode();
		}
		return FormattingMode.REFORMAT;
	}

	@Override
	public void scheduleIndentAdjustment(Document document, int offset) {
		this.delegate.scheduleIndentAdjustment(document, offset);
	}

	@Override
	public void scheduleReformatWhenSettingsComputed(PsiFile file) {
		this.delegate.scheduleReformatWhenSettingsComputed(file);
	}

}
