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

package io.guise.framework.platform;

import static java.util.Collections.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An abstract implementation of the platform user local environment.
 * @author Garret Wilson
 */
public class AbstractEnvironment implements Environment {

	/** The map of environment properties. */
	private final Map<String, Object> propertyMap = new ConcurrentHashMap<String, Object>();

	@Override
	public boolean hasProperty(final String name) {
		return propertyMap.containsKey(name); //see if the map contains the given property key
	}

	@SuppressWarnings("unchecked")
	@Override
	//cast needed so that Sun JDK 1.6.0_03-b05 will know which type we want; not required for Eclipse 3.4M3
	public <T> T getProperty(final String name) {
		return (T)getProperty(name, null); //return the value with no default value
	}

	@SuppressWarnings("unchecked")
	@Override
	//we cast the property value to the expected caller type as a convenience
	public <T> T getProperty(final String name, final T defaultValue) {
		final T value = (T)propertyMap.get(name); //get the property value, if there is one
		return value != null ? value : defaultValue; //return the default value (which may also be null) if we couldn't find a value
	}

	@SuppressWarnings("unchecked")
	@Override
	//cast needed so that Sun JDK 1.6.0_03-b05 will know which type we want; not required for Eclipse 3.4M3
	public <T> T getRequiredProperty(final String name) {
		final T value = (T)getProperty(name); //get the property value, if there is one
		if(value == null) { //if there is no value
			throw new IllegalStateException("Missing environment property: " + name);
		}
		return value; //return the value
	}

	@Override
	public void setProperty(final String name, final Object value) {
		propertyMap.put(name, value); //set the property value
	}

	@Override
	public void setProperties(final Map<String, Object> map) {
		propertyMap.putAll(map); //put all the properties in the map
	}

	@Override
	public void removeProperty(final String name) {
		propertyMap.remove(name); //remove this property
	}

	@Override
	public Map<String, Object> getProperties() {
		return unmodifiableMap(propertyMap); //return the map of property names and values
	}
}
