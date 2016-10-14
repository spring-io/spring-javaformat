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

package io.spring.format.gradle;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@literal gradle springFormatApply}.
 *
 * @author Phillip Webb
 */
public class ApplyIT {

	@Test
	public void apply() throws Exception {
		new ProjectCreator().run("apply", "springFormatApply");
		String location = "src/main/java/simple/Simple.java";
		Path original = Paths.get("src/test/resources/apply/" + location);
		Path formatted = Paths.get("target/apply/" + location);
		Path expected = Paths.get("src/test/resources/validate-ok/" + location);
		assertThat(Files.readAllLines(formatted))
				.isNotEqualTo(Files.readAllLines(original))
				.isEqualTo(Files.readAllLines(expected));
	}

}
