/*
 * Copyright © 2020 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.mesh;

import static com.github.npathai.hamcrestopt.OptionalMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.util.*;

import org.junit.jupiter.api.Test;

/**
 * Tests of {@link DefaultMeshContext}.
 * @author Garret Wilson
 */
public class DefaultMeshContextTest {

	@Test
	@SuppressWarnings("try")
	void testNestedScopes() {
		final MeshContext context = new DefaultMeshContext(MeshScope.create(Map.of("test", "foo")));
		assertThat("Root scope variable visible initially.", context.findVariable("test"), isPresentAndIs("foo"));
		try (final MeshContext.ScopeNesting scopeNesting = context.nestScope()) {
			context.setVariable("test", "bar");
			context.setVariable("other", "foobar");
			assertThat("Nested scope overrides root scope.", context.findVariable("test"), isPresentAndIs("bar"));
			assertThat("New nested scope variable visible.", context.findVariable("other"), isPresentAndIs("foobar"));
		}
		assertThat("Root scope variable visible again after nested scope ends.", context.findVariable("test"), isPresentAndIs("foo"));
		assertThat("New nested scope variables gone after nested scope ends.", context.findVariable("other"), isEmpty());
	}

}
