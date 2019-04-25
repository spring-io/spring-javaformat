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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.checks.design.HideUtilityClassConstructorCheck;
import com.puppycrawl.tools.checkstyle.utils.AnnotationUtil;

/**
 * Extension to {@link HideUtilityClassConstructorCheck} that doesn't fail certain common
 * Spring patterns.
 *
 * @author Phillip Webb
 */
public class SpringHideUtilityClassConstructor extends HideUtilityClassConstructorCheck {

	private static final Set<String> BYPASS_ANNOTATIONS;
	static {
		Set<String> annotations = new LinkedHashSet<>();
		annotations.add("org.springframework.context.annotation.Configuration");
		annotations.add("org.springframework.boot.autoconfigure.SpringBootApplication");
		annotations.add("org.springframework.boot.autoconfigure.EnableAutoConfiguration");
		Set<String> shortNames = annotations.stream().map((name) -> name.substring(name.lastIndexOf(".") + 1))
				.collect(Collectors.toSet());
		annotations.addAll(shortNames);
		BYPASS_ANNOTATIONS = Collections.unmodifiableSet(annotations);
	}

	@Override
	public void visitToken(DetailAST ast) {
		if (!isBypassed(ast)) {
			super.visitToken(ast);
		}
	}

	private boolean isBypassed(DetailAST ast) {
		for (String bypassAnnotation : BYPASS_ANNOTATIONS) {
			if (AnnotationUtil.containsAnnotation(ast, bypassAnnotation)) {
				return true;
			}
		}
		return false;
	}

}
