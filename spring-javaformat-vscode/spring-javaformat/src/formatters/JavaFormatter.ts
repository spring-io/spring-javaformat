import { TextDocument, Range, TextEdit } from 'vscode'

import FormatService from '../services/FormatService'

export async function formatJava(document: TextDocument): Promise<Array<TextEdit>> {
  const code = await FormatService.getInstance().formatCode(document.getText())

  const range = new Range(document.positionAt(0), document.positionAt(document.getText().length))
  return [TextEdit.replace(range, code)]
}
