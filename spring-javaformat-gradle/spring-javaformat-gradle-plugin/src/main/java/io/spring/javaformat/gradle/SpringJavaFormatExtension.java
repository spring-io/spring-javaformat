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

package io.spring.javaformat.gradle;

import java.util.Collection;

import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;

/**
 * Gradle DSL extension for {@link SpringJavaFormatPlugin}.
 *
 * @author Phillip Webb
 */
public class SpringJavaFormatExtension {

	private Collection<SourceSet> sourceSets;

	private String encoding;

	/**
	 * Create a new {@link SpringJavaFormatExtension} instance.
	 * @param project the source gradle project
	 */
	public SpringJavaFormatExtension(Project project) {
	}

	/**
	 * Get the sources sets that the plugin should uses.
	 * @return the the source sets to use
	 */
	public Collection<SourceSet> getSourceSets() {
		return this.sourceSets;
	}

	/**
	 * Set the sources sets that the plugin should uses.
	 * @param sourceSets the source sets to use
	 */
	public void setSourceSets(Collection<SourceSet> sourceSets) {
		this.sourceSets = sourceSets;
	}

	/**
	 * Get the file encoding in use.
	 * @return the encoding the file encoding
	 */
	public String getEncoding() {
		return this.encoding;
	}

	/**
	 * Set the file encoding in use.
	 * @param encoding the encoding to set
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

}
