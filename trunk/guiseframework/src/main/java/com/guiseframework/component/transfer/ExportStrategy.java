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

package com.guiseframework.component.transfer;

import com.guiseframework.component.Component;

/**A strategy for exporting data from a component.
@param <C> The type of component supported by this export strategy.
@author Garret Wilson
*/
public interface ExportStrategy<C extends Component>
{

	/**Exports data from the given component.
	@param component The component from which data will be transferred.
	@return The object to be transferred, or <code>null</code> if no data can be transferred.
	*/
	public Transferable<C> exportTransfer(final C component);
}
