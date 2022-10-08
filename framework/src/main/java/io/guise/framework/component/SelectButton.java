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

import io.guise.framework.model.*;

/**
 * Selectable button.
 * @author Garret Wilson
 */
public class SelectButton extends AbstractSelectActionControl implements SelectButtonControl {

	/** Default constructor. */
	public SelectButton() {
		this(new DefaultInfoModel(), new DefaultActionModel(), new DefaultEnableable()); //construct the class with default models
	}

	/**
	 * Info model, action model, and enableable object constructor.
	 * @param infoModel The component info model.
	 * @param actionModel The component action model.
	 * @param enableable The enableable object in which to store enabled status.
	 * @throws NullPointerException if the given info model, action model, and/or enableable object is <code>null</code>.
	 */
	public SelectButton(final InfoModel infoModel, final ActionModel actionModel, final Enableable enableable) {
		super(infoModel, actionModel, enableable); //construct the parent class
	}

	/**
	 * Prototype constructor.
	 * @param actionPrototype The prototype on which this component should be based.
	 */
	/*TODO fix
		public Button(final ActionPrototype actionPrototype)
		{
			this(actionPrototype, actionPrototype, actionPrototype);	//use the action prototype as every needed model
		}
	*/

}
