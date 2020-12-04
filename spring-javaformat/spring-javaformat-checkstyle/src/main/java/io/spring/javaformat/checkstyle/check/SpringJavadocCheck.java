/*
 * Copyright 2017-2020 the original author or authors.
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

package io.spring.javaformat.checkstyle.check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.TextBlock;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Checks that the javadoc comments follow Spring conventions.
 *
 * @author Phillip Webb
 */
public class SpringJavadocCheck extends AbstractSpringCheck {

	private static final List<Pattern> CASE_CHECKED_TAG_PATTERNS;
	static {
		List<Pattern> patterns = new ArrayList<>();
		patterns.add(Pattern.compile("@param\\s+\\S+\\s+(.*)"));
		patterns.add(Pattern.compile("@throws\\s+\\S+\\s+(.*)"));
		patterns.add(Pattern.compile("@return\\s+(.*)"));
		CASE_CHECKED_TAG_PATTERNS = Collections.unmodifiableList(patterns);
	}

	private static final List<Pattern> BANNED_TAGS;
	static {
		List<Pattern> patterns = new ArrayList<>();
		patterns.add(Pattern.compile("(@soundtrack)\\s+.*"));
		BANNED_TAGS = Collections.unmodifiableList(patterns);
	}

	private static final Pattern SINCE_TAG_PATTERN = Pattern.compile("@since\\s+(.*)");

	private static final Pattern AT_TAG_PATTERN = Pattern.compile("@\\w+\\s+.*");

	private static final Pattern NON_JAVADOC_COMMENT = Pattern.compile("\\(non-Javadoc\\)", Pattern.CASE_INSENSITIVE);

	private static final Set<Integer> TOP_LEVEL_TYPES;
	static {
		Set<Integer> topLevelTypes = new HashSet<Integer>();
		topLevelTypes.add(TokenTypes.INTERFACE_DEF);
		topLevelTypes.add(TokenTypes.CLASS_DEF);
		topLevelTypes.add(TokenTypes.ENUM_DEF);
		topLevelTypes.add(TokenTypes.ANNOTATION_DEF);
		TOP_LEVEL_TYPES = Collections.unmodifiableSet(topLevelTypes);
	}

	private boolean requireSinceTag;

	private boolean publicOnlySinceTags;

	private boolean allowNonJavadocComments;

	private Map<Integer, TextBlock> blockComments;

	@Override
	public int[] getDefaultTokens() {
		return new int[] { TokenTypes.INTERFACE_DEF, TokenTypes.CLASS_DEF, TokenTypes.ENUM_DEF,
				TokenTypes.ANNOTATION_DEF, TokenTypes.METHOD_DEF, TokenTypes.CTOR_DEF };
	}

	@Override
	public int[] getAcceptableTokens() {
		return new int[] { TokenTypes.INTERFACE_DEF, TokenTypes.CLASS_DEF, TokenTypes.ENUM_DEF,
				TokenTypes.ANNOTATION_DEF, TokenTypes.METHOD_DEF, TokenTypes.CTOR_DEF, TokenTypes.ENUM_CONSTANT_DEF,
				TokenTypes.ANNOTATION_FIELD_DEF };
	}

	@Override
	public void beginTree(DetailAST rootAST) {
		super.beginTree(rootAST);
		this.blockComments = new HashMap<>();
		FileContents contents = getFileContents();
		for (List<TextBlock> blockComments : contents.getBlockComments().values()) {
			for (TextBlock blockComment : blockComments) {
				this.blockComments.put(blockComment.getEndLineNo(), blockComment);
			}
		}
	}

	@Override
	public void visitToken(DetailAST ast) {
		int lineNumber = ast.getLineNo();
		TextBlock javadoc = getFileContents().getJavadocBefore(lineNumber);
		if (javadoc != null) {
			checkJavadoc(ast, javadoc);
		}
		if (!this.allowNonJavadocComments) {
			checkForNonJavadocComments(javadoc);
			checkForNonJavadocComments(getBlockCommentBefore(lineNumber));
		}
	}

	public TextBlock getBlockCommentBefore(int lineNoBefore) {
		FileContents contents = getFileContents();
		int lineNo = lineNoBefore - 1;
		while (lineNo > 0 && (contents.lineIsBlank(lineNo) || contents.lineIsComment(lineNo))) {
			lineNo--;
		}
		return this.blockComments.get(lineNo);
	}

	private void checkJavadoc(DetailAST ast, TextBlock javadoc) {
		checkBannedTags(ast, javadoc);
		checkTagCase(ast, javadoc);
		checkSinceTag(ast, javadoc);
		checkMethodJavaDoc(ast, javadoc);
	}

