/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package com.guiseframework.validator;

/**An abstract implementation of a validator restricted to a range.
The step value is considered relative either to the minimum value, if available, the maximum value, if available, or zero, in that order or priority.
@param <V> The value type this validator supports.
@author Garret Wilson
*/
public abstract class AbstractRangeValidator<V> extends AbstractValidator<V> implements RangeValidator<V>
{
	
	/**The minimum value, inclusive, or <code>null</code> if the range has no lower bound.*/
	private final V minimum;

		/**@return The minimum value, inclusive, or <code>null</code> if the range has no lower bound.*/
		public V getMinimum() {return minimum;}

	/**The maximum value, inclusive, or <code>null</code> if the range has no upper bound.*/
	private final V maximum;

		/**@return The maximum value, inclusive, or <code>null</code> if the range has no upper bound.*/
		public V getMaximum() {return maximum;}

	/**The step amount, or <code>null</code> if the range has no increment value specified.*/
	private final V step;

		/**@return The step amount, or <code>null</code> if the range has no increment value specified.*/
		public V getStep() {return step;}

	/**Minimum, maximum, step, and value required constructor.
	@param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	@param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	@param step The step amount, or <code>null</code> if the range has no increment value specified.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	*/
	public AbstractRangeValidator(final V minimum, final V maximum, final V step, final boolean valueRequired)
	{
		super(valueRequired);	//construct the parent class
		this.minimum=minimum;
		this.maximum=maximum;
		this.step=step;
	}

}
