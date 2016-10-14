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

package org.eclipse.jdt.internal.formatter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.formatter.Preparator.Phase;

/**
 * Extended version of {@link DefaultCodeFormatter} that allows additional
 * {@link Preparator preparators} to be used.
 *
 * @author Phillip Webb
 */
public class ExtendedCodeFormatter extends DefaultCodeFormatter {

	private final List<Preparator> preparators = new ArrayList<>();

	public ExtendedCodeFormatter() {
		super();
	}

	public ExtendedCodeFormatter(DefaultCodeFormatterOptions defaultCodeFormatterOptions,
			Map<String, String> options) {
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
	protected void prepareWraps(int kind) {
		ASTNode astRoot = getField("astRoot", ASTNode.class);
		TokenManager tokenManager = getField("tokenManager", TokenManager.class);
		applyPreparators(Phase.PRE_WRAPPING, astRoot, tokenManager);
		super.prepareWraps(kind);
		applyPreparators(Phase.POST_WRAPPING, astRoot, tokenManager);
	}

	private void applyPreparators(Phase preWrapping, ASTNode astRoot,
			TokenManager tokenManager) {
		this.preparators.stream()
				.filter((preparator) -> preparator.getPhase() == preWrapping)
				.forEach((preparator) -> preparator.apply(tokenManager, astRoot));
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
