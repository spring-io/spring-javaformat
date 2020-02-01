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

package io.spring.javaformat.checkstyle.filter;

import java.util.Set;
import java.util.SortedSet;

import com.puppycrawl.tools.checkstyle.DefaultContext;
import com.puppycrawl.tools.checkstyle.ModuleFactory;
import com.puppycrawl.tools.checkstyle.PackageNamesLoader;
import com.puppycrawl.tools.checkstyle.PackageObjectFactory;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.api.Context;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.LocalizedMessage;

/**
 * Base class for {@link AbstractCheck checks} that act as a filter for a single child.
 *
 * @author Phillip Webb
 */
public class CheckFilter extends AbstractCheck {

	private Context childContext;

	private AbstractCheck check;

	@Override
	public void finishLocalSetup() {
		DefaultContext context = new DefaultContext();
		context.add("severity", getSeverity());
		context.add("tabWidth", String.valueOf(getTabWidth()));
		this.childContext = context;
	}

	@Override
	public void setupChild(Configuration childConf) throws CheckstyleException {
		ModuleFactory moduleFactory = createModuleFactory();
		String name = childConf.getName();
		Object module = moduleFactory.createModule(name);
		if (!(module instanceof AbstractCheck)) {
			throw new CheckstyleException("OptionalCheck is not allowed as a parent of " + name
					+ " Please review 'Parent Module' section for this Check.");
		}
		if (this.check != null) {
			throw new CheckstyleException("Can only make a single check optional");
		}
		AbstractCheck check = (AbstractCheck) module;
		check.contextualize(this.childContext);
		check.configure(childConf);
		check.init();
		this.check = check;
	}

	private ModuleFactory createModuleFactory() {
		try {
			ClassLoader classLoader = AbstractCheck.class.getClassLoader();
			Set<String> packageNames = PackageNamesLoader.getPackageNames(classLoader);
			return new PackageObjectFactory(packageNames, classLoader);
		}
		catch (CheckstyleException ex) {
			throw new IllegalStateException(ex);
		}
	}

	@Override
	public int[] getDefaultTokens() {
		return this.check.getDefaultTokens();
	}

	@Override
	public int[] getAcceptableTokens() {
		return this.check.getAcceptableTokens();
	}

	@Override
	public int[] getRequiredTokens() {
		return this.check.getRequiredTokens();
	}

	@Override
	public boolean isCommentNodesRequired() {
		return this.check.isCommentNodesRequired();
	}

	@Override
	public SortedSet<LocalizedMessage> getMessages() {
		return this.check.getMessages();
	}

	@Override
	public void beginTree(DetailAST rootAST) {
		this.check.setFileContents(getFileContents());
		this.check.clearMessages();
		this.check.beginTree(rootAST);
	}

	@Override
	public void finishTree(DetailAST rootAST) {
		this.check.finishTree(rootAST);
	}

	@Override
	public void visitToken(DetailAST ast) {
		this.check.visitToken(ast);
	}

	@Override
	public void leaveToken(DetailAST ast) {
		this.check.leaveToken(ast);
	}

}
