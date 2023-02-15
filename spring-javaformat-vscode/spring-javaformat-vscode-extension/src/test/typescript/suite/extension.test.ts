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

import * as assert from 'assert'
import * as vscode from 'vscode'
import * as path from 'path'
import * as fs from 'fs'

export const WORKSPACE_PATH = path.resolve(__dirname, '..', '..', '..', 'test-workspace')
export const FILE_PATH = path.resolve(WORKSPACE_PATH, 'Test.java')

suite('Extension Test Suite', () => {
  test('Format file', async () => {
    const initial = 'public static class Test {public static void main(String[] args){}}\n'
    const expected = 'public static class Test {\n\n\tpublic static void main(String[] args) {\n\t}\n\n}\n'
    if (!fs.existsSync(WORKSPACE_PATH)) {
      fs.mkdirSync(WORKSPACE_PATH)
    }
    fs.closeSync(fs.openSync(FILE_PATH, 'w'))
    const document = await vscode.workspace.openTextDocument(vscode.Uri.file(FILE_PATH))
    const editor = await vscode.window.showTextDocument(document)
    await editor.edit((builder) => {
      builder.delete(new vscode.Range(new vscode.Position(0, 0), document.positionAt(document.getText().length)))
      builder.insert(new vscode.Position(0, 0), initial)
    })
    editor.selection = new vscode.Selection(0, 0, 0, 0)
    await vscode.commands.executeCommand('editor.action.formatDocument')
    const actual = document.getText()
    assert.deepEqual(actual, expected)
  })
})