	private void checkBannedTags(DetailAST ast, TextBlock javadoc) {
		String[] text = javadoc.getText();
		for (int i = 0; i < text.length; i++) {
			for (Pattern pattern : BANNED_TAGS) {
				Matcher matcher = pattern.matcher(text[i]);
				if (matcher.find()) {
					String tagName = matcher.group(1).trim();
					log(javadoc.getStartLineNo() + i, tagName.length(), "javadoc.bannedTag", tagName);
				}
			}
		}
	}

	private void checkTagCase(DetailAST ast, TextBlock javadoc) {
		String[] text = javadoc.getText();
		for (int i = 0; i < text.length; i++) {
			for (Pattern pattern : CASE_CHECKED_TAG_PATTERNS) {
				Matcher matcher = pattern.matcher(text[i]);
				if (matcher.find()) {
					String description = matcher.group(1).trim();
					if (startsWithUppercase(description)) {
						log(javadoc.getStartLineNo() + i, text[i].length() - description.length(), "javadoc.badCase");
					}
				}
			}
		}
	}

	private void checkSinceTag(DetailAST ast, TextBlock javadoc) {
		if (!TOP_LEVEL_TYPES.contains(ast.getType())) {
			return;
		}
		String[] text = javadoc.getText();
		DetailAST interfaceDef = getInterfaceDef(ast);
		boolean privateType = !isPublicOrProtected(ast) && (interfaceDef == null || !isPublicOrProtected(interfaceDef));
		boolean innerType = ast.getParent() != null;
		boolean found = false;
		for (int i = 0; i < text.length; i++) {
			Matcher matcher = SINCE_TAG_PATTERN.matcher(text[i]);
			if (matcher.find()) {
				found = true;
				String description = matcher.group(1).trim();
				if (this.publicOnlySinceTags && privateType) {
					log(javadoc.getStartLineNo() + i, text[i].length() - description.length(), "javadoc.publicSince");
				}
			}
		}
		if (this.requireSinceTag && !innerType && !found && !(this.publicOnlySinceTags && privateType)) {
			log(javadoc.getStartLineNo(), 0, "javadoc.missingSince");
		}
	}

	private void checkMethodJavaDoc(DetailAST ast, TextBlock javadoc) {
		if (TokenTypes.METHOD_DEF != ast.getType()) {
			return;
		}
		String[] text = javadoc.getText();
		for (int i = 0; i < text.length; i++) {
			Matcher matcher = AT_TAG_PATTERN.matcher(text[i]);
			if (matcher.find() && i > 0 && text[i - 1].trim().equals("*")) {
				log(javadoc.getStartLineNo() + i - 1, 0, "javadoc.emptyLineBeforeTag");
			}
		}
	}

	private boolean startsWithUppercase(String description) {
		return description.length() > 0 && Character.isUpperCase(description.charAt(0));
	}

	private void checkForNonJavadocComments(TextBlock block) {
		if (block == null) {
			return;
		}
		String[] text = block.getText();
		for (int i = 0; i < text.length; i++) {
			if (NON_JAVADOC_COMMENT.matcher(text[i]).find()) {
				log(block.getStartLineNo() + i - 1, 0, "javadoc.nonJavadocComment");
			}
		}
	}

	public void setRequireSinceTag(boolean requireSinceTag) {
		this.requireSinceTag = requireSinceTag;
	}

	public void setPublicOnlySinceTags(boolean publicOnlySinceTags) {
		this.publicOnlySinceTags = publicOnlySinceTags;
	}

	public void setAllowNonJavadocComments(boolean allowNonJavadocComments) {
		this.allowNonJavadocComments = allowNonJavadocComments;
	}

	private DetailAST getInterfaceDef(DetailAST ast) {
		return findParent(ast, TokenTypes.INTERFACE_DEF);
	}

	private DetailAST findParent(DetailAST ast, int classDef) {
		while (ast != null) {
			if (ast.getType() == classDef) {
				return ast;
			}
			ast = ast.getParent();
		}
		return null;
	}

	private boolean isPublicOrProtected(DetailAST ast) {
		DetailAST modifiers = ast.findFirstToken(TokenTypes.MODIFIERS);
		if (modifiers == null) {
			return false;
		}
		return modifiers.findFirstToken(TokenTypes.LITERAL_PUBLIC) != null
				|| modifiers.findFirstToken(TokenTypes.LITERAL_PROTECTED) != null;
	}

}
