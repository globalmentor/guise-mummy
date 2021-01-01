/*
 * Copyright © 2020 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.mesh;

import static java.util.Spliterator.*;
import static java.util.Spliterators.*;
import static java.util.stream.Collectors.*;
import static java.util.stream.StreamSupport.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.*;

/**
 * Tests of {@link MeshIterator}.
 * @author Garret Wilson
 */
public class MeshIteratorTest {

	/** @see MeshIterator#toIterator(Object) */
	@Test
	void testToIteratorSupportsArrays() {
		assertThat("String[]", stream(spliteratorUnknownSize(MeshIterator.toIterator(new String[] {"one", "two", "three"}), ORDERED), false).collect(toList()),
				contains("one", "two", "three"));
		assertThat("Integer[]", stream(spliteratorUnknownSize(MeshIterator.toIterator(new Integer[] {1, 2, 3}), ORDERED), false).collect(toList()),
				contains(1, 2, 3));
		assertThat("int[]", stream(spliteratorUnknownSize(MeshIterator.toIterator(new int[] {1, 2, 3}), ORDERED), false).collect(toList()), contains(1, 2, 3));
		assertThat("long[]", stream(spliteratorUnknownSize(MeshIterator.toIterator(new long[] {1L, 2L, 3L}), ORDERED), false).collect(toList()),
				contains(1L, 2L, 3L));
		assertThat("double[]", stream(spliteratorUnknownSize(MeshIterator.toIterator(new double[] {1.0, 2.0, 3.0}), ORDERED), false).collect(toList()),
				contains(1.0, 2.0, 3.0));
	}

}
