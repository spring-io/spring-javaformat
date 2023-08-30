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

import path = require('path')
import vscode = require('vscode')
import childprocess = require('child_process')

const JAR_PATH = path.resolve(__dirname, '..', 'runtime', 'spring-java-format.jar')

export default class SpringDocumentFormattingEditProvider implements vscode.DocumentFormattingEditProvider {
  provideDocumentFormattingEdits(
    document: vscode.TextDocument,
    options: vscode.FormattingOptions,
    token: vscode.CancellationToken,
  ): vscode.ProviderResult<vscode.TextEdit[]> {
    if (vscode.window.visibleTextEditors.every((editor) => editor.document.fileName !== document.fileName)) {
      return []
    }
    return this.runFormatter(document, token).then(
      (edits) => edits,
      (err) => {
        if (err) {
          console.log(err)
          return Promise.reject(`Check the console in dev tools to find errors when formatting spring-javaformat`)
        }
      },
    )
  }

  private runFormatter(document: vscode.TextDocument, token: vscode.CancellationToken): Promise<vscode.TextEdit[]> {
    return new Promise<vscode.TextEdit[]>((resolve, reject) => {
      console.log(`formatting ${document.uri} using spring-javaformat`)
      let stdout = ''
      let stderr = ''
      const cwd = path.dirname(document.fileName)
      const args = ['-jar', JAR_PATH]
      const process = childprocess.spawn('java', args, { cwd })
      token.onCancellationRequested(() => !process.killed && process.kill())
      process.stdout.setEncoding('utf8')
      process.stdout.on('data', (data) => (stdout += data))
      process.stderr.on('data', (data) => (stderr += data))
      process.on('error', (err) => {
        console.log(`spring-javaformat returned error ${err}`)
        if (err && (<any>err).code === 'ENOENT') {
          return reject(`failed to find run spring-javaformat due to missing 'java' executable`)
        }
      })
      process.on('close', (code) => {
        if (code !== 0) {
          console.log(`spring-javaformat returned error code ${code}`)
          return reject(stderr)
        }
        console.log('spring-javaformat returned without error')
        const fileStart = new vscode.Position(0, 0)
        const fileEnd = document.lineAt(document.lineCount - 1).range.end
        const textEdits: vscode.TextEdit[] = [new vscode.TextEdit(new vscode.Range(fileStart, fileEnd), stdout)]
        return resolve(textEdits)
      })
      if (process.pid) {
        console.log('sending document data to spring-javaformat')
        process.stdin.end(document.getText())
      }
    })
  }
}
