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

package io.spring.format.formatter.intellij.codestyle.monitor;

/**
 * Trigger used to to update the state for this monitor. Triggers are thread safe and can
 * be called from any active thread.
 *
 * @author Phillip Webb
 */
public interface Trigger {

	/**
	 * Update the state of the monitor.
	 * @param state the updated state
	 */
	void updateState(State state);

	/**
	 * The desired state of the plugin for this monitor.
	 */
	enum State {

		/**
		 * The plugin should be active.
		 */
		ACTIVE,

		/**
		 * The plugin need not be active.
		 */
		NOT_ACTIVE

	}

}
