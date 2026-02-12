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

package dev.guise.mesh;

import java.util.Optional;

import org.jspecify.annotations.*;

/// A pluggable interpolation engine for Guise Mesh.
/// @author Garret Wilson
public interface MeshInterpolator {

	/// Determines if the given text has one or more expressions to interpolate. No evaluations are performed, and syntax is not guaranteed to be fully or even
	/// partially validated.
	///
	/// The result of this method must be consistent with the presence of a result from [#findInterpolation(MeshContext, CharSequence, MexlEvaluator)] for
	/// the same valid input text. For example, if [#findInterpolation(MeshContext, CharSequence, MexlEvaluator)] returns a value for which
	/// [Optional#isPresent()] is `false`, this method must also return `false` for the same input text.
	/// @param text The text being considered for interpolation.
	/// @return `true` if the text has expressions to be interpolated
	/// @throws MeshInterpolationException if the interpolation syntax of the given text is incorrect.
	public boolean hasInterpolation(@NonNull CharSequence text) throws MeshInterpolationException;

	/// Interpolates the given text and returns the interpolated result if there was a change.
	///
	/// The presence of a result must be consistent with [#hasInterpolation(CharSequence)] for the same input text.
	/// @param context The context of meshing.
	/// @param text The text to interpolate.
	/// @param evaluator The strategy for evaluating Mesh Expression Language (MEXL) expressions.
	/// @return The interpolated text if interpolation actually occurred; will be empty if no changes were made to the text.
	/// @throws MeshInterpolationException if the interpolation syntax of the given text is incorrect.
	/// @throws MexlException if there was an error parsing or otherwise processing an expression.
	public Optional<CharSequence> findInterpolation(@NonNull MeshContext context, @NonNull CharSequence text, @NonNull MexlEvaluator evaluator)
			throws MeshInterpolationException, MexlException;

	/// Interpolates the given text and returns the interpolated result.
	/// @apiNote This is a convenience method that functions equivalently to [#findInterpolation(MeshContext, CharSequence, MexlEvaluator)] except that the
	///          text will be returned even if no changes were made.
	/// @implSpec The default implementation delegates to [#findInterpolation(MeshContext, CharSequence, MexlEvaluator)].
	/// @param context The context of meshing.
	/// @param text The text to interpolate.
	/// @param evaluator The strategy for evaluating Mesh Expression Language (MEXL) expressions.
	/// @return The interpolated text if interpolation actually occurred; will be empty if no changes were made to the text.
	/// @throws MeshInterpolationException if the interpolation syntax of the given text is incorrect.
	/// @throws MexlException if there was an error parsing or otherwise processing an expression.
	public default CharSequence interpolate(@NonNull final MeshContext context, @NonNull final CharSequence text, @NonNull final MexlEvaluator evaluator)
			throws MeshInterpolationException, MexlException {
		return findInterpolation(context, text, evaluator).orElse(text);
	}

}
