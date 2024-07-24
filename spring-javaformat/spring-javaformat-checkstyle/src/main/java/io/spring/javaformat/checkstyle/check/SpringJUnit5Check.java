/*
 * Copyright 2017-2023 the original author or authors.
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	private static final String JUNIT4_TEST_ANNOTATION_NAME = "org.junit.Test";

	private static final List<Annotation> TEST_ANNOTATIONS;
	static {
		Set<Annotation> annotations = new LinkedHashSet<>();
		annotations.add(new Annotation("org.junit.jupiter.api", "RepeatedTest"));
		annotations.add(new Annotation("org.junit.jupiter.api", "Test"));
		annotations.add(new Annotation("org.junit.jupiter.api", "TestFactory"));
		annotations.add(new Annotation("org.junit.jupiter.api", "TestTemplate"));
		annotations.add(new Annotation("org.junit.jupiter.api", "ParameterizedTest"));
		TEST_ANNOTATIONS = Collections.unmodifiableList(new ArrayList<>(annotations));
	}

	private static final List<Annotation> LIFECYCLE_ANNOTATIONS;
	static {
		Set<Annotation> annotations = new LinkedHashSet<>();
		annotations.add(new Annotation("org.junit.jupiter.api", "BeforeAll"));
		annotations.add(new Annotation("org.junit.jupiter.api", "BeforeEach"));
		annotations.add(new Annotation("org.junit.jupiter.api", "AfterAll"));
		annotations.add(new Annotation("org.junit.jupiter.api", "AfterEach"));
		LIFECYCLE_ANNOTATIONS = Collections.unmodifiableList(new ArrayList<>(annotations));
	}

	private static final Annotation NESTED_ANNOTATION = new Annotation("org.junit.jupiter.api", "Nested");

	private static final Set<String> BANNED_IMPORTS;
	static {
		Set<String> bannedImports = new LinkedHashSet<>();
		bannedImports.add(JUNIT4_TEST_ANNOTATION_NAME);
		bannedImports.add("org.junit.After");
		bannedImports.add("org.junit.AfterClass");
		bannedImports.add("org.junit.Before");
		bannedImports.add("org.junit.BeforeClass");
		bannedImports.add("org.junit.Rule");
		bannedImports.add("org.junit.ClassRule");
		BANNED_IMPORTS = Collections.unmodifiableSet(bannedImports);
	}

	private List<String> unlessImports = new ArrayList<>();

	private final List<DetailAST> testMethods = new ArrayList<>();

	private final Map<String, FullIdent> imports = new LinkedHashMap<>();

	private final List<DetailAST> lifecycleMethods = new ArrayList<>();

	private final List<DetailAST> nestedTestClasses = new ArrayList<>();

	private DetailAST testClass;

	@Override
	public int[] getAcceptableTokens() {
		return new int[] { TokenTypes.METHOD_DEF, TokenTypes.IMPORT, TokenTypes.CLASS_DEF };
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
				break;
			case TokenTypes.IMPORT:
				visitImport(ast);
				break;
			case TokenTypes.CLASS_DEF:
				visitClassDefinition(ast);
				break;
		}
	}

	private void visitMethodDef(DetailAST ast) {
		if (containsAnnotation(ast, TEST_ANNOTATIONS)) {
			this.testMethods.add(ast);
		}
		if (containsAnnotation(ast, LIFECYCLE_ANNOTATIONS)) {
			this.lifecycleMethods.add(ast);
		}
	}

	private boolean containsAnnotation(DetailAST ast, List<Annotation> annotations) {
		List<String> annotationNames = annotations.stream().flatMap((annotation) ->
				Stream.of(annotation.simpleName, annotation.fullyQualifiedName())).collect(Collectors.toList());
		try {
			return AnnotationUtil.containsAnnotation(ast, annotationNames);
		}
		catch (NoSuchMethodError ex) {
			// Checkstyle >= 10.3 (https://github.com/checkstyle/checkstyle/issues/14134)
			Set<String> annotationNamesSet = new HashSet<>(annotationNames);
			try {
				return (boolean) AnnotationUtil.class.getMethod("containsAnnotation",  DetailAST.class, Set.class)
						.invoke(null, ast, annotationNamesSet);
			}
			catch (Exception ex2) {
				throw new RuntimeException("containsAnnotation failed", ex2);
			}
		}
	}

	private void visitImport(DetailAST ast) {
		FullIdent ident = FullIdent.createFullIdentBelow(ast);
		this.imports.put(ident.getText(), ident);
	}

	private void visitClassDefinition(DetailAST ast) {
		if (ast.getParent().getType() == TokenTypes.COMPILATION_UNIT) {
			this.testClass = ast;
		}
		else {
			if (containsAnnotation(ast, Arrays.asList(NESTED_ANNOTATION))) {
				this.nestedTestClasses.add(ast);
			}
		}
	}

	@Override
	public void finishTree(DetailAST rootAST) {
		if (shouldCheck()) {
			check();
		}
	}

	private boolean shouldCheck() {
		if (this.testMethods.isEmpty() && this.lifecycleMethods.isEmpty() && this.nestedTestClasses.isEmpty()) {
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
		if (this.testClass != null) {
			checkVisibility(Arrays.asList(this.testClass), "junit5.publicClass", null);
		}
		checkVisibility(this.nestedTestClasses, "junit5.publicNestedClass", "junit5.privateNestedClass");
		for (String bannedImport : BANNED_IMPORTS) {
			FullIdent ident = this.imports.get(bannedImport);
			if (ident != null) {
				log(ident.getLineNo(), ident.getColumnNo(), "junit5.bannedImport", bannedImport);
			}
		}
		for (DetailAST testMethod : this.testMethods) {
			if (AnnotationUtil.containsAnnotation(testMethod, JUNIT4_TEST_ANNOTATION_NAME)) {
				log(testMethod, "junit5.bannedTestAnnotation");
			}
		}
		checkVisibility(this.testMethods, "junit5.testPublicMethod", "junit5.testPrivateMethod");
		checkVisibility(this.lifecycleMethods, "junit5.lifecyclePublicMethod", "junit5.lifecyclePrivateMethod");
	}

	private void checkVisibility(List<DetailAST> asts, String publicMessageKey, String privateMessageKey) {
		for (DetailAST ast : asts) {
			DetailAST modifiers = ast.findFirstToken(TokenTypes.MODIFIERS);
			if (modifiers.findFirstToken(TokenTypes.LITERAL_PUBLIC) != null) {
				log(ast, publicMessageKey);
			}
			if ((privateMessageKey != null) && (modifiers.findFirstToken(TokenTypes.LITERAL_PRIVATE) != null)) {
				log(ast, privateMessageKey);
			}
		}
	}

	private void log(DetailAST ast, String key) {
		String name = ast.findFirstToken(TokenTypes.IDENT).getText();
		log(ast.getLineNo(), ast.getColumnNo(), key, name);
	}

	public void setUnlessImports(String unlessImports) {
		this.unlessImports = Collections
			.unmodifiableList(Arrays.stream(unlessImports.split(",")).map(String::trim).collect(Collectors.toList()));
	}

	private static final class Annotation {

		private final String packageName;

		private final String simpleName;

		private Annotation(String packageName, String simpleName) {
			this.packageName = packageName;
			this.simpleName = simpleName;
		}

		private String fullyQualifiedName() {
			return this.packageName + "." + this.simpleName;
		}

	}

}
