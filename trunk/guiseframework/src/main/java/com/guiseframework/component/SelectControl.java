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

import static com.globalmentor.java.Classes.*;

/**
 * A control to allow selection by the user of a value from a collection.
 * @param <V> The type of values to select.
 * @author Garret Wilson
 */
public interface SelectControl<V> extends ValueControl<V> {

	/** The value representation strategy bound property. */
	public final static String VALUE_REPRESENTATION_STRATEGY_PROPERTY = getPropertyName(SelectControl.class, "valueRepresentationStrategy");

}
