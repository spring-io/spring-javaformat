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
import java.util.List;

import com.intellij.openapi.util.TextRange;
import org.eclipse.jface.text.IRegion;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EclipseRegionAdapter}.
 *
 * @author Phillip Webb
 */
public class EclipseRegionAdapterTests {

	@Test
	public void getOffsetShouldReturnStartOffset() throws Exception {
		IRegion region = new EclipseRegionAdapter(new TextRange(10, 20));
		assertThat(region.getOffset()).isEqualTo(10);
	}

	@Test
	public void getLengthShouldReturnLength() throws Exception {
		IRegion region = new EclipseRegionAdapter(new TextRange(10, 20));
		assertThat(region.getLength()).isEqualTo(10);
	}

	@Test
	public void asArrayWhenCollectionIsNullShouldReturnEmptyArray() throws Exception {
		IRegion[] regions = EclipseRegionAdapter.asArray(null);
		assertThat(regions).isNotNull().isEmpty();
	}

	@Test
	public void asArrayShouldReturnArray() throws Exception {
		List<TextRange> ranges = Arrays.asList(new TextRange(10, 20),
				new TextRange(30, 35));
		IRegion[] regions = EclipseRegionAdapter.asArray(ranges);
		assertThat(regions).hasSize(2);
		assertThat(regions[0].getOffset()).isEqualTo(10);
		assertThat(regions[0].getLength()).isEqualTo(10);
		assertThat(regions[1].getOffset()).isEqualTo(30);
		assertThat(regions[1].getLength()).isEqualTo(5);
	}

}
