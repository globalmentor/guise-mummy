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

package com.guiseframework.platform;

import java.util.*;

/**Access to the platform user local environment.
Properties stored in the environment should be persisted across sessions, using an appropriate platform storage mechanism such as cookies.
@author Garret Wilson
*/
public interface Environment
{

	/**Determines if an environment property exists.
	@param name The name of the property to check.
	@return <code>true</code> if the environment has the given property.
	*/
	public boolean hasProperty(final String name);

	/**Retrieves an environment property by its name.
	A {@link ClassCastException} will eventually be thrown if the given value is not the generic type requested.
	@param <T> The type of property value expected.
	@param name The name of the property to retrieve.
	@return The property value, or <code>null</code> if there is no such property.
	*/
	public <T> T getProperty(final String name);

	/**Retrieves an environment property by its name, returning a default value if no value is available.
	A {@link ClassCastException} will eventually be thrown if the given value is not the generic type requested.
	@param <T> The type of property value expected.
	@param name The name of the property to retrieve.
	@param defaultValue The value to return if no such property is available, or <code>null</code> if there is no default value.
	@return The property value, or the provided default value if there is no such property.
	*/
	public <T> T getProperty(final String name, final T defaultValue);

	/**Retrieves a required environment property by its name, throwing an exception if the value is missing.
	A {@link ClassCastException} will eventually be thrown if the given value is not the generic type requested.
	@param <T> The type of property value expected.
	@param name The name of the property to retrieve.
	@return The property value.
	@exception IllegalStateException if no such property exists.
	*/
	public <T> T getRequiredProperty(final String name);

	/**Sets an environment property.
	@param name The name of the property.
	@param value The value to associate with the name.
	@exception IllegalArgumentException if the given property cannot be set to the given value or cannot be changed.
	*/
	public void setProperty(final String name, final Object value);

	/**Sets multiple environment properties.
	@param map The map of property names and values to set.
	@exception IllegalArgumentException if the given property cannot be set to the given value or cannot be changed.
	*/
	public void setProperties(final Map<String, Object> map);

	/**Removes the property specified by the given name.
	@param name The name of the property to remove.
	@exception IllegalArgumentException if the given property cannot be removed.
	*/
	public void removeProperty(final String name);

	/**Returns the available environment properties as a read-only map of property names and values.
	@return The available environment properties.
	*/
	public Map<String, Object> getProperties();
}
