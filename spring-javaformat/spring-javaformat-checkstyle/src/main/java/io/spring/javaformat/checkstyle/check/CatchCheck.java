package io.spring.javaformat.checkstyle.check;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Checks that catch blocks follow Spring conventions.
 *
 * @author Phillip Webb
 */
public class CatchCheck extends AbstractCheck {

	@Override
	public int[] getDefaultTokens() {
		return getAcceptableTokens();
	}

	@Override
	public int[] getAcceptableTokens() {
		return new int[] { TokenTypes.LITERAL_CATCH };
	}

	@Override
	public int[] getRequiredTokens() {
		return CommonUtils.EMPTY_INT_ARRAY;
	}

	@Override
	public void visitToken(DetailAST ast) {
		if (ast.getType() == TokenTypes.LITERAL_CATCH) {
			visitCatch(ast);
		}
	}

	private void visitCatch(DetailAST ast) {
		DetailAST child = ast.getFirstChild();
		while (child != null && child.getType() != TokenTypes.PARAMETER_DEF) {
			child = child.getNextSibling();
		}
		if (child != null) {
			visitParameterDef(child);
		}
	}

	private void visitParameterDef(DetailAST ast) {
		DetailAST lastChild = ast.getLastChild();
		if (lastChild != null && lastChild.getType() == TokenTypes.IDENT) {
			checkIdent(lastChild);
		}
	}

	private void checkIdent(DetailAST ast) {
		String text = ast.getText();
		if (text.length() == 1) {
			log(ast.getLineNo(), ast.getColumnNo(), "catch.singleLetter");
		}
	}

}
