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

package io.spring.javaformat.formatter.preparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.spring.javaformat.formatter.eclipse.Preparator;
import io.spring.javaformat.formatter.eclipse.Token;
import io.spring.javaformat.formatter.eclipse.TokenManager;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;

/**
 * {@link Preparator} to fine tune Javadoc whitespace.
 *
 * @author Phillip Webb
 */
class JavadocLineBreakPreparator implements Preparator {

	private final static List<String> PARAM_TAGS;

	static {
		List<String> paramTags = new ArrayList<String>();
		paramTags.add(TagElement.TAG_PARAM);
		paramTags.add(TagElement.TAG_EXCEPTION);
		paramTags.add(TagElement.TAG_SERIALFIELD);
		paramTags.add(TagElement.TAG_THROWS);
		paramTags.add(TagElement.TAG_RETURN);
		paramTags.add(TagElement.TAG_DEPRECATED);
		PARAM_TAGS = Collections.unmodifiableList(paramTags);
	}

	@Override
	public void apply(TokenManager tokenManager, ASTNode astRoot) {
		ASTVisitor visitor = new Vistor(tokenManager);
		for (Comment comment : getComments(astRoot)) {
			comment.accept(visitor);
		}
	}

	@SuppressWarnings("unchecked")
	private List<Comment> getComments(ASTNode astRoot) {
		if (astRoot.getRoot() instanceof CompilationUnit) {
			CompilationUnit compilationUnit = (CompilationUnit) astRoot.getRoot();
			return compilationUnit.getCommentList();
		}
		return Collections.emptyList();
	}

	private static class Vistor extends ASTVisitor {

		private final TokenManager tokenManager;

		private TokenManager commentTokenManager;

		private ASTNode declaration;

		private boolean firstTagElement;

		private boolean hasText;

		Vistor(TokenManager tokenManager) {
			this.tokenManager = tokenManager;
		}

		@Override
		public boolean visit(Javadoc node) {
			int commentIndex = this.tokenManager.firstIndexIn(node,
					TerminalTokens.TokenNameCOMMENT_JAVADOC);
			Token commentToken = this.tokenManager.get(commentIndex);
			this.commentTokenManager = new TokenManager(
					commentToken.getInternalStructure(), this.tokenManager);
			this.declaration = node.getParent();
			this.firstTagElement = true;
			this.hasText = false;
			return true;
		}

		@Override
		public boolean visit(TextElement node) {
			this.hasText = true;
			return true;
		}

		@Override
		public boolean visit(TagElement node) {
			if (isSquashRequired(node, this.declaration)) {
				int startIndex = this.commentTokenManager
						.findIndex(node.getStartPosition(), -1, false);
				Token token = this.commentTokenManager.get(startIndex);
				token.clearLineBreaksBefore();
				token.putLineBreaksBefore(this.declaration instanceof TypeDeclaration
						&& this.firstTagElement && this.hasText ? 2 : 1);
				this.firstTagElement = false;
			}
			return true;
		}

		private boolean isSquashRequired(TagElement node, ASTNode declaration) {
			if (declaration instanceof TypeDeclaration) {
				String tagName = node.getTagName();
				return (!node.isNested() && tagName != null && tagName.startsWith("@"));
			}
			return PARAM_TAGS.contains(node.getTagName());
		}

	}

}
