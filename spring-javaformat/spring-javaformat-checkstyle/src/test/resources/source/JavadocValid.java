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
 * Valid Javadoc.
 *
 * @param <T> this is a valid param
 * @author Phillip Webb
 * @author Venkata Naga Sai Srikanth Gollapudi
 */
public class JavadocValid<T> {

	/**
	 * Do something.
	 * @param something a lovely thing
	 */
	public void test(String something) {
	}

	/**
	 * Do something else.
	 * @param something a loveley thing. Even if we've got some additional desc.
	 */
	public void test2(String something) {
	}

	/**
	 * Do something else.
	 * @param something a loveley thing that goes on a bit and causes us to wrap at end.
	 * Even if we've got some additional desc.
	 * @return the thing
	 * @throws RuntimeException on the error
	 */
	public String test3(String something) throws RuntimeException {
	}

    /**
     * Do something with acronyms.
     * @param something JNI hints for the thing
     * @return the thing
     * @throws RuntimeException on the error
     */
    public String test4(String something) throws RuntimeException {
    }

	/**
	 * Class with a numeric date since.
	 * @since 28.12.2003
	 */
	public class NumericDateSince {

		/**
		 * Method with version-based since.
		 * @since 1.3.0
		 */
		public void versionSince() {
		}

	}

	/**
	 * Class with an alphanumeric date since.
	 * @since 16 April 2001
	 */
	public class AlphanumericDateSince {

		/**
		 * Method with version-based since.
		 * @since 1.3.0
		 */
		public void versionSince() {
		}

	}

}
