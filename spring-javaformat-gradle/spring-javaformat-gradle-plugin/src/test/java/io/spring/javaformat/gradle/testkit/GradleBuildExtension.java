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

package io.spring.javaformat.gradle.testkit;

import java.lang.reflect.Field;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * An {@link Extension} for managing the lifecycle of a {@link GradleBuild} stored in a
 * field named {@code gradleBuild}.
 *
 * @author Andy Wilkinson
 * @author Scott Frederick
 * @author Phillip Webb
 */
public class GradleBuildExtension implements BeforeEachCallback, AfterEachCallback {

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		extractGradleBuild(context).before();
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		extractGradleBuild(context).after();
	}

	private GradleBuild extractGradleBuild(ExtensionContext context) throws Exception {
		Object testInstance = context.getRequiredTestInstance();
		Field gradleBuildField = testInstance.getClass().getDeclaredField("gradleBuild");
		gradleBuildField.setAccessible(true);
		GradleBuild gradleBuild = (GradleBuild) gradleBuildField.get(testInstance);
		return gradleBuild;
	}

}
