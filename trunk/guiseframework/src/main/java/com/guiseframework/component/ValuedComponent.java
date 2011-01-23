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

package com.guiseframework.component;

import java.beans.PropertyVetoException;

import com.guiseframework.model.Valued;

/**A component that can be initialized with a value and allows a value to be retrieved.
This component is useful for editing complex values (such as contact information).
@param <V> The type of value displayed within the component.
@author Garret Wilson
*/
public interface ValuedComponent<V> extends Component, Valued<V>
{

	/**@return The class representing the type of value displayed in the component.*/
	public Class<V> getValueClass();

	/**@return The current value displayed in the component, or <code>null</code> if there is no value.*/
	public V getValue();

	/**Sets the new value to be displayed in the component.
	@param newValue The new value.
	@exception PropertyVetoException if the provided value is not valid or the change has otherwise been vetoed.
	*/
	public void setValue(final V newValue) throws PropertyVetoException;

}
