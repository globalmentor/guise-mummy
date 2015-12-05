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

/**
 * Indicates a class that can provide a component to represent some value.
 * @param <V> The type of value for which components should be created.
 * @param <C> The type of component created for the value.
 * @author Garret Wilson
 */
public interface ComponentFactory<V, C extends Component> {

	/**
	 * Creates a component for the given value.
	 * @param value The value for which a component should be created.
	 * @return A new component to represent the given value, or <code>null</code> if the provided value is <code>null</code>.
	 */
	public C createComponent(final V value);

}
