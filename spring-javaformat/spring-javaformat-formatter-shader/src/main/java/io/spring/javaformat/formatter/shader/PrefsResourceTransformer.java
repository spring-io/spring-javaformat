/*
 * Copyright 2017-2020 the original author or authors.
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

package io.spring.javaformat.formatter.shader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.maven.plugins.shade.relocation.Relocator;
import org.apache.maven.plugins.shade.resource.ResourceTransformer;

/**
 * {@link ResourceTransformer} to fix formatter preference files.
 *
 * @author Phillip Webb
 */
public class PrefsResourceTransformer implements ResourceTransformer {

	private final Map<String, List<String>> transformedResources = new LinkedHashMap<>();

	@Override
	public boolean canTransformResource(String resource) {
		return resource.endsWith("formatter.prefs");
	}

	@Override
	public void processResource(String resource, InputStream is, List<Relocator> relocators) throws IOException {
		List<String> trandformedLines = new ArrayList<>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = reader.readLine();
		while (line != null) {
			trandformedLines.add(transformLine(line, relocators));
			line = reader.readLine();
		}
		this.transformedResources.put(resource, trandformedLines);
	}

	private String transformLine(String line, List<Relocator> relocators) {
		int splitIndex = line.lastIndexOf('=');
		if (splitIndex == -1) {
			return line;
		}
		String key = line.substring(0, splitIndex);
		String value = line.substring(splitIndex + 1);
		return relocateKey(key, relocators) + "=" + value;
	}

	private String relocateKey(String key, List<Relocator> relocators) {
		for (Relocator relocator : relocators) {
			if (relocator.canRelocateClass(key)) {
				return relocator.relocateClass(key);
			}
		}
		return key;
	}

	@Override
	public boolean hasTransformedResource() {
		return true;
	}

	@Override
	public void modifyOutputStream(JarOutputStream os) throws IOException {
		for (Map.Entry<String, List<String>> entry : this.transformedResources.entrySet()) {
			write(os, entry.getKey(), entry.getValue());
		}
	}

	private void write(JarOutputStream os, String resource, List<String> lines) throws IOException {
		os.putNextEntry(new JarEntry(resource));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
		for (String line : lines) {
			writer.write(line);
			writer.write("\n");
		}
		writer.flush();
	}

}
