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

import com.guiseframework.model.*;
import com.guiseframework.prototype.ActionPrototype;

/**Control with an action model rendered as a button.
@author Garret Wilson
*/
public class Button extends AbstractButtonControl
{

	/**Default constructor.*/
	public Button()
	{
		this(new DefaultInfoModel(), new DefaultActionModel(), new DefaultEnableable());	//construct the class with default models
	}

	/**Label constructor.
	@param label The text of the label, or <code>null</code> if there should be no label.
	*/
	public Button(final String label)
	{
		this(label, null);	//construct the class with no icon
	}

	/**Label and icon constructor.
	@param label The text of the label, or <code>null</code> if there should be no label.
	@param icon The icon URI, which may be a resource URI, or <code>null</code> if there is no icon URI.
	*/
	public Button(final String label, final URI icon)
	{
		this(new DefaultInfoModel(label, icon), new DefaultActionModel(), new DefaultEnableable());	//construct the class  with a default info model and the given label text
	}

	/**Info model, action model, and enableable object constructor.
	@param infoModel The component info model.
	@param actionModel The component action model.
	@param enableable The enableable object in which to store enabled status.
	@exception NullPointerException if the given info model, action model, and/or enableable object is <code>null</code>.
	*/
	public Button(final InfoModel infoModel, final ActionModel actionModel, final Enableable enableable)
	{
		super(infoModel, actionModel, enableable);	//construct the parent class
	}

	/**Prototype constructor.
	@param actionPrototype The prototype on which this component should be based.
	@exception NullPointerException if the given prototype is <code>null</code>.
	*/
	public Button(final ActionPrototype actionPrototype)
	{
		this(actionPrototype, actionPrototype, actionPrototype);	//use the action prototype as every needed model
	}
	
}
