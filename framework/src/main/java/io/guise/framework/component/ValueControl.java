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

package io.guise.framework.component;

import static com.globalmentor.java.Classes.*;

import io.guise.framework.model.ValueModel;

/**
 * A control to accept input by the user of a value.
 * @param <V> The type of value to represent.
 * @author Garret Wilson
 */
public interface ValueControl<V> extends Control, /*TODO del if not wanted EditComponent, */ValueModel<V>, ValuedComponent<V> {

	/** The converter bound property. */
	public static final String CONVERTER_PROPERTY = getPropertyName(ValueControl.class, "converter");

}
