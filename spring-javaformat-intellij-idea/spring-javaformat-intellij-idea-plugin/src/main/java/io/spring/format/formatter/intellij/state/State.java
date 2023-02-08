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

package io.spring.format.formatter.intellij.state;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;

/**
 * The state of the plugin.
 *
 * @author Phillip Webb
 */
public enum State {

	/**
	 * The plugin is active.
	 */
	ACTIVE,

	/**
	 * The plugin is not active.
	 */
	NOT_ACTIVE;

	private static final Key<State> KEY = Key.create(State.class.getName());

	/**
	 * Put this state to the given project.
	 * @param project the project that should save the state
	 */
	public void put(Project project) {
		project.putUserData(KEY, this);
	}

	/**
	 * Return the state from the given project.
	 * @param project the project to check
	 * @return the state of the project
	 */
	public static State get(Project project) {
		State state = (project != null) ? project.getUserData(KEY) : null;
		return (state != null) ? state : NOT_ACTIVE;
	}

}
