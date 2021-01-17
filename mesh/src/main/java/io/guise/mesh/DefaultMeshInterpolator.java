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

import static com.globalmentor.java.CharSequences.*;
import static com.globalmentor.java.Conditions.*;
import static java.lang.String.format;
import static java.util.Objects.*;

import java.util.Optional;
import java.util.function.Function;

import javax.annotation.*;

/**
 * Default Guise Mesh interpolation implementation.
 * @author Garret Wilson
 */
public class DefaultMeshInterpolator implements MeshInterpolator {

	/** The default Guise Mesh left interpolation expression delimiter. */
	public static final String LEFT_EXPRESSION_DELIMITER = "^{";

	/** The default Guise Mesh right interpolation expression delimiter. */
	public static final String RIGHT_EXPRESSION_DELIMITER = "}";

	/** Singleton shared instance. */
	public static final DefaultMeshInterpolator INSTANCE = new DefaultMeshInterpolator();

	/**
	 * {@inheritDoc}
	 * @implSpec To evaluate expressions this implementation delegates to {@link MexlEvaluator#findExpressionResult(MeshContext, CharSequence)} and uses the
	 *           {@link Object#toString()} value of the returned result, or the empty string if no result is returned.
	 */
	@Override
	public Optional<CharSequence> findInterpolation(final MeshContext context, final CharSequence text, final MexlEvaluator evaluator)
			throws MeshInterpolationException, MexlException {
		return findInterpolation(text, LEFT_EXPRESSION_DELIMITER, RIGHT_EXPRESSION_DELIMITER,
				expression -> evaluator.findExpressionResult(context, expression).map(Object::toString).orElse(""));
	}

	/**
	 * Interpolates the given text and returns the interpolated result if there was a change.
	 * @implSpec This current implementation only supports a left delimiter of exactly two characters and a right delimiter of exactly one character.
	 * @implSpec This implementation does not support nested delimiters.
	 * @implSpec This implementation does not fully support surrogate characters as expression delimiters.
	 * @param text The text to interpolate.
	 * @param leftExpressionDelimiter The string demarcating the left side of an interpolation expression.
	 * @param rightExpressionDelimiter The string demarcating the right side of an interpolation expression.
	 * @param evaluator The strategy for evaluating the expression and returning a result; a return value of <code>null</code> will be interpolated as the string
	 *          <code>"null"</code>.
	 * @return The interpolated text if interpolation actually occurred; will be empty if no changes were made to the text.
	 * @throws MeshInterpolationException if the interpolation syntax of the given text is incorrect.
	 * @throws MexlException if there was an error parsing or otherwise processing an expression.
	 */
	protected static Optional<CharSequence> findInterpolation(@Nonnull final CharSequence text, final String leftExpressionDelimiter,
			final String rightExpressionDelimiter, @Nonnull final Function<CharSequence, CharSequence> evaluator) throws MeshInterpolationException {
		checkArgument(leftExpressionDelimiter.length() == 2,
				"Interpolation left expression delimiter `%s` not supported; currently only a left delimiter of exactly two characters is supported.",
				leftExpressionDelimiter);
		checkArgument(rightExpressionDelimiter.length() == 1,
				"Interpolation right expression delimiter `%s` not supported; currently only a right delimiter of exactly one character is supported.",
				leftExpressionDelimiter);
		requireNonNull(evaluator);
		final char signalChar = leftExpressionDelimiter.charAt(0);
		final char leftDelimiterChar = leftExpressionDelimiter.charAt(1);
		final char rightDelimiterChar = rightExpressionDelimiter.charAt(0);
		StringBuilder interpolationBuilder = null; //we may not need to interpolate
		final int length = text.length();
		//		int index = 0; //start at the beginning of the character sequence
		for(int index = 0; index < length;) {
			final int signalIndex = indexOf(text, signalChar, index);
			if(signalIndex == -1) { //if no signal was found
				if(interpolationBuilder != null) { //if we have started building, add what is remaining
					assert index > 0 : "If there is no signal on the first round, there is no way we could have started building yet.";
					interpolationBuilder.append(text, index, length); //append everything up to but not including the last search location
				}
				break; //short-circuit
			}
			if(signalIndex == length - 1 || text.charAt(signalIndex + 1) != leftDelimiterChar) { //false signal (signal at end of string, or not followed by a delimiter)
				if(interpolationBuilder != null) { //add the false signal only if we have started building; otherwise, just skip over it (we always skip it)
					assert signalIndex > 0 : "We would not have started building yet if the signal were at the beginning.";
					interpolationBuilder.append(text, index, signalIndex); //append only what we've searched up to this point
					interpolationBuilder.append(signalChar); //what we found was a false signal, so add it too
				}
				index = signalIndex + 1;
				continue;
			}
			final int expressionStartIndex = signalIndex + 2;
			final int rightDelimiterIndex = indexOf(text, rightDelimiterChar, expressionStartIndex); //search could start at end of string, which is allowed by the API
			if(rightDelimiterIndex == -1) {
				throw new MeshInterpolationException(
						format("Mesh interpolation string `%s` missing ending delimiter for interpolation expression starting at index %d.", text, signalIndex));
			}
			final int expressionEndIndex = rightDelimiterIndex;
			final CharSequence expression = text.subSequence(expressionStartIndex, expressionEndIndex);
			final CharSequence result = evaluator.apply(expression);
			if(interpolationBuilder != null) { //if we've starting building already (this is the first point we know we have to)
				assert signalIndex > 0 : "We would not have started building yet if the signal were at the beginning.";
				interpolationBuilder.append(text, index, signalIndex); //append only what we've searched for in this iteration (there may have been a false signal after an earlier expression)
			} else { //if we haven't started building
				interpolationBuilder = new StringBuilder(length + result.length() + 16); //make a shot-in-the-dark guess about capacity, which is more accurate than nothing
				if(signalIndex > 0) { //if there is anything to add before the signal  
					interpolationBuilder.append(text, 0, signalIndex); //append everything up to but not including the signal, including any false signals
				}
			}
			interpolationBuilder.append(result);
			index = rightDelimiterIndex + 1; //start searching after the entire expression block
		}
		return Optional.ofNullable(interpolationBuilder);
	}

}
