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

import java.util.function.Function;

/**
 * This is a valid example of a lambda where the block is required.
 *
 * @author Phillip Webb
 */
public class LambdaNecessaryTryBlock {

	public Function<String, Integer> test() {
		return (string) -> {
			try {
				return 0;
			}
			catch (Exception ex) {
				// Ignore
			}
		};
	}

}
