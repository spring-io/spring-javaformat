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

package io.spring.javaformat.config;

/**
 * Java JDK baseline version expected be used when formatting.
 *
 * @author Phillip Webb
 */
public enum JavaBaseline {

	/**
	 * Use JDK 8+ compatible formatter.
	 */
	V8,

	/**
	 * Use JDK 17+ or higher compatible formatter.
	 */
	V17

}
