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

package io.spring.javaformat.checkstyle.check;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.checks.annotation.AnnotationLocationCheck;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Spring variant of {@link AnnotationLocationCheck}.
 *
 * @author Phillip Webb
 */
public class SpringAnnotationLocationCheck extends AbstractCheck {

	private static final Set<String> JSPECIFY_ANNOTATION_NAMES = new HashSet<>(
			Arrays.asList("NonNull", "Nullable", "NullMarked", "NullUnmarked"));

	@Override
	public int[] getDefaultTokens() {
		return new int[] { TokenTypes.CLASS_DEF, TokenTypes.INTERFACE_DEF, TokenTypes.PACKAGE_DEF,
				TokenTypes.ENUM_CONSTANT_DEF, TokenTypes.ENUM_DEF, TokenTypes.METHOD_DEF, TokenTypes.CTOR_DEF,
				TokenTypes.VARIABLE_DEF, TokenTypes.RECORD_DEF, TokenTypes.COMPACT_CTOR_DEF, };
	}

	@Override
	public int[] getAcceptableTokens() {
		return new int[] { TokenTypes.CLASS_DEF, TokenTypes.INTERFACE_DEF, TokenTypes.PACKAGE_DEF,
				TokenTypes.ENUM_CONSTANT_DEF, TokenTypes.ENUM_DEF, TokenTypes.METHOD_DEF, TokenTypes.CTOR_DEF,
				TokenTypes.VARIABLE_DEF, TokenTypes.ANNOTATION_DEF, TokenTypes.ANNOTATION_FIELD_DEF,
				TokenTypes.RECORD_DEF, TokenTypes.COMPACT_CTOR_DEF, };
	}

	@Override
	public int[] getRequiredTokens() {
		return CommonUtil.EMPTY_INT_ARRAY;
	}

	@Override
	public void visitToken(DetailAST ast) {
		if (ast.getType() != TokenTypes.VARIABLE_DEF || ast.getParent().getType() == TokenTypes.OBJBLOCK) {
			DetailAST node = ast.findFirstToken(TokenTypes.MODIFIERS);
			node = (node != null) ? node : ast.findFirstToken(TokenTypes.ANNOTATIONS);
			checkAnnotations(node, getExpectedAnnotationIndentation(node));
		}
	}

	private int getExpectedAnnotationIndentation(DetailAST node) {
		return node.getColumnNo();
	}

	private void checkAnnotations(DetailAST node, int correctIndentation) {
		DetailAST annotation = node.getFirstChild();
		while (annotation != null && annotation.getType() == TokenTypes.ANNOTATION) {
			checkAnnotation(correctIndentation, annotation);
			annotation = annotation.getNextSibling();
		}
	}

	private void checkAnnotation(int correctIndentation, DetailAST annotation) {
		String annotationName = getAnnotationName(annotation);
		if (!isCorrectLocation(annotation) && !isJSpecifyAnnotation(annotationName)) {
			log(annotation, AnnotationLocationCheck.MSG_KEY_ANNOTATION_LOCATION_ALONE, annotationName);
		}
		else if (annotation.getColumnNo() != correctIndentation && !hasNodeBefore(annotation)) {
			log(annotation, AnnotationLocationCheck.MSG_KEY_ANNOTATION_LOCATION, annotationName,
					annotation.getColumnNo(), correctIndentation);
		}
	}

	private String getAnnotationName(DetailAST annotation) {
		DetailAST identNode = annotation.findFirstToken(TokenTypes.IDENT);
		if (identNode == null) {
			identNode = annotation.findFirstToken(TokenTypes.DOT).findFirstToken(TokenTypes.IDENT);
		}
		return identNode.getText();
	}

	private boolean isCorrectLocation(DetailAST annotation) {
		return !hasNodeBeside(annotation);
	}

	private boolean hasNodeBeside(DetailAST annotation) {
		return hasNodeBefore(annotation) || hasNodeAfter(annotation);
	}

	private boolean hasNodeBefore(DetailAST annotation) {
		int annotationLineNo = annotation.getLineNo();
		DetailAST previousNode = annotation.getPreviousSibling();
		return (previousNode != null) && (annotationLineNo == previousNode.getLineNo());
	}

	private boolean hasNodeAfter(DetailAST annotation) {
		int annotationLineNo = annotation.getLineNo();
		DetailAST nextNode = annotation.getNextSibling();
		nextNode = (nextNode != null) ? nextNode : annotation.getParent().getNextSibling();
		return annotationLineNo == nextNode.getLineNo();
	}

	private boolean isJSpecifyAnnotation(String annotationName) {
		return JSPECIFY_ANNOTATION_NAMES.contains(annotationName);
	}

}
