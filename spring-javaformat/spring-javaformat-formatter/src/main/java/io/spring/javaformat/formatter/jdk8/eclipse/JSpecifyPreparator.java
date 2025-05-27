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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.spring.javaformat.eclipse.jdt.jdk8.core.dom.ASTNode;
import io.spring.javaformat.eclipse.jdt.jdk8.core.dom.ASTVisitor;
import io.spring.javaformat.eclipse.jdt.jdk8.core.dom.Annotation;
import io.spring.javaformat.eclipse.jdt.jdk8.core.dom.CompilationUnit;
import io.spring.javaformat.eclipse.jdt.jdk8.core.dom.IExtendedModifier;
import io.spring.javaformat.eclipse.jdt.jdk8.core.dom.ImportDeclaration;
import io.spring.javaformat.eclipse.jdt.jdk8.core.dom.MethodDeclaration;
import io.spring.javaformat.eclipse.jdt.jdk8.core.dom.SingleVariableDeclaration;
import io.spring.javaformat.eclipse.jdt.jdk8.core.dom.VariableDeclarationStatement;
import io.spring.javaformat.eclipse.jdt.jdk8.core.formatter.CodeFormatter;
import io.spring.javaformat.eclipse.jdt.jdk8.internal.formatter.Preparator;
import io.spring.javaformat.eclipse.jdt.jdk8.internal.formatter.TokenManager;

public class JSpecifyPreparator implements Preparator {

	private static final String PACKAGE_NAME = "org.jspecify.annotations";

	private static final Set<String> ANNOTATION_NAMES = new HashSet<>(
			Arrays.asList("NonNull", "Nullable", "NullMarked", "NullUnmarked"));

	private static final Set<String> FULLY_QUALIFIED_ANNOTATION_NAMES = ANNOTATION_NAMES.stream()
		.map((annotationName) -> PACKAGE_NAME + "." + annotationName)
		.collect(Collectors.toSet());

	@Override
	public void apply(int kind, TokenManager tokenManager, ASTNode astRoot) {
		if ((kind & CodeFormatter.K_COMPILATION_UNIT) != 0) {
			ASTVisitor visitor = new Vistor(tokenManager);
			astRoot.accept(visitor);
		}
	}

	private static class Vistor extends ASTVisitor {

		private final TokenManager tokenManager;

		private final Map<String, String> fullyQualified = new HashMap<>();

		Vistor(TokenManager tokenManager) {
			this.tokenManager = tokenManager;
		}

		@Override
		public boolean visit(CompilationUnit node) {
			this.fullyQualified.clear();
			return super.visit(node);
		}

		@Override
		public boolean visit(ImportDeclaration node) {
			String name = node.getName().toString();
			if (name.equals(PACKAGE_NAME) || name.startsWith(PACKAGE_NAME)) {
				Set<String> annotationNames = (node.isOnDemand()) ? ANNOTATION_NAMES
						: Collections.singleton(name.substring(name.lastIndexOf(".") + 1));
				for (String annotationName : annotationNames) {
					this.fullyQualified.put(annotationName, PACKAGE_NAME + "." + annotationName);
				}
			}
			return super.visit(node);
		}

		@Override
		public boolean visit(MethodDeclaration node) {
			clearLineBreaksIfHasJSpecifyAnnotation(node.modifiers());
			return true;
		}

		@Override
		public boolean visit(VariableDeclarationStatement node) {
			clearLineBreaksIfHasJSpecifyAnnotation(node.modifiers());
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

		@SuppressWarnings("unchecked")
		private void clearLineBreaksIfHasJSpecifyAnnotation(List<?> modifiers) {
			Annotation lastAnnotation = getLastAnnotation((List<IExtendedModifier>) modifiers);
			if (isJSpecifyAnnotation(lastAnnotation)) {
				this.tokenManager.lastTokenIn(lastAnnotation, -1).clearLineBreaksAfter();
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
			String fullyQualifiedName = (annotation != null)
					? this.fullyQualified.get(annotation.getTypeName().toString()) : null;
			return (fullyQualifiedName != null) && FULLY_QUALIFIED_ANNOTATION_NAMES.contains(fullyQualifiedName);
		}

	}

}
