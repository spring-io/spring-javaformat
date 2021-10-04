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

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.formatter.CodeFormatter;

/**
 * Strategy interface used by {@link ExtendedCodeFormatter} to allow additional code
 * formatting before wrapping.
 *
 * @author Phillip Webb
 */
public interface Preparator {

	default Phase getPhase() {
		return Phase.POST_WRAPPING;
	}

	/**
	 * Apply the preparator.
	 * @param kind the format kind (see
	 * {@link CodeFormatter#format(int, String, org.eclipse.jface.text.IRegion[], int, String)}
	 * for details)
	 * @param tokenManager the token manager
	 * @param astRoot the AST root node
	 */
	void apply(int kind, TokenManager tokenManager, ASTNode astRoot);

	/**
	 * The phase where the {@link Preparator} should be applied.
	 */
	enum Phase {

		/**
		 * Apply the preparator before wrapping.
		 */
		PRE_WRAPPING,

		/**
		 * Apply the preparator after wrapping.
		 */
		POST_WRAPPING;

	}

}
