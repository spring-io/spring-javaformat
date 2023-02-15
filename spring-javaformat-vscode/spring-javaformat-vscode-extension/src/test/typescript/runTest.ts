/*
 * Copyright 2017-2023 the original author or authors.
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

import * as path from 'path'

import { runTests } from '@vscode/test-electron'

async function main() {
  try {
    const extensionDevelopmentPath = path.resolve(__dirname, '../../../../')
    const extensionTestsPath = path.resolve(__dirname, './suite/index')
    await runTests({ extensionDevelopmentPath, extensionTestsPath, launchArgs: ['--disable-extensions'] })
  } catch (err) {
    console.error('Failed to run tests')
    process.exit(1)
  }
}

main()
