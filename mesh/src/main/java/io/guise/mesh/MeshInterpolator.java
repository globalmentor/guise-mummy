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

import java.util.Optional;

import javax.annotation.*;

/**
 * A pluggable interpolation engine for Guise Mesh.
 * @author Garret Wilson
 */
public interface MeshInterpolator {

	/**
	 * Interpolates the given text and returns the interpolated result if there was a change.
	 * @param context The context of meshing.
	 * @param text The text to interpolate.
	 * @param evaluator The strategy for evaluating Mesh Expression Language (MEXL) expressions.
	 * @return The interpolated text if interpolation actually occurred; will be empty if no changes were made to the text.
	 * @throws MeshInterpolationException if the interpolation syntax of the given text is incorrect.
	 * @throws MexlException if there was an error parsing or otherwise processing an expression.
	 */
	public Optional<CharSequence> findInterpolation(@Nonnull MeshContext context, @Nonnull CharSequence text, @Nonnull MexlEvaluator evaluator)
			throws MeshInterpolationException, MexlException;

	/**
	 * Interpolates the given text and returns the interpolated result.
	 * @apiNote This is a convenience method that functions equivalently to {@link #findInterpolation(MeshContext, CharSequence, MexlEvaluator)} except that the
	 *          text will be returned even if no changes were made.
	 * @implSpec The default implementation delegates to {@link #findInterpolation(MeshContext, CharSequence, MexlEvaluator)}.
	 * @param context The context of meshing.
	 * @param text The text to interpolate.
	 * @param evaluator The strategy for evaluating Mesh Expression Language (MEXL) expressions.
	 * @return The interpolated text if interpolation actually occurred; will be empty if no changes were made to the text.
	 * @throws MeshInterpolationException if the interpolation syntax of the given text is incorrect.
	 * @throws MexlException if there was an error parsing or otherwise processing an expression.
	 */
	public default CharSequence interpolate(@Nonnull final MeshContext context, @Nonnull final CharSequence text, @Nonnull final MexlEvaluator evaluator)
			throws MeshInterpolationException, MexlException {
		return findInterpolation(context, text, evaluator).orElse(text);
	}

}
