/*
 * Copyright 2017-2021 the original author or authors.
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

package org.eclipse.jdt.internal.formatter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.formatter.Preparator.Phase;

/**
 * Extended version of {@link DefaultCodeFormatter} that allows additional
 * {@link Preparator preparators} to be used.
 *
 * @author Phillip Webb
 */
public class ExtendedCodeFormatter extends DefaultCodeFormatter {

	private static boolean useLegacyTokenize = false;

	private final List<Preparator> preparators = new ArrayList<>();

	public ExtendedCodeFormatter() {
		super();
	}

	public ExtendedCodeFormatter(DefaultCodeFormatterOptions defaultCodeFormatterOptions, Map<String, String> options) {
		super(defaultCodeFormatterOptions, options);
	}

	public ExtendedCodeFormatter(DefaultCodeFormatterOptions options) {
		super(options);
	}

	public ExtendedCodeFormatter(Map<String, String> options) {
		super(options);
	}

	protected void addPreparator(Preparator preparator) {
		this.preparators.add(preparator);
	}

	@Override
	protected void tokenizeSource(int kind) {
		if (useLegacyTokenize) {
			legacyTokenizeSource(kind);
			return;
		}
		try {
			super.tokenizeSource(kind);
		}
		catch (NoSuchMethodError ex) {
			useLegacyTokenize = true;
			legacyTokenizeSource(kind);
		}
	}

	private void legacyTokenizeSource(int kind) {
		this.tokens.clear();
		long sourceLevel = CompilerOptions.versionToJdkLevel(this.sourceLevel);
		Scanner scanner = new Scanner(true, false, false, sourceLevel, null, null, false);
		scanner.setSource(this.sourceArray);
		scanner.fakeInModule = (kind & K_MODULE_INFO) != 0;
		while (true) {
			try {
				int tokenType = scanner.getNextToken();
				if (tokenType == TerminalTokens.TokenNameEOF) {
					break;
				}
				Token token = Token.fromCurrent(scanner, tokenType);
				this.tokens.add(token);
			}
			catch (InvalidInputException ex) {
				Token token = Token.fromCurrent(scanner, TerminalTokens.TokenNameNotAToken);
				this.tokens.add(token);
			}
		}
	}

	@Override
	protected void prepareWraps(int kind) {
		ASTNode astRoot = getField("astRoot", ASTNode.class);
		TokenManager tokenManager = getField("tokenManager", TokenManager.class);
		applyPreparators(Phase.PRE_WRAPPING, kind, astRoot, tokenManager);
		super.prepareWraps(kind);
		applyPreparators(Phase.POST_WRAPPING, kind, astRoot, tokenManager);
	}

	private void applyPreparators(Phase preWrapping, int kind, ASTNode astRoot, TokenManager tokenManager) {
		this.preparators.stream().filter((preparator) -> preparator.getPhase() == preWrapping)
				.forEach((preparator) -> preparator.apply(kind, tokenManager, astRoot));
	}

	@SuppressWarnings("unchecked")
	private <T> T getField(String name, Class<T> type) {
		try {
			Field field = DefaultCodeFormatter.class.getDeclaredField(name);
			field.setAccessible(true);
			return (T) field.get(this);
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

}
