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
 * Bad cases for use of {@link Deprecated @Deprecated}.
 *
 * @author Andy Wilkinson
 */
@Deprecated
public class DeprecatedBadCase {
	
	@java.lang.Deprecated
	public static final String SOME_CONSTANT;
	
	@Deprecated(since = "")
	public void someMethod() {
		
	}
	
	@Deprecated
	private class InnerClass {
		
		@Deprecated(since = "")
		private void someInnerMethod() {
			
		}
		
	}

}
