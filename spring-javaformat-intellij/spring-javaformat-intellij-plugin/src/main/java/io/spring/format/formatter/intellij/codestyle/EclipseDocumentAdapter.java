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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.jetbrains.annotations.NotNull;

/**
 * Adapter class to expose an IntelliJ {@link com.intellij.openapi.editor.Document} as an
 * Eclipse {@link org.eclipse.jface.text.Document}.
 *
 * @author Phillip Webb
 */
class EclipseDocumentAdapter extends Document {

	private final com.intellij.openapi.editor.Document intellijDocument;

	EclipseDocumentAdapter(
			@NotNull com.intellij.openapi.editor.Document intellijDocument) {
		super(intellijDocument.getText());
		this.intellijDocument = intellijDocument;
	}

	@Override
	public void replace(int pos, int length, String text, long modificationStamp)
			throws BadLocationException {
		super.replace(pos, length, text, modificationStamp);
		this.intellijDocument.replaceString(pos, pos + length, text);
	}

}
