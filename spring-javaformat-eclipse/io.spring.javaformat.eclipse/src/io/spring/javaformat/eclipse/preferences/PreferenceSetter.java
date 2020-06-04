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

package io.spring.javaformat.eclipse.preferences;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;

public class PreferenceSetter {

	private IScopeContext projectScope;

	private boolean resetAll = false;

	public PreferenceSetter(IProject project) {
		this.projectScope = new ProjectScope(project);
	}

	public void reset() {
		this.resetAll = true;
		set();
		this.resetAll = false;
	}

	public void set() {
		configureJDTUi();
		configureJDTCore();
	}

	private void configureJDTUi() {
		IEclipsePreferences prefs = getNode("org.eclipse.jdt.ui");
		if (null == prefs) {
			return;
		}
		if (this.resetAll) {
			try {
				prefs.clear();
				prefs.flush();
			}
			catch (BackingStoreException e) {
				e.printStackTrace();
			}
			return;
		}

		Map<String, String> options = new HashMap<>();
		try {
			Properties properties = new Properties();
			try (InputStream inputStream = PreferenceSetter.class.getResourceAsStream("org.eclipse.jdt.ui.prefs")) {
				properties.load(inputStream);
				options = (Map) Collections.unmodifiableMap(properties);
			}
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
		options.forEach((key, value) -> prefs.put(key, value));

		savePrefs(prefs);
	}

	private void configureJDTCore() {

		IEclipsePreferences prefs = getNode("org.eclipse.jdt.core"); // does all
		if (null == prefs) {
			return;
		}
		if (this.resetAll) {
			try {
				prefs.clear();
				prefs.flush();
			}
			catch (BackingStoreException e) {
				e.printStackTrace();
			}
			return;
		}
		Map<String, String> options = new HashMap<>();
		try {
			Properties properties = new Properties();
			try (InputStream inputStream = PreferenceSetter.class.getResourceAsStream("org.eclipse.jdt.core.prefs")) {
				properties.load(inputStream);
				options = (Map) Collections.unmodifiableMap(properties);
			}
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
		options.forEach((key, value) -> prefs.put(key, value));

		savePrefs(prefs);
	}

	private IEclipsePreferences getNode(String node) {
		return this.projectScope.getNode(node);
	}

	private void savePrefs(IEclipsePreferences prefs) {
		try {
			prefs.flush();
		}
		catch (org.osgi.service.prefs.BackingStoreException e) {
			e.printStackTrace();
		}

	}

}
