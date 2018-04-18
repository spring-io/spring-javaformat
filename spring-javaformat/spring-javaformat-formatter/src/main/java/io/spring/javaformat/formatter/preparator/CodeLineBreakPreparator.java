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

import io.spring.javaformat.formatter.eclipse.Preparator;
import io.spring.javaformat.formatter.eclipse.Token;
import io.spring.javaformat.formatter.eclipse.TokenManager;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;

/**
 * {@link Preparator} to fine tune curly-brace line breaks.
 *
 * @author Phillip Webb
 */
class CodeLineBreakPreparator implements Preparator {

	@Override
	public void apply(TokenManager tokenManager, ASTNode astRoot) {
		ASTVisitor visitor = new Vistor(tokenManager);
		astRoot.accept(visitor);
	}

	private static class Vistor extends ASTVisitor {

		private final TokenManager tokenManager;

		Vistor(TokenManager tokenManager) {
			this.tokenManager = tokenManager;
		}

		@Override
		public boolean visit(TypeDeclaration node) {
			visitType(node);
			return true;
		}

		@Override
		public boolean visit(AnnotationTypeDeclaration node) {
			visitType(node);
			return true;
		}

		@Override
		public boolean visit(EnumDeclaration node) {
			visitType(node);
			return true;
		}

		private void visitType(AbstractTypeDeclaration node) {
			SimpleName name = node.getName();
			int openBraceIndex = (name == null
					? this.tokenManager.firstIndexIn(node, TerminalTokens.TokenNameLBRACE)
					: this.tokenManager.firstIndexAfter(name,
							TerminalTokens.TokenNameLBRACE));
			Token openBraceToken = this.tokenManager.get(openBraceIndex);
			openBraceToken.clearLineBreaksAfter();
			openBraceToken.putLineBreaksAfter(2);
			int closeBraceIndex = this.tokenManager.lastIndexIn(node,
					TerminalTokens.TokenNameRBRACE);
			Token closeBraceToken = this.tokenManager.get(closeBraceIndex);
			closeBraceToken.clearLineBreaksBefore();
			closeBraceToken.putLineBreaksBefore(2);
		}

		@Override
		public boolean visit(FieldDeclaration node) {
			int index = this.tokenManager.lastIndexIn(node,
					TerminalTokens.TokenNameSEMICOLON);
			while (tokenIsOfType(index + 1, TerminalTokens.TokenNameCOMMENT_LINE,
					TerminalTokens.TokenNameCOMMENT_BLOCK)) {
				if (this.tokenManager.get(index).getLineBreaksAfter() > 0
						|| this.tokenManager.get(index + 1).getLineBreaksBefore() > 0) {
					break;
				}
				index++;
			}
			Token token = this.tokenManager.get(index);
			if (tokenIsOfType(index + 1, TerminalTokens.TokenNamestatic)) {
				return true;
			}
			token.clearLineBreaksAfter();
			token.putLineBreaksAfter(2);
			return true;
		}

		private boolean tokenIsOfType(int index, int... types) {
			if (index < this.tokenManager.size()) {
				Token token = this.tokenManager.get(index);
				for (int type : types) {
					if (token.tokenType == type) {
						return true;
					}
				}
			}
			return false;
		}

	}

}
