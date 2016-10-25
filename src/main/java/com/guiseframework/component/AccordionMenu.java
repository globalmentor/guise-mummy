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

import com.guiseframework.component.layout.*;
import com.guiseframework.event.ActionEvent;
import com.guiseframework.event.ActionListener;
import com.guiseframework.model.*;
import com.guiseframework.prototype.MenuPrototype;

/**
 * A menu that collapses its children's children between its children, like an accordion. By default rollover open is disabled.
 * @author Garret Wilson
 * @see Menu#setRolloverOpenEnabled
 */
public class AccordionMenu extends AbstractMenu {

	/**
	 * Axis constructor.
	 * @param axis The axis along which the menu is oriented.
	 * @throws NullPointerException if the given axis is <code>null</code>.
	 */
	public AccordionMenu(final Flow axis) {
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
	public AccordionMenu(final InfoModel infoModel, final ActionModel actionModel, final Enableable enableable, final Flow axis) {
		super(infoModel, actionModel, enableable, new MenuLayout(axis)); //construct the parent class
		setRolloverOpenEnabled(false); //default to not showing the menu as open upon rollover
		addActionListener(new ActionListener() { //create an action listener to toggle the menu's open status

			@Override
			public void actionPerformed(final ActionEvent actionEvent) { //if the action is performed
				setOpen(!isOpen()); //toggle the menu's open status
			}

		});
	}

	/**
	 * Prototype and axis constructor.
	 * @param menuPrototype The prototype on which this component should be based.
	 * @param axis The axis along which the menu is oriented.
	 * @throws NullPointerException if the given prototype is <code>null</code>.
	 */
	public AccordionMenu(final MenuPrototype menuPrototype, final Flow axis) {
		this(menuPrototype, menuPrototype, menuPrototype, axis); //use the menu prototype as every needed model
	}

}
