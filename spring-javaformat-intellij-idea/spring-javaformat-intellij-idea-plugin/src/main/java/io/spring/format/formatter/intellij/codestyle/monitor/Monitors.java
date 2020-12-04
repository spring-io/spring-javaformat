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

package io.spring.format.formatter.intellij.codestyle.monitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.intellij.openapi.project.Project;

import io.spring.format.formatter.intellij.codestyle.monitor.Trigger.State;

/**
 * Utility class used to manage a collection of {@link Monitors}. Creates and manages
 * individual {@link Monitor} instances and takes care of combining {@link Trigger}
 * results.
 *
 * @author Phillip Webb
 */
public class Monitors {

	private final Consumer<State> stateChangeConsumer;

	private final List<Monitor> monitors;

	private final List<State> states = new ArrayList<State>();

	/**
	 * Create a new {@link Monitors} instnace.
	 * @param project the source project
	 * @param stateChangeConsumer consumer called whenever the ultimate state changes
	 * @param monitorFactories factories used to create the monitors
	 */
	public Monitors(Project project, Consumer<State> stateChangeConsumer, Monitor.Factory... monitorFactories) {
		this(project, stateChangeConsumer, Arrays.asList(monitorFactories));
	}

	/**
	 * Create a new {@link Monitors} instnace.
	 * @param project the source project
	 * @param stateChangeConsumer consumer called whenever the ultimate state changes
	 * @param monitorFactories factories used to create the monitors
	 */
	public Monitors(Project project, Consumer<State> stateChangeConsumer, List<Monitor.Factory> monitorFactories) {
		this.stateChangeConsumer = stateChangeConsumer;
		List<Monitor> monitors = new ArrayList<>(monitorFactories.size());
		for (Monitor.Factory factory : monitorFactories) {
			Monitor monitor = factory.createMonitor(project, addTrigger());
			if (monitor != null) {
				monitors.add(monitor);
			}
		}
		this.monitors = Collections.unmodifiableList(monitors);
	}

	private Trigger addTrigger() {
		int index = addState();
		return (state) -> {
			boolean activeBefore;
			boolean activeAfter;
			synchronized (this.states) {
				activeBefore = containsActiveState(this.states);
				this.states.set(index, state);
				activeAfter = containsActiveState(this.states);
			}
			if (activeBefore != activeAfter) {
				this.stateChangeConsumer.accept(activeAfter ? State.ACTIVE : State.NOT_ACTIVE);
			}
		};
	}

	private boolean containsActiveState(List<State> states) {
		return states.stream().anyMatch((item) -> item == State.ACTIVE);
	}

	private int addState() {
		synchronized (this.states) {
			this.states.add(State.NOT_ACTIVE);
			return this.states.size() - 1;
		}
	}

	public void stop() {
		for (Monitor monitor : this.monitors) {
			monitor.stop();
		}
	}

}
