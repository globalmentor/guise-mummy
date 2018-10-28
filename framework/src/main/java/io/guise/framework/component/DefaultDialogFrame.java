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

package io.guise.framework.component;

import io.guise.framework.model.DefaultValueModel;
import io.guise.framework.model.ValueModel;

/**
 * Default implementation of a frame meant for communication of a value.
 * @param <V> The value to be communicated.
 * @author Garret Wilson
 */
public class DefaultDialogFrame<V> extends AbstractDialogFrame<V> {

	/**
	 * Value class constructor.
	 * @param valueClass The class indicating the type of value held in the model.
	 * @throws NullPointerException if the given value class is <code>null</code>.
	 */
	public DefaultDialogFrame(final Class<V> valueClass) {
		this(new DefaultValueModel<V>(valueClass)); //use a default value model
	}

	/**
	 * Value class and component constructor.
	 * @param valueClass The class indicating the type of value held in the model.
	 * @param component The single child component, or <code>null</code> if this frame should have no child component.
	 * @throws NullPointerException if the given value class is <code>null</code>.
	 */
	public DefaultDialogFrame(final Class<V> valueClass, final Component component) {
		this(new DefaultValueModel<V>(valueClass), component); //use a default value model
	}

	/**
	 * Value model constructor.
	 * @param valueModel The component value model.
	 * @throws NullPointerException if the given value model is <code>null</code>.
	 */
	public DefaultDialogFrame(final ValueModel<V> valueModel) {
		this(valueModel, new LayoutPanel()); //default to a layout panel
	}

	/**
	 * Value model and component constructor.
	 * @param valueModel The component value model.
	 * @param component The single child component, or <code>null</code> if this frame should have no child component.
	 * @throws NullPointerException if the given value model is <code>null</code>.
	 */
	public DefaultDialogFrame(final ValueModel<V> valueModel, final Component component) {
		super(valueModel, component); //construct the parent class
	}

}
