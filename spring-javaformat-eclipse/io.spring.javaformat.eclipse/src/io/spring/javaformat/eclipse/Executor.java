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

package io.spring.javaformat.eclipse;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

/**
 * Utility to execute a block of code and deal with errors.
 *
 * @author Phillip Webb
 */
public class Executor {

	private final String failureMessage;

	/**
	 * Create a new {@link Executor} instance with a specific message to use on failure.
	 * @param failureMessage the failure message
	 */
	public Executor(String failureMessage) {
		this.failureMessage = failureMessage;
	}

	/**
	 * Run the given command, dealing with any exceptions.
	 * @param command the command to run
	 * @throws CoreException on error
	 */
	public void run(Command command) throws CoreException {
		try {
			command.run();
		}
		catch (Exception ex) {
			if (ex instanceof CoreException) {
				throw (CoreException) ex;
			}
			String msg = NLS.bind(this.failureMessage, ex.getMessage());
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, msg, ex));
		}
	}

	/**
	 * A command that can be run.
	 */
	public interface Command {

		/**
		 * Run the command.
		 * @throws Exception on error
		 */
		void run() throws Exception;

	}

}
