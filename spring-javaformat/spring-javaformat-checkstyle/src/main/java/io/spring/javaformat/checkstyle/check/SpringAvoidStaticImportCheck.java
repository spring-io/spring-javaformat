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

package io.spring.javaformat.checkstyle.check;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.puppycrawl.tools.checkstyle.checks.imports.AvoidStarImportCheck;
import com.puppycrawl.tools.checkstyle.checks.imports.AvoidStaticImportCheck;

/**
 * Spring variant of {@link AvoidStarImportCheck}.
 *
 * @author Phillip Webb
 */
public class SpringAvoidStaticImportCheck extends AvoidStaticImportCheck {

	private static final Set<String> ALWAYS_EXCLUDED;
	static {
		Set<String> excludes = new LinkedHashSet<>();
		excludes.add("io.restassured.RestAssured.*");
		excludes.add("org.assertj.core.api.Assertions.*");
		excludes.add("org.assertj.core.api.Assumptions.*");
		excludes.add("org.assertj.core.api.HamcrestCondition.*");
		excludes.add("org.awaitility.Awaitility.*");
		excludes.add("org.hamcrest.CoreMatchers.*");
		excludes.add("org.hamcrest.Matchers.*");
		excludes.add("org.junit.Assert.*");
		excludes.add("org.junit.Assume.*");
		excludes.add("org.junit.internal.matchers.ThrowableMessageMatcher.*");
		excludes.add("org.junit.jupiter.api.Assertions.*");
		excludes.add("org.junit.jupiter.api.Assumptions.*");
		excludes.add("org.junit.jupiter.api.Assertions.*");
		excludes.add("org.mockito.ArgumentMatchers.*");
		excludes.add("org.mockito.BDDMockito.*");
		excludes.add("org.mockito.Matchers.*");
		excludes.add("org.mockito.AdditionalMatchers.*");
		excludes.add("org.mockito.Mockito.*");
		excludes.add("org.springframework.boot.configurationprocessor.ConfigurationMetadataMatchers.*");
		excludes.add("org.springframework.boot.configurationprocessor.TestCompiler.*");
		excludes.add("org.springframework.boot.test.autoconfigure.AutoConfigurationImportedCondition.*");
		excludes.add("org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo");
		excludes.add("org.springframework.restdocs.headers.HeaderDocumentation.*");
		excludes.add("org.springframework.restdocs.hypermedia.HypermediaDocumentation.*");
		excludes.add("org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*");
		excludes.add("org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*");
		excludes.add("org.springframework.restdocs.operation.preprocess.Preprocessors.*");
		excludes.add("org.springframework.restdocs.payload.PayloadDocumentation.*");
		excludes.add("org.springframework.restdocs.request.RequestDocumentation.*");
		excludes.add("org.springframework.restdocs.restassured3.operation.preprocess.RestAssuredPreprocessors.*");
		excludes.add("org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.*");
		excludes.add("org.springframework.restdocs.snippet.Attributes.*");
		excludes.add("org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.*");
		excludes.add("org.springframework.security.config.Customizer.*");
		excludes.add("org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.*");
		excludes.add("org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.*");
		excludes.add("org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*");
		excludes.add("org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*");
		excludes.add("org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*");
		excludes.add("org.springframework.test.web.client.ExpectedCount.*");
		excludes.add("org.springframework.test.web.client.match.MockRestRequestMatchers.*");
		excludes.add("org.springframework.test.web.client.response.MockRestResponseCreators.*");
		excludes.add("org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*");
		excludes.add("org.springframework.test.web.servlet.result.MockMvcResultHandlers.*");
		excludes.add("org.springframework.test.web.servlet.result.MockMvcResultMatchers.*");
		excludes.add("org.springframework.test.web.servlet.setup.MockMvcBuilders.*");
		excludes.add("org.springframework.web.reactive.function.BodyInserters.*");
		excludes.add("org.springframework.web.reactive.function.server.RequestPredicates.*");
		excludes.add("org.springframework.web.reactive.function.server.RouterFunctions.*");
		excludes.add("org.springframework.ws.test.client.RequestMatchers.*");
		excludes.add("org.springframework.ws.test.client.ResponseCreators.*");
		ALWAYS_EXCLUDED = Collections.unmodifiableSet(excludes);
	}

	public SpringAvoidStaticImportCheck() {
		setExcludes(ALWAYS_EXCLUDED.toArray(new String[0]));
	}

	@Override
	public void setExcludes(String... excludes) {
		Set<String> merged = new LinkedHashSet<>(ALWAYS_EXCLUDED);
		merged.addAll(Arrays.asList(excludes));
		super.setExcludes(merged.toArray(new String[0]));
	}

}
