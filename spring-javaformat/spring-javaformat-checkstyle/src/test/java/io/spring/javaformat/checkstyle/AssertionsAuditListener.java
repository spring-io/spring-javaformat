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

package io.spring.javaformat.checkstyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.puppycrawl.tools.checkstyle.AuditEventDefaultFormatter;
import com.puppycrawl.tools.checkstyle.AuditEventFormatter;
import com.puppycrawl.tools.checkstyle.DefaultLogger;
import com.puppycrawl.tools.checkstyle.Definitions;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.LocalizedMessage;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link AuditListener} that checks expected events occur.
 *
 * @author Phillip Webb
 */
class AssertionsAuditListener implements AuditListener {

	static final AuditEventFormatter FORMATTER = new AuditEventDefaultFormatter();

	private final List<String> checks;

	private List<String> filenames = new ArrayList<>();

	private final StringBuilder message = new StringBuilder();

	private final Map<SeverityLevel, Integer> severityCounts = new TreeMap<>();

	AssertionsAuditListener(List<String> checks) {
		this.checks = checks;
	}

	@Override
	public void auditStarted(AuditEvent event) {
		recordLevel(event);
		recordLocalizedMessage(DefaultLogger.AUDIT_STARTED_MESSAGE);
		recordLevel(event);
	}

	@Override
	public void auditFinished(AuditEvent event) {
		recordLevel(event);
		recordLocalizedMessage(DefaultLogger.AUDIT_FINISHED_MESSAGE);
		recordMessage(this.severityCounts.toString());
		int errors = this.severityCounts.getOrDefault(SeverityLevel.ERROR, 0);
		recordMessage(errors + (errors == 1 ? " error" : " errors"));
		System.out.println(this.filenames);
		System.out.println(this.message);
		this.checks.forEach(this::runCheck);
	}

	private void runCheck(String check) {
		String description = this.filenames.toString();
		if (check.startsWith("+")) {
			assertThat(this.message.toString()).as(description).contains(check.substring(1));
		}
		else if (check.startsWith("-")) {
			assertThat(this.message.toString()).as(description).doesNotContain(check.substring(1));
		}
	}

	@Override
	public void fileStarted(AuditEvent event) {
		this.filenames.add(event.getFileName());
	}

	@Override
	public void fileFinished(AuditEvent event) {
	}

	@Override
	public void addError(AuditEvent event) {
		recordLevel(event);
		if (event.getSeverityLevel() != SeverityLevel.IGNORE) {
			recordMessage(FORMATTER.format(event));
		}
	}

	@Override
	public void addException(AuditEvent event, Throwable throwable) {
		recordLevel(event);
		recordLocalizedMessage(DefaultLogger.ADD_EXCEPTION_MESSAGE, event.getFileName());
	}

	private void recordLevel(AuditEvent event) {
		this.severityCounts.compute(event.getSeverityLevel(), (level, count) -> (count == null ? 1 : count + 1));
	}

	private void recordLocalizedMessage(String message, String... args) {
		recordMessage(new LocalizedMessage(0, Definitions.CHECKSTYLE_BUNDLE, message, args, null,
				LocalizedMessage.class, null).getMessage());
	}

	private void recordMessage(String message) {
		this.message.append(message);
		this.message.append("\n");
	}

}
