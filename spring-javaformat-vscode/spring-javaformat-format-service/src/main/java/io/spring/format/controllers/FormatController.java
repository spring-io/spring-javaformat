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

package io.spring.format.controllers;


import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.spring.format.formatter.FormatFiles;
import io.spring.format.vo.FormatVo;



/**.
 * @author Howard Zuo
 */
@RestController
public class FormatController {

	@RequestMapping(method = RequestMethod.POST, value = "/format")
	String formatFile(@RequestBody FormatVo formatVo) {

		List<String> contents = FormatFiles.formatFiles(Arrays.asList(formatVo.getFilePath()));

		if (contents.size() == 0) {
			return "";
		}
		return contents.get(0);
	}
}
