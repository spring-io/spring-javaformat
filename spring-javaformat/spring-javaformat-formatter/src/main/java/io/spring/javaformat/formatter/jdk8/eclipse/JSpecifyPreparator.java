/*
 * Copyright 2017-2025 the original author or authors.
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

package io.spring.javaformat.formatter.jdk8.eclipse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.spring.javaformat.eclipse.jdt.jdk8.core.dom.ASTNode;
import io.spring.javaformat.eclipse.jdt.jdk8.core.dom.ASTVisitor;
import io.spring.javaformat.eclipse.jdt.jdk8.core.dom.Annotation;
import io.spring.javaformat.eclipse.jdt.jdk8.core.dom.IExtendedModifier;
import io.spring.javaformat.eclipse.jdt.jdk8.core.dom.MethodDeclaration;
import io.spring.javaformat.eclipse.jdt.jdk8.core.dom.SingleVariableDeclaration;
import io.spring.javaformat.eclipse.jdt.jdk8.core.formatter.CodeFormatter;
import io.spring.javaformat.eclipse.jdt.jdk8.internal.formatter.Preparator;
import io.spring.javaformat.eclipse.jdt.jdk8.internal.formatter.TokenManager;

public class JSpecifyPreparator implements Preparator {

	private static final Set<String> ANNOTATION_NAMES = new HashSet<>(
			Arrays.asList("NonNull", "Nullable", "NullMarked", "NullUnmarked"));

	@Override
	public void apply(int kind, TokenManager tokenManager, ASTNode astRoot) {
		if ((kind & CodeFormatter.K_COMPILATION_UNIT) != 0) {
			ASTVisitor visitor = new Vistor(tokenManager);
			astRoot.accept(visitor);
		}
	}

	private static class Vistor extends ASTVisitor {

		private final TokenManager tokenManager;

		Vistor(TokenManager tokenManager) {
			this.tokenManager = tokenManager;
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean visit(MethodDeclaration node) {
			Annotation lastAnnotation = getLastAnnotation((List<IExtendedModifier>) node.modifiers());
			if (isJSpecifyAnnotation(lastAnnotation)) {
				this.tokenManager.lastTokenIn(lastAnnotation, -1).clearLineBreaksAfter();
			}
			return true;
		}

		@Override
		@SuppressWarnings("unchecked")
		public void endVisit(SingleVariableDeclaration node) {
			if (node.isVarargs()) {
				List<Annotation> annotations = node.varargsAnnotations();
				Annotation lastAnnotation = getLastAnnotation(annotations);
				if (isJSpecifyAnnotation(lastAnnotation)) {
					this.tokenManager.lastTokenIn(lastAnnotation, -1).spaceAfter();
				}
			}
		}

		private Annotation getLastAnnotation(List<? extends IExtendedModifier> modifiers) {
			Annotation annotation = null;
			for (IExtendedModifier modifier : modifiers) {
				if (!modifier.isAnnotation()) {
					return annotation;
				}
				annotation = (Annotation) modifier;
			}
			return annotation;
		}

		private boolean isJSpecifyAnnotation(Annotation annotation) {
			return (annotation != null) && ANNOTATION_NAMES.contains(annotation.getTypeName().toString());
		}

	}

}
