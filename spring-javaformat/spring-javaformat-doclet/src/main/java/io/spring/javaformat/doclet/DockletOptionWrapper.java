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
import java.util.function.BiPredicate;

import jdk.javadoc.doclet.Doclet;

/**
 * Wraps an existing Doclet option to allow custom processing.
 *
 * @param option the option being watched.
 * @param processor called to handle {@link #process(String, List)}. Processor that return
 * {@code false} will delegate to the wrapped option process method
 * @author Phillip Webb
 */
record DockletOptionWrapper(Doclet.Option option,
		BiPredicate<DockletOptionWrapper, List<String>> processor) implements Doclet.Option, Comparable<Doclet.Option> {

	@Override
	public int getArgumentCount() {
		return option().getArgumentCount();
	}

	@Override
	public String getDescription() {
		return option().getDescription();
	}

	@Override
	public Kind getKind() {
		return option().getKind();
	}

	@Override
	public List<String> getNames() {
		return option().getNames();
	}

	@Override
	public String getParameters() {
		return option().getParameters();
	}

	@Override
	public boolean process(String name, List<String> arguments) {
		return processor().test(this, arguments) || option().process(name, arguments);
	}

	@Override
	public final String toString() {
		return option().toString();
	}

	@Override
	public int compareTo(Doclet.Option other) {
		return getNames().get(0).compareTo(other.getNames().get(0));
	}

}
