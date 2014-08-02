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

import com.guiseframework.model.*;
import com.guiseframework.prototype.ValuePrototype;

/**Link that stores a Boolean value in its model representing the selected state.
A validator requiring a non-<code>null</code> value is automatically installed.
@author Garret Wilson
*/
public class BooleanSelectLink extends AbstractBooleanSelectActionControl implements SelectLinkControl
{

	/**Default constructor.*/
	public BooleanSelectLink()
	{
		this(new DefaultInfoModel(), new DefaultActionModel(), new DefaultValueModel<Boolean>(Boolean.class, Boolean.FALSE), new DefaultEnableable());	//construct the class with default models
	}

	/**Info model, action model, value model, and enableable object constructor.
	@param infoModel The component info model.
	@param actionModel The component action model.
	@param valueModel The component value model.
	@param enableable The enableable object in which to store enabled status.
	@throws NullPointerException if the given info model, action model, and/or enableable object is <code>null</code>.
	*/
	public BooleanSelectLink(final InfoModel infoModel, final ActionModel actionModel, final ValueModel<Boolean> valueModel, final Enableable enableable)
	{
		super(infoModel, actionModel, valueModel, enableable);	//construct the parent class		
	}

	/**Prototype constructor.
	@param valuePrototype The prototype on which this component should be based.
	*/
	public BooleanSelectLink(final ValuePrototype<Boolean> valuePrototype)
	{
		this(valuePrototype, new DefaultActionModel(), valuePrototype, valuePrototype);	//use the value prototype as every needed model except for the action model
	}
}
