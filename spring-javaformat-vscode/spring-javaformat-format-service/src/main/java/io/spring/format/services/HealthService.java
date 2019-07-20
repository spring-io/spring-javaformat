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

package io.spring.format.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * .
 *
 * @author Howard Zuo
 */
@Service
public class HealthService {

	private Long lastHeartbeat;

	public Long getLastHeartbeat() {
		return this.lastHeartbeat;
	}

	public void setLastHeartbeat(Long lastHeartbeat) {
		this.lastHeartbeat = lastHeartbeat;
	}

	@Scheduled(fixedRate = 1000 * 60 * 5)
	public void liveCheck() {
		if (this.lastHeartbeat == null) {
			return;
		}
		if ((System.currentTimeMillis() - this.lastHeartbeat) > (1000 * 60 * 5)) {
			System.exit(0);
		}
	}

}
