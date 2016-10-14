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
import java.util.function.Supplier;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiBundle;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import io.spring.javaformat.formatter.Formatter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.TextEdit;

/**
 * Reformatter used {@link SpringCodeStyleManager} to determine when formatting can apply
 * and to perform the actual formatting.
 *
 * @author Phillip Webb
 */
class SpringReformatter {

	private final Supplier<Project> project;

	private final Supplier<Application> application;

	private final Supplier<PsiDocumentManager> documentManager;

	SpringReformatter(Supplier<Project> project) {
		this.project = project;
		this.application = () -> ApplicationManager.getApplication();
		this.documentManager = () -> PsiDocumentManager.getInstance(project.get());
	}

	SpringReformatter(Supplier<Project> project, Supplier<Application> application,
			Supplier<PsiDocumentManager> documentManager) {
		this.project = project;
		this.application = application;
		this.documentManager = documentManager;
	}

	public boolean canReformat(PsiFile file) {
		return StdFileTypes.JAVA.equals(file.getFileType());
	}

	public void reformat(PsiFile file, Collection<TextRange> ranges) {
		this.application.get().assertWriteAccessAllowed();
		this.documentManager.get().commitAllDocuments();
		if (!file.isWritable()) {
			throwNotWritableException(file);
		}
		reformat(file, ranges, this.documentManager.get().getDocument(file));
	}

	private void throwNotWritableException(PsiElement element)
			throws IncorrectOperationException {
		if (element instanceof PsiDirectory) {
			String url = ((PsiDirectory) element).getVirtualFile().getPresentableUrl();
			throw new IncorrectOperationException(
					PsiBundle.message("cannot.modify.a.read.only.directory", url));
		}
		PsiFile file = element.getContainingFile();
		if (file == null) {
			throw new IncorrectOperationException();
		}
		VirtualFile virtualFile = file.getVirtualFile();
		if (virtualFile == null) {
			throw new IncorrectOperationException();
		}
		throw new IncorrectOperationException(PsiBundle.message(
				"cannot.modify.a.read.only.file", virtualFile.getPresentableUrl()));
	}

	private void reformat(PsiFile file, Collection<TextRange> ranges, Document document) {
		if (document != null) {
			Formatter formatter = new Formatter();
			String source = document.getText();
			IRegion[] regions = EclipseRegionAdapter.asArray(ranges);
			TextEdit edit = formatter.format(source, regions);
			applyEdit(document, edit);
		}
	}

	private void applyEdit(Document document, TextEdit textEdit) {
		runWriteCommandAction(() -> {
			try {
				EclipseDocumentAdapter adapter = new EclipseDocumentAdapter(document);
				textEdit.apply(adapter);
				this.documentManager.get().commitDocument(document);
			}
			catch (Exception ex) {
				throw new IllegalStateException(ex);
			}
		});
	}

	protected void runWriteCommandAction(Runnable runnable) {
		WriteCommandAction.runWriteCommandAction(this.project.get(), runnable);
	}

}
