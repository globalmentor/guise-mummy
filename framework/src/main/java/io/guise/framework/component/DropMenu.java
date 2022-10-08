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

import io.guise.framework.component.layout.*;
import io.guise.framework.model.*;
import io.guise.framework.prototype.MenuPrototype;

/**
 * A menu that drops its children down from the top or over to the side. By default rollover open is enabled.
 * @author Garret Wilson
 * @see Menu#setRolloverOpenEnabled
 */
public class DropMenu extends AbstractMenu {

	/**
	 * Axis constructor.
	 * @param axis The axis along which the menu is oriented.
	 * @throws NullPointerException if the given axis is <code>null</code>.
	 */
	public DropMenu(final Flow axis) {
		this(new DefaultInfoModel(), new DefaultActionModel(), new DefaultEnableable(), axis); //construct the class with default models
	}

	/**
	 * Info model, action model, enableable, and menu layout constructor.
	 * @param infoModel The component info model.
	 * @param actionModel The component action model.
	 * @param enableable The enableable object in which to store enabled status.
	 * @param axis The axis along which the menu is oriented.
	 * @throws NullPointerException if the given info model, action model, enableable, and/or layout is <code>null</code>.
	 */
	public DropMenu(final InfoModel infoModel, final ActionModel actionModel, final Enableable enableable, final Flow axis) {
		super(infoModel, actionModel, enableable, new MenuLayout(axis)); //construct the parent class
		setRolloverOpenEnabled(true); //default to showing the menu as open upon rollover
	}

	/**
	 * Prototype and axis constructor.
	 * @param menuPrototype The prototype on which this component should be based.
	 * @param axis The axis along which the menu is oriented.
	 * @throws NullPointerException if the given prototype and/or axis is <code>null</code>.
	 */
	public DropMenu(final MenuPrototype menuPrototype, final Flow axis) {
		this(menuPrototype, menuPrototype, menuPrototype, axis); //use the menu prototype as every needed model
	}

}
