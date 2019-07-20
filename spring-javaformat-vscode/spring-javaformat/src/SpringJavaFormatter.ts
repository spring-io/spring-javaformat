import {
  DocumentFormattingEditProvider,
  TextDocument,
  FormattingOptions,
  CancellationToken,
  ProviderResult,
  TextEdit,
  Range
} from 'vscode'

import FormatService from './FormatService'

export default class SpringJavaFormatter implements DocumentFormattingEditProvider {
  provideDocumentFormattingEdits(
    document: TextDocument,
    options: FormattingOptions,
    token: CancellationToken
  ): ProviderResult<TextEdit[]> {
    return FormatService.getInstance()
      .formatCode(document.getText())
      .then(content => {
        const range = new Range(document.positionAt(0), document.positionAt(document.getText().length))
        return [TextEdit.replace(range, content)]
      })
  }
}
