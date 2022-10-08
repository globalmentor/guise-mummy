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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.urf.model.*;

/**
 * Tests of {@link JexlMexlEvaluator}.
 * @author Garret Wilson
 */
public class JexlMexlEvaluatorTest {

	@Test
	public void shouldRetrieveMapValue() {
		final MeshContext context = new DefaultMeshContext();
		context.setVariable("foo", Map.of("bar", 123));
		assertThat(JexlMexlEvaluator.INSTANCE.evaluate(context, "foo.bar"), is(123));
	}

	@Test
	public void shouldRetrieveUrfProperty() {
		final MeshContext context = new DefaultMeshContext();
		final UrfObject foo = new UrfObject();
		foo.setPropertyValueByHandle("bar", 123);
		context.setVariable("foo", foo);
		assertThat(JexlMexlEvaluator.INSTANCE.evaluate(context, "foo.bar"), is(123));
	}

	@Test
	public void shouldRetrieveUrfPropertyAsArray() {
		final MeshContext context = new DefaultMeshContext();
		final UrfObject foo = new UrfObject();
		foo.setPropertyValueByHandle("bar", 123);
		context.setVariable("foo", foo);
		assertThat(JexlMexlEvaluator.INSTANCE.evaluate(context, "foo[\"bar\"]"), is(123));
	}

	/** @see UrfResourceDescription#getPropertyCount() */
	@Test
	public void shouldNotSeeUrfPojoProperty() {
		final MeshContext context = new DefaultMeshContext();
		context.setVariable("foo", new UrfObject());
		assertThat(JexlMexlEvaluator.INSTANCE.evaluate(context, "foo.propertyCount"), is(nullValue()));
	}

	/** Verifies that our special URF resolver is used only for the URF object, and that it still allows map values to be looked up later in the chain. */
	@Test
	public void shouldRetrieveUrfMapKeyValue() {
		final MeshContext context = new DefaultMeshContext();
		final UrfObject foo = new UrfObject();
		foo.setPropertyValueByHandle("bar", Map.of("test", 123));
		context.setVariable("foo", foo);
		assertThat(JexlMexlEvaluator.INSTANCE.evaluate(context, "foo.bar.test"), is(123));
	}

	/** Verifies that an URF object within a map still has its properties visible (i.e. the map resolver did not prevent URF access later in the chain). */
	@Test
	public void shouldRetrieveMapUrfProperty() {
		final MeshContext context = new DefaultMeshContext();
		final UrfObject bar = new UrfObject();
		bar.setPropertyValueByHandle("test", 123);
		context.setVariable("foo", Map.of("bar", bar));
		assertThat(JexlMexlEvaluator.INSTANCE.evaluate(context, "foo.bar.test"), is(123));
	}

}
