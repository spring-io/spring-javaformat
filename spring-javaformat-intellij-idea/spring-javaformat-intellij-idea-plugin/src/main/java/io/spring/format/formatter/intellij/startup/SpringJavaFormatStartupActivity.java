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

package io.spring.format.formatter.intellij.startup;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;

/**
 * {@link StartupActivity} hook for {@link ManagedSpringJavaFormatProject}.
 *
 * @author Phillip Webb
 */
public class SpringJavaFormatStartupActivity implements StartupActivity {

	@Override
	public void runActivity(Project project) {
		new ManagedSpringJavaFormatProject(project);
	}

}
