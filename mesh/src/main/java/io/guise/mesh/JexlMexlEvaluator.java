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

import static java.lang.String.format;
import static java.util.Objects.*;

import java.util.*;

import javax.annotation.*;

import org.apache.commons.jexl3.*;
import org.apache.commons.jexl3.introspection.*;

import io.urf.URF;
import io.urf.model.UrfResourceDescription;

/**
 * Pluggable strategy for evaluating Mesh Expression Language (MEXL) expressions.
 * @apiNote This strategy is primarily to provide an additional layer of indirection to be able to change expression evaluation libraries or write a new one if
 *          the currently available libraries are insufficient.
 * @implSpec This implementation supports retrieving {@link UrfResourceDescription} properties using an URF property handle.
 * @author Garret Wilson
 */
public class JexlMexlEvaluator implements MexlEvaluator {

	/**
	 * Custom property resolver for retrieving properties from a {@link UrfResourceDescription} by property handle.
	 * @implSpec This implementation does not support setting properties.
	 * @see UrfResourceDescriptionPropertyGet
	 */
	private static final JexlUberspect.PropertyResolver URF_PROPERTY_RESOLVER = new JexlUberspect.PropertyResolver() {
		@Override
		public JexlPropertyGet getPropertyGet(final JexlUberspect uber, final Object object, final Object identifier) {
			final String propertyHandle = identifier.toString();
			if(!(object instanceof UrfResourceDescription)) {
				return null;
			}
			return new UrfResourceDescriptionPropertyGet(propertyHandle);
		}

		@Override
		public JexlPropertySet getPropertySet(final JexlUberspect uber, final Object object, final Object identifier, final Object arg) {
			throw new UnsupportedOperationException("Setting URF properties not supported.");
		}
	};

	/**
	 * A custom resolver for handling additional types.
	 * @implSpec For an instance of {@link UrfResourceDescription}, an {@link #URF_PROPERTY_RESOLVER} is used; otherwise, the default
	 *           {@link JexlUberspect#JEXL_STRATEGY} is used.
	 */
	private static final JexlUberspect.ResolverStrategy RESOLVER_STRATEGY = new JexlUberspect.ResolverStrategy() {
		@Override
		public List<JexlUberspect.PropertyResolver> apply(final JexlOperator operator, final Object object) {
			if(object instanceof UrfResourceDescription) {
				return List.of(URF_PROPERTY_RESOLVER);
			}
			return JexlUberspect.JEXL_STRATEGY.apply(operator, object);
		}
	};

	/**
	 * Singleton shared instance.
	 * @implNote This variable must be initialized after the property resolver and resolver strategy private constant instances.
	 */
	public static final JexlMexlEvaluator INSTANCE = new JexlMexlEvaluator();

	private final JexlEngine jexl;

	/** This class cannot be publicly instantiated. */
	private JexlMexlEvaluator() {
		jexl = new JexlBuilder().strategy(RESOLVER_STRATEGY).create();
	}

	@Override
	public Object evaluate(final MeshContext context, final String expression) throws MexlException {
		try {
			return jexl.createExpression(expression).evaluate(new MeshJexlContext(context));
		} catch(final JexlException jexlException) {
			throw new MexlException(format("Error in MEXL expression `%s`: %s", expression, jexlException.getMessage()), jexlException);
		}
	}

	/**
	 * A JEXL context that delegates to the meshing context.
	 * @implSpec This implementation does not support setting variables.
	 */
	private static class MeshJexlContext implements JexlContext {

		private final MeshContext meshContext;

		/**
		 * Constructor.
		 * @param meshContext The meshing context.
		 */
		public MeshJexlContext(@Nonnull final MeshContext meshContext) {
			this.meshContext = requireNonNull(meshContext);
		}

		/**
		 * {@inheritDoc}
		 * @implSpec This implementation delegates to {@link MeshContext#hasVariable(String)}.
		 */
		@Override
		public boolean has(final String name) {
			return meshContext.hasVariable(name);
		}

		/**
		 * {@inheritDoc}
		 * @implSpec This implementation delegates to {@link MeshContext#findVariable(String)} and returns <code>null</code> if the variable is not present.
		 */
		@Override
		public Object get(final String name) {
			return meshContext.findVariable(name).orElse(null);
		}

		/**
		 * {@inheritDoc}
		 * @implSpec This implementation delegates to {@link MeshContext#setVariable(String, Object)}.
		 */
		@Override
		public void set(final String name, final Object value) {
			meshContext.setVariable(name, value);
		}

	}

	/**
	 * Strategy for retrieving a property from an {@link UrfResourceDescription} by property handle.
	 * @see UrfResourceDescription#findPropertyValueByHandle(String)
	 */
	private static class UrfResourceDescriptionPropertyGet implements JexlPropertyGet {

		private final String propertyHandle;

		/**
		 * Constructor.
		 * @param propertyHandle The property handle.
		 * @throws IllegalArgumentException if the given property handle is not a valid URF property handle.
		 */
		public UrfResourceDescriptionPropertyGet(@Nonnull final String propertyHandle) {
			this.propertyHandle = URF.Handle.checkArgumentValid(propertyHandle);
		}

		@Override
		public Object invoke(final Object object) throws Exception {
			return ((UrfResourceDescription)object).findPropertyValueByHandle(propertyHandle).orElse(null);
		}

		@Override
		public Object tryInvoke(final Object object, final Object key) {
			if(object instanceof UrfResourceDescription && key instanceof String) {
				return ((UrfResourceDescription)object).findPropertyValueByHandle(propertyHandle).orElse(null);
			}
			return JexlEngine.TRY_FAILED;
		}

		@Override
		public boolean tryFailed(final Object rval) {
			return rval == JexlEngine.TRY_FAILED;
		}

		@Override
		public boolean isCacheable() {
			return true;
		}

	}

}
