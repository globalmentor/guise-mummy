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

package com.guiseframework.input;

/**A strategy for processing input.
@author Garret Wilson
*/
public interface InputStrategy
{

	/**@return The parent input strategy, or <code>null</code> if there is no parent input strategy.*/
	public InputStrategy getParent();

	/**Processes input, returning whether the input was consumed.
	If the input is not consumed by this input strategy, it is sent to the parent input strategy, if any, for processing.
	@param input The input to process.
	@return <code>true</code> if the input was consumed and should not be processed further.
	@throws NullPointerException if the given input is <code>null</code>.
	*/
	public boolean input(final Input input);

}
