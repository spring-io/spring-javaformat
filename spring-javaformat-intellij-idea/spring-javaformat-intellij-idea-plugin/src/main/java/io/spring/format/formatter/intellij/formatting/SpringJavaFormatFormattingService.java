/*
 * Copyright 2017-present the original author or authors.
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

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import com.intellij.formatting.FormattingContext;
import com.intellij.formatting.service.AbstractDocumentFormattingService;
import com.intellij.formatting.service.FormattingService;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.TextEdit;
import org.jetbrains.annotations.NotNull;

import io.spring.format.formatter.intellij.state.State;
import io.spring.javaformat.config.JavaFormatConfig;
import io.spring.javaformat.formatter.Formatter;

/**
 * {@link FormattingService} to apply Spring formatting conventions.
 *
 * @author Phillip Webb
 */
public class SpringJavaFormatFormattingService extends AbstractDocumentFormattingService {

	private static final String NORMALIZED_LINE_SEPARATOR = "\n";

	private static final Set<Feature> FEATURES = Set.of(Feature.FORMAT_FRAGMENTS);

	private static final FileType JAVA_FILE_TYPE = FileTypeManager.getInstance().getStdFileType("JAVA");

	private final BiConsumer<Project, Runnable> runAction;

	public SpringJavaFormatFormattingService() {
		this(WriteCommandAction::runWriteCommandAction);
	}

	SpringJavaFormatFormattingService(BiConsumer<Project, Runnable> runAction) {
		this.runAction = runAction;
	}

	@Override
	public @NotNull Set<Feature> getFeatures() {
		return FEATURES;
	}

	@Override
	public boolean canFormat(@NotNull PsiFile file) {
		return JAVA_FILE_TYPE.equals(file.getFileType()) && State.get(file.getProject()) == State.ACTIVE;
	}

	@Override
	public void formatDocument(@NotNull Document document, @NotNull List<TextRange> formattingRanges,
			@NotNull FormattingContext formattingContext, boolean canChangeWhiteSpaceOnly, boolean quickFormat) {
		VirtualFile file = formattingContext.getVirtualFile();
		Path path = (file != null) ? file.toNioPath() : null;
		JavaFormatConfig config = JavaFormatConfig.findFrom(path);
		Formatter formatter = new Formatter(config);
		String source = document.getText();
		formattingRanges = (!formattingRanges.isEmpty()) ? formattingRanges : List.of(TextRange.allOf(source));
		IRegion[] regions = EclipseRegionAdapter.asArray(formattingRanges);
		TextEdit edit = formatter.format(source, regions, NORMALIZED_LINE_SEPARATOR);
		applyEdit(formattingContext.getProject(), document, edit);
	}

	private void applyEdit(Project project, Document document, TextEdit textEdit) {
		this.runAction.accept(project, () -> {
			try {
				IDocument adapted = new EclipseDocumentAdapter(document);
				textEdit.apply(adapted);
			}
			catch (Exception ex) {
				throw new IllegalStateException(ex);
			}
		});
	}

}
