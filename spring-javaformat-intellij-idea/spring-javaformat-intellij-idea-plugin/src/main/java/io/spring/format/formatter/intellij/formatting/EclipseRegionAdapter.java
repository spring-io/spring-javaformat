/*
 * Copyright 2017-present the original author or authors.
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

package io.spring.format.formatter.intellij.formatting;

import java.util.ArrayList;
import java.util.List;

import com.intellij.openapi.util.TextRange;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

/**
 * Adapter class to expose an IntelliJ {@link TextRange} as an Eclipse {@link IRegion}.
 *
 * @author Phillip Webb
 */
class EclipseRegionAdapter extends Region {

	private static final IRegion[] NO_REGIONS = {};

	EclipseRegionAdapter(TextRange range) {
		super(range.getStartOffset(), range.getLength());
	}

	static IRegion[] asArray(List<TextRange> ranges) {
		if (ranges == null) {
			return NO_REGIONS;
		}
		List<IRegion> regions = new ArrayList<>(ranges.size());
		for (TextRange range : ranges) {
			regions.add(new EclipseRegionAdapter(range));
		}
		return regions.toArray(new IRegion[regions.size()]);
	}

}
