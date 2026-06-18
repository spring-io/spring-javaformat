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
 * Wraps an existing Doclet option and listen for calls to {@link #process(String, List)}.
 *
 * @param option the option being watched.
 * @param onProcess {@link BiConsumer} called when {@link #process(String, List)} is
 * called
 * @author Phillip Webb
 */
record WatchedDocletOption(Doclet.Option option,
		BiConsumer<String, List<String>> onProcess) implements Doclet.Option, Comparable<Doclet.Option> {

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
	public boolean process(String option, List<String> arguments) {
		onProcess().accept(option, arguments);
		return option().process(option, arguments);
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
