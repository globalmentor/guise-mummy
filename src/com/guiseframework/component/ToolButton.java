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

import java.net.URI;

import com.guiseframework.event.MouseAdapter;
import com.guiseframework.event.MouseEnterEvent;
import com.guiseframework.event.MouseExitEvent;
import com.guiseframework.model.*;
import com.guiseframework.prototype.ActionPrototype;

/**Control with an action model rendered as a tool button.
Tool buttons are typically presented on toolbars and rendered differently than a normal button;
they usually are more subtle and may only present button decorations upon certain gestures such as mouse overs.
@author Garret Wilson
*/
public class ToolButton extends AbstractButtonControl implements ToolButtonControl
{

	/**Default constructor.*/
	public ToolButton()
	{
		this(new DefaultLabelModel(), new DefaultActionModel(), new DefaultEnableable());	//construct the class with default models
	}

	/**Label constructor.
	@param label The text of the label, or <code>null</code> if there should be no label.
	*/
	public ToolButton(final String label)
	{
		this(label, null);	//construct the class with no icon
	}

	/**Label and icon constructor.
	@param label The text of the label, or <code>null</code> if there should be no label.
	@param icon The icon URI, which may be a resource URI, or <code>null</code> if there is no icon URI.
	*/
	public ToolButton(final String label, final URI icon)
	{
		this(new DefaultLabelModel(label, icon), new DefaultActionModel(), new DefaultEnableable());	//construct the class  with a default label model and the given label text
	}

	/**Label model, action model, and enableable object constructor.
	@param labelModel The component label model.
	@param actionModel The component action model.
	@param enableable The enableable object in which to store enabled status.
	@exception NullPointerException if the given label model, action model, and/or enableable object is <code>null</code>.
	*/
	public ToolButton(final LabelModel labelModel, final ActionModel actionModel, final Enableable enableable)
	{
		super(labelModel, actionModel, enableable);	//construct the parent class
	}

	/**Prototype constructor.
	@param actionPrototype The prototype on which this component should be based.
	@exception NullPointerException if the given prototype is <code>null</code>.
	*/
	public ToolButton(final ActionPrototype actionPrototype)
	{
		this(actionPrototype, actionPrototype, actionPrototype);	//use the action prototype as every needed model
		addMouseListener(new MouseAdapter()	//listen for the mouse over the control TODO eventually promote this (with modified logic for menus) to the tops of all action control hierarchy, as we already ignore rollover change unless needed
		{
			/**Called when the mouse enters the target.
			@param mouseEvent The event providing mouse information
			*/
			public void mouseEntered(final MouseEnterEvent mouseEvent)
			{
				setRollover(true);	//turn on the rollover state
			}

			/**Called when the mouse exits the target.
			@param mouseEvent The event providing mouse information
			*/
			public void mouseExited(final MouseExitEvent mouseEvent)
			{
				setRollover(false);	//turn off the rollover state
			}
		});
	}
	
}
