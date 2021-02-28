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
	 * Determines if the given text has one or more expressions to interpolate. No evaluations are performed, and syntax is not guaranteed to be fully or even
	 * partially validated.
	 * <p>
	 * The result of this method must be consistent with the presence of a result from {@link #findInterpolation(MeshContext, CharSequence, MexlEvaluator)} for
	 * the same valid input text. For example, if {@link #findInterpolation(MeshContext, CharSequence, MexlEvaluator)} returns a value for which
	 * {@link Optional#isPresent()} is <code>false</code>, this method must also return <code>false</code> for the same input text.
	 * </p>
	 * @param text The text being considered for interpolation.
	 * @return <code>true</code> if the text has expressions to be interpolated
	 * @throws MeshInterpolationException if the interpolation syntax of the given text is incorrect.
	 */
	public boolean hasInterpolation(@Nonnull CharSequence text) throws MeshInterpolationException;

	/**
	 * Interpolates the given text and returns the interpolated result if there was a change.
	 * <p>
	 * The presence of a result must be consistent with {@link #hasInterpolation(CharSequence)} for the same input text.
	 * </p>
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
