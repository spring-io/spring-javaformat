
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
import java.util.function.BiFunction;

/**
 * Lambda extra parentheses. If configured we enfoce that single args don't use
 * parentheses.
 *
 * @author Phillip Webb
 */
public class LambdaExtraParens {

	public Function<String, Integer> test() {
		return (string) -> 1;
	}

	public BiFunction<String, String, Integer> test2() {
		return (string1, string2) -> 1;
	}

	public void test3() {
		Object x = null;
		List<String> result = input((String x) -> 123);
	}

	private <T extends CharSequence> List<T> input(Function<T, Integer> in) {
	}

}
