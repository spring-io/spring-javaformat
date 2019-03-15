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

package io.spring.javaformat.checkstyle.filter;

import java.lang.reflect.Field;
import java.util.Objects;

import com.puppycrawl.tools.checkstyle.TreeWalkerAuditEvent;
import com.puppycrawl.tools.checkstyle.TreeWalkerFilter;
import com.puppycrawl.tools.checkstyle.api.LocalizedMessage;

/**
 * {@link TreeWalkerFilter} that can used to relax the {@code 'this.'} requirement when
 * referring to an outer class from an inner class.
 *
 * @author Phillip Webb
 */
public class RequiresOuterThisFilter implements TreeWalkerFilter {

	private static final Field ARGS_FIELD = getArgsField();

	@Override
	public boolean accept(TreeWalkerAuditEvent event) {
		LocalizedMessage message = event.getLocalizedMessage();
		if ("require.this.variable".equals(message.getKey())) {
			Object[] args = getArgs(message);
			String prefex = (args.length > 1 ? Objects.toString(args[1]) : null);
			if (prefex != null && prefex.length() > 0) {
				return false;
			}
		}
		return true;
	}

	private Object[] getArgs(LocalizedMessage message) {
		if (ARGS_FIELD == null) {
			throw new IllegalStateException("Unable to extract message args");
		}
		try {
			return (Object[]) ARGS_FIELD.get(message);
		}
		catch (Exception ex) {
			return null;
		}
	}

	private static Field getArgsField() {
		try {
			Field field = LocalizedMessage.class.getDeclaredField("args");
			field.setAccessible(true);
			return field;
		}
		catch (Exception ex) {
			return null;
		}
	}

}
