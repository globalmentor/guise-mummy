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
	public Object evaluate(@Nonnull final MeshContext context, @Nonnull final String expression) throws MexlException;

}
