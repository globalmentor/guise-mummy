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

import java.util.Optional;

import javax.annotation.*;

/**
 * Pluggable strategy for evaluating Mesh Expression Language (MEXL) expressions.
 * @apiNote This strategy is primarily to provide an additional layer of indirection to be able to change expression evaluation libraries or write a new one if
 *          the currently available libraries are insufficient.
 * @author Garret Wilson
 */
public interface MexlEvaluator {

	/**
	 * Evaluates an expression using the given meshing context.
	 * @param context The context of meshing.
	 * @param expression The expression to evaluate.
	 * @return The result of the expression.
	 * @throws MexlException if there was an error parsing or otherwise processing the expression.
	 */
	public Object evaluate(@Nonnull final MeshContext context, @Nonnull final CharSequence expression) throws MexlException;

	/**
	 * Evaluates an expression using the given meshing context and returns the result as an optional value. If the expression evaluates to an instance of
	 * {@link Optional}, that instance will be returned.
	 * @apiNote This is a convenience method for evaluating an expression and returning an optional value. It will never return <code>null</code>. However it will
	 *          not wrap a resulting {@link Optional} instance in another {@link Optional}. Thus this method functions analogously to
	 *          {@link Optional#flatMap(java.util.function.Function)}.
	 * @implSpec The default implementation delegates to {@link #evaluate(MeshContext, CharSequence)}.
	 * @param context The context of meshing.
	 * @param expression The expression to evaluate.
	 * @return The result of the expression, which will be empty if the expression evaluated to <code>null</code>.
	 * @throws MexlException if there was an error parsing or otherwise processing the expression.
	 */
	public default Optional<Object> findExpressionResult(@Nonnull final MeshContext context, @Nonnull final CharSequence expression) throws MexlException {
		final Object result = evaluate(context, expression);
		@SuppressWarnings("unchecked")
		final Optional<Object> optionalResult = result instanceof Optional ? (Optional<Object>)result : Optional.ofNullable(result);
		return optionalResult;
	}

}
