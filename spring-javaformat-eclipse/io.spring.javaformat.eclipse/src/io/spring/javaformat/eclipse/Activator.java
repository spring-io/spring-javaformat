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

package io.spring.javaformat.eclipse;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 *
 * @author Phillip Webb
 */
public class Activator extends AbstractUIPlugin {

	/**
	 * The ID of the plugin.
	 */
	public static final String PLUGIN_ID = "io.spring.javaformat.eclipse"; //$NON-NLS-1$

	private static Activator plugin;

	private final List<Plugin> javaCorePlugins = new ArrayList<>();

	public Activator() {
		this.javaCorePlugins.add(new io.spring.javaformat.eclipse.jdt.jdk8.core.JavaCore());
		this.javaCorePlugins.add(new io.spring.javaformat.eclipse.jdt.jdk17.core.JavaCore());
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		for (Plugin javaCorePlugin : this.javaCorePlugins) {
			javaCorePlugin.start(context);
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		for (Plugin javaCorePlugin : this.javaCorePlugins) {
			javaCorePlugin.stop(context);
		}
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}

}
