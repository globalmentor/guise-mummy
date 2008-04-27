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

package com.guiseframework.prototype;

import java.util.Set;

import com.globalmentor.beans.PropertyBindable;

import static com.globalmentor.java.Classes.*;

/**An object that provides prototypes.
When the available prototypes change, a {@link #PROTOTYPE_PROVISIONS_PROPERTY} property change event is fired.
Typically provided prototypes are merged with other prototypes into menus and/or toolbars.
@author Garret Wilson
*/
public interface PrototypeProvider extends PropertyBindable
{

	/**The prototype provisions property.*/
	public final static String PROTOTYPE_PROVISIONS_PROPERTY=getPropertyName(PrototypeProvider.class, "prototypeProvisions");

	/**Returns the prototypes provisions currently provided by this provider.
	This is a read-only bound property.
	@return The prototypes provisions currently provided by this provider.
	@see #PROTOTYPE_PROVISIONS_PROPERTY
	*/
	public Set<PrototypeProvision<?>> getPrototypeProvisions();

}
