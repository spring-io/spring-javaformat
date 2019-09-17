import { TextDocument, Range, TextEdit } from 'vscode'

import FormatService from '../services/FormatService'
import MarkdownIt from 'markdown-it'
import Token from 'markdown-it/lib/token'

const md = new MarkdownIt()

export async function formatMarkdown(document: TextDocument): Promise<Array<TextEdit>> {
  const source = document.getText()
  const tokens = md.parse(source, {})

  const editsPromise = tokens
    .filter((t): t is Token => t.type === 'fence' && t.tag === 'code' && t.info === 'java')
    .map(async token => {
      const startIndex = source.indexOf(token.content)
      const code = await FormatService.getInstance().formatCode(token.content)

      const range = new Range(
        document.positionAt(startIndex),
        document.positionAt(startIndex + token.content.length)
      )

      return TextEdit.replace(range, code)
    })

  return Promise.all(editsPromise)
}
