/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.format.formatter.intellij.codestyle;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Tests for {@link SpringCodeStyleManager}.
 *
 * @author Phillip Webb
 */
public class SpringCodeStyleManagerTests {

	@Mock
	private CodeStyleManager delegate;

	@Mock
	private SpringReformatter springReformatter;

	private SpringCodeStyleManager styleManager;

	@Mock
	private PsiFile file;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.styleManager = new SpringCodeStyleManager(this.delegate,
				this.springReformatter);
	}

	@Test
	public void reformatTextWithOffsetWhenCantFormatShouldCallDelegate() {
		given(this.springReformatter.canReformat(any())).willReturn(false);
		this.styleManager.reformatText(this.file, 10, 20);
		verify(this.delegate).reformatText(this.file, 10, 20);
	}

	@Test
	public void reformatTextWithOffsetWhenCanFormatShouldCallFormatter() {
		given(this.springReformatter.canReformat(any())).willReturn(true);
		Set<TextRange> ranges = new HashSet<>(Arrays.asList(new TextRange(10, 20)));
		this.styleManager.reformatText(this.file, 10, 20);
		verify(this.springReformatter).reformat(this.file, ranges);
		verifyZeroInteractions(this.delegate);
	}

	@Test
	public void reformatTextWithRangeWhenCantFormatShouldCallDelegate() {
		given(this.springReformatter.canReformat(any())).willReturn(false);
		Collection<TextRange> ranges = Arrays.asList(new TextRange(10, 20));
		this.styleManager.reformatText(this.file, ranges);
		verify(this.delegate).reformatText(this.file, ranges);
	}

	@Test
	public void reformatTextWithRangeWhenCanFormatShouldCallFormatter() {
		given(this.springReformatter.canReformat(any())).willReturn(true);
		Collection<TextRange> ranges = Arrays.asList(new TextRange(10, 20));
		this.styleManager.reformatText(this.file, ranges);
		verify(this.springReformatter).reformat(this.file, ranges);
		verifyZeroInteractions(this.delegate);
	}

	@Test
	public void reformatTextWithContextWhenCantFormatShouldCallDelegate() {
		given(this.springReformatter.canReformat(any())).willReturn(false);
		Collection<TextRange> ranges = Arrays.asList(new TextRange(10, 20));
		this.styleManager.reformatTextWithContext(this.file, ranges);
		verify(this.delegate).reformatTextWithContext(this.file, ranges);
	}

	@Test
	public void reformatTextWithContextWhenCanFormatShouldCallFormatter() {
		given(this.springReformatter.canReformat(any())).willReturn(true);
		Collection<TextRange> ranges = Arrays.asList(new TextRange(10, 20));
		this.styleManager.reformatTextWithContext(this.file, ranges);
		verify(this.springReformatter).reformat(this.file, ranges);
		verifyZeroInteractions(this.delegate);
	}

}
