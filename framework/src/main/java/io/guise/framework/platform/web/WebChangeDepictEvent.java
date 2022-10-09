/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.platform.web;

import java.util.*;

import static java.util.Collections.*;
import static java.util.Objects.*;

import static com.globalmentor.collections.Maps.*;

import com.globalmentor.javascript.JSON;
import com.globalmentor.model.NameValuePair;

import io.guise.framework.platform.DepictedObject;

/**
 * Indicates that one or more properties of a depicted object have changed on the web platform. Each property value can be an object a {@link List}, or a
 * {@link Map} of other properties.
 * @author Garret Wilson
 */
public class WebChangeDepictEvent extends AbstractWebDepictEvent {

	private static final long serialVersionUID = -6187566638917814160L;

	/** The read-only map of properties. */
	private final Map<String, Object> properties;

	/** @return The read-only map of properties. */
	public Map<String, Object> getProperties() {
		return properties;
	}

	/**
	 * Depicted object and properties constructor.
	 * @param depictedObject The depicted object on which the event initially occurred.
	 * @param properties The relevant properties; properties with duplicate names replace earlier properties of the same name.
	 * @throws NullPointerException if the given depicted object and/or properties is <code>null</code>.
	 */
	@SuppressWarnings("unchecked")
	public WebChangeDepictEvent(final DepictedObject depictedObject, final NameValuePair<String, Object>... properties) {
		super(depictedObject); //construct the parent class
		this.properties = unmodifiableMap(addAll(new HashMap<String, Object>(properties.length), properties)); //add all the properties to a new map
	}

	/**
	 * Depicted object and properties map constructor.
	 * @param depictedObject The depicted object on which the event initially occurred.
	 * @param properties The map representing the relevant properties.
	 * @throws NullPointerException if the given depicted object and/or properties is <code>null</code>.
	 */
	public WebChangeDepictEvent(final DepictedObject depictedObject, final Map<String, Object> properties) {
		super(depictedObject); //construct the parent class
		this.properties = unmodifiableMap(new HashMap<String, Object>(requireNonNull(properties, "Properties cannot be null.")));
	}

	@Override
	public String toString() {
		return super.toString() + JSON.serialize(getProperties());
	}
}
