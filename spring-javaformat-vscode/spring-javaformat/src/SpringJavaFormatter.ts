import {
  DocumentFormattingEditProvider,
  TextDocument,
  FormattingOptions,
  CancellationToken,
  ProviderResult,
  TextEdit,
  Range
} from 'vscode'

import { formatMarkdown } from './formatters/MarkdownFormatter'
import { formatJava } from './formatters/JavaFormatter'

export default class SpringJavaFormatter implements DocumentFormattingEditProvider {
  provideDocumentFormattingEdits(
    document: TextDocument,
    options: FormattingOptions,
    token: CancellationToken
  ): ProviderResult<TextEdit[]> {
    if (document.languageId === 'java') {
      return formatJava(document)
    }
    if (document.languageId === 'markdown') {
      return formatMarkdown(document)
    }
    return []
  }
}
