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

package io.spring.javaformat.doclet;

import java.util.List;
import java.util.function.BiConsumer;

import jdk.javadoc.doclet.Doclet;

/**
 * A docklet option.
 *
 * @param name the name
 * @param argumentCount the number of arguments
 * @param description the description
 * @param parameters the parameters
 * @param processor the processor to call
 * @author Phillip Webb
 */
record DocletOption(String name, int argumentCount, String description, String parameters,
		BiConsumer<String, List<String>> processor) implements Doclet.Option, Comparable<Doclet.Option> {

	@Override
	public int getArgumentCount() {
		return argumentCount();
	}

	@Override
	public String getDescription() {
		return description();
	}

	@Override
	public Kind getKind() {
		return Kind.STANDARD;
	}

	@Override
	public List<String> getNames() {
		return List.of(name());
	}

	@Override
	public String getParameters() {
		return parameters();
	}

	@Override
	public boolean process(String option, List<String> arguments) {
		processor().accept(option, arguments);
		return true;
	}

	@Override
	public int compareTo(Doclet.Option other) {
		return getNames().get(0).compareTo(other.getNames().get(0));
	}

}
