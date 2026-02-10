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

package io.spring.javaformat.checkstyle.check;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Check for compliance with Spring-style method visibility. Checks that:
 *
 * <ul>
 * <li>package-private and private classes do not have public methods
 * unless they are also annotated with {@link Override @Override}
 * <li>final classes do not have protected methods unless that are also
 * annotated with {@link Override @Override}
 * </ul>
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 */
public class SpringMethodVisibilityCheck extends AbstractSpringCheck {

	@Override
	public int[] getAcceptableTokens() {
		return new int[] { TokenTypes.METHOD_DEF };
	}

	@Override
	public void visitToken(DetailAST ast) {
		DetailAST modifiers = ast.findFirstToken(TokenTypes.MODIFIERS);
		if (modifiers.findFirstToken(TokenTypes.LITERAL_PUBLIC) != null) {
			visitPublicMethod(modifiers, ast);
		}
		else if (modifiers.findFirstToken(TokenTypes.LITERAL_PROTECTED) != null) {
			visitProtectedMethod(modifiers, ast);
		}
	}

	private void visitPublicMethod(DetailAST modifiers, DetailAST method) {
		if (hasOverrideAnnotation(modifiers)) {
			return;
		}
		DetailAST classDef = getClassDef(method.getParent());
		if (classDef == null || isPublicOrProtected(classDef)) {
			return;
		}
		DetailAST interfaceDef = getInterfaceDef(classDef.getParent());
		if (interfaceDef != null && isPublicOrProtected(interfaceDef)) {
			return;
		}
		DetailAST ident = method.findFirstToken(TokenTypes.IDENT);
		log(ident.getLineNo(), ident.getColumnNo(), "methodvisibility.publicMethod", ident.getText());
	}

	private void visitProtectedMethod(DetailAST modifiers, DetailAST method) {
		if (hasOverrideAnnotation(modifiers)) {
			return;
		}
		DetailAST classDef = getClassDef(method.getParent());
		if (classDef == null || !isFinal(classDef)) {
			return;
		}
		DetailAST ident = method.findFirstToken(TokenTypes.IDENT);
		log(ident.getLineNo(), ident.getColumnNo(), "methodvisibility.protectedMethodInFinalClass", ident.getText());
	}

	private boolean hasOverrideAnnotation(DetailAST modifiers) {
		DetailAST candidate = modifiers.getFirstChild();
		while (candidate != null) {
			if (candidate.getType() == TokenTypes.ANNOTATION) {
				DetailAST dot = candidate.findFirstToken(TokenTypes.DOT);
				String name = (dot != null ? dot : candidate).findFirstToken(TokenTypes.IDENT).getText();
				if ("Override".equals(name)) {
					return true;
				}
			}
			candidate = candidate.getNextSibling();
		}
		return false;
	}

	private DetailAST getClassDef(DetailAST ast) {
		return findParent(ast, TokenTypes.CLASS_DEF);
	}

	private DetailAST getInterfaceDef(DetailAST ast) {
		return findParent(ast, TokenTypes.INTERFACE_DEF);
	}

	private DetailAST findParent(DetailAST ast, int classDef) {
		while (ast != null) {
			if (ast.getType() == classDef) {
				return ast;
			}
			ast = ast.getParent();
		}
		return null;
	}

	private boolean isPublicOrProtected(DetailAST ast) {
		DetailAST modifiers = ast.findFirstToken(TokenTypes.MODIFIERS);
		if (modifiers == null) {
			return false;
		}
		return modifiers.findFirstToken(TokenTypes.LITERAL_PUBLIC) != null
				|| modifiers.findFirstToken(TokenTypes.LITERAL_PROTECTED) != null;
	}

	private boolean isFinal(DetailAST ast) {
		DetailAST modifiers = ast.findFirstToken(TokenTypes.MODIFIERS);
		if (modifiers == null) {
			return false;
		}
		return modifiers.findFirstToken(TokenTypes.FINAL) != null;
	}

}
