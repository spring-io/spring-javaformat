/*
 * Copyright 2017-2019 the original author or authors.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.AnnotationUtil;

/**
 * Checks that JUnit 5 conventions are followed and that JUnit 4 is not accidentally used.
 *
 * @author Phillip Webb
 */
public class SpringJUnit5Check extends AbstractSpringCheck {

	private static final String JUNIT4_TEST_ANNOTATION = "org.junit.Test";

	private static final String JUNIT5_TEST_ANNOTATION = "org.junit.jupiter.api.Test";

	private static final String JUNIT5_TEST_TEMPLATE_ANNOTATION = "org.junit.jupiter.api.TestTemplate";

	private static final List<String> TEST_ANNOTATIONS = Collections
			.unmodifiableList(Arrays.asList("Test", "TestTemplate", JUNIT4_TEST_ANNOTATION, JUNIT5_TEST_ANNOTATION,
					JUNIT5_TEST_TEMPLATE_ANNOTATION));

	private static final List<String> LIFECYCLE_ANNOTATIONS = Collections.unmodifiableList(Arrays.asList("BeforeAll",
			"org.junit.jupiter.api.BeforeAll", "BeforeEach", "org.junit.jupiter.api.BeforeEach", "AfterAll",
			"org.junit.jupiter.api.AfterAll", "AfterEach", "org.junit.jupiter.api.AfterEach"));

	private static final List<String> BANNED_IMPORTS;
	static {
		List<String> bannedImports = new ArrayList<>();
		bannedImports.add(JUNIT4_TEST_ANNOTATION);
		bannedImports.add("org.junit.After");
		bannedImports.add("org.junit.AfterClass");
		bannedImports.add("org.junit.Before");
		bannedImports.add("org.junit.BeforeClass");
		bannedImports.add("org.junit.Rule");
		bannedImports.add("org.junit.ClassRule");
		BANNED_IMPORTS = Collections.unmodifiableList(bannedImports);
	}

	private List<String> unlessImports = new ArrayList<>();

	private final List<DetailAST> testMethods = new ArrayList<>();

	private final Map<String, FullIdent> imports = new LinkedHashMap<>();

	private final List<DetailAST> lifecycleMethods = new ArrayList<>();

	@Override
	public int[] getAcceptableTokens() {
		return new int[] { TokenTypes.METHOD_DEF, TokenTypes.IMPORT };
	}

	@Override
	public void beginTree(DetailAST rootAST) {
		this.imports.clear();
		this.testMethods.clear();
		this.lifecycleMethods.clear();
	}

	@Override
	public void visitToken(DetailAST ast) {
		switch (ast.getType()) {
		case TokenTypes.METHOD_DEF:
			visitMethodDef(ast);
		case TokenTypes.IMPORT:
			visitImport(ast);
			break;
		}
	}

	private void visitMethodDef(DetailAST ast) {
		if (AnnotationUtil.containsAnnotation(ast, TEST_ANNOTATIONS)) {
			this.testMethods.add(ast);
		}
		if (AnnotationUtil.containsAnnotation(ast, LIFECYCLE_ANNOTATIONS)) {
			this.lifecycleMethods.add(ast);
		}
	}

	private void visitImport(DetailAST ast) {
		FullIdent ident = FullIdent.createFullIdentBelow(ast);
		this.imports.put(ident.getText(), ident);
	}

	@Override
	public void finishTree(DetailAST rootAST) {
		if (shouldCheck()) {
			check();
		}
	}

	private boolean shouldCheck() {
		if (this.testMethods.isEmpty() && this.lifecycleMethods.isEmpty()) {
			return false;
		}
		for (String unlessImport : this.unlessImports) {
			if (this.imports.containsKey(unlessImport)) {
				return false;
			}
		}
		return true;
	}

	private void check() {
		for (String bannedImport : BANNED_IMPORTS) {
			FullIdent ident = this.imports.get(bannedImport);
			if (ident != null) {
				log(ident.getLineNo(), ident.getColumnNo(), "junit5.bannedImport", bannedImport);
			}
		}
		for (DetailAST testMethod : this.testMethods) {
			if (AnnotationUtil.containsAnnotation(testMethod, JUNIT4_TEST_ANNOTATION)) {
				log(testMethod, "junit5.bannedTestAnnotation");
			}
		}
		checkMethodVisibility(this.testMethods, "junit5.testPublicMethod", "junit5.testPrivateMethod");
		checkMethodVisibility(this.lifecycleMethods, "junit5.lifecyclePublicMethod", "junit5.lifecyclePrivateMethod");
	}

	private void checkMethodVisibility(List<DetailAST> methods, String publicMessageKey, String privateMessageKey) {
		for (DetailAST method : methods) {
			DetailAST modifiers = method.findFirstToken(TokenTypes.MODIFIERS);
			if (modifiers.findFirstToken(TokenTypes.LITERAL_PUBLIC) != null) {
				log(method, publicMessageKey);
			}
			if (modifiers.findFirstToken(TokenTypes.LITERAL_PRIVATE) != null) {
				log(method, privateMessageKey);
			}
		}
	}

	private void log(DetailAST method, String key) {
		String name = method.findFirstToken(TokenTypes.IDENT).getText();
		log(method.getLineNo(), method.getColumnNo(), key, name);
	}

	public void setUnlessImports(String unlessImports) {
		this.unlessImports = Collections.unmodifiableList(
				Arrays.stream(unlessImports.split(",")).map(String::trim).collect(Collectors.toList()));
	}

}
