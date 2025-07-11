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

/**
 * This is an example of a ternary expression.
 *
 * @author Phillip Webb
 */
public class TernaryEqualsEquals {

	public void valid() {
		boolean a = true;
		boolean b = false;
		boolean bb = true;
		int c = (a != b) ? 1 : 2;
		int d = ((a != b) | (a == b)) ? 1 : 2;
		int e = (a != (b | bb)) ? 1 : 2;
	}

	public void notValid() {
		Boolean a = true;
		int c = (a == null) ? 1 : 2;
	}

}
