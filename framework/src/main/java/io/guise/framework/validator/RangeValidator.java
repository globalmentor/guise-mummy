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

package io.guise.framework.validator;

/**
 * A validator restricting a value to a range. The step value is considered relative either to the minimum value, if available, the maximum value, if available,
 * or zero, in that order or priority.
 * @param <V> The value type this validator supports.
 * @author Garret Wilson
 */
public interface RangeValidator<V> extends Validator<V> {

	/** @return The minimum value, inclusive, or <code>null</code> if the range has no lower bound. */
	public V getMinimum();

	/** @return The maximum value, inclusive, or <code>null</code> if the range has no upper bound. */
	public V getMaximum();

	/** @return The step amount, or <code>null</code> if the range has no increment value specified. */
	public V getStep();

}
