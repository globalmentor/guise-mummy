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

package com.guiseframework.event;

/**An object that listens for selection of a value.
@param <V> The type of value being selected.
@author Garret Wilson
*/
public interface ValueSelectListener<V> extends GuiseEventListener
{

	/**Called when a value is selected.
	@param valueEvent The event indicating the value selected.
	*/
	public void valueSelected(final ValueEvent<V> valueEvent);

}
