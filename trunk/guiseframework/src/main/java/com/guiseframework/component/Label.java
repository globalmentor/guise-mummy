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
import com.guiseframework.prototype.LabelPrototype;

/**A label component.
This component installs a default export strategy supporting export of the following content types:
<ul>
	<li>The label content type.</li>
</ul>
@author Garret Wilson
*/
public class Label extends AbstractLabel
{

	/**Default constructor with a default info model.*/
	public Label()
	{
		this(new DefaultInfoModel());	//construct the class with a default info model
	}

	/**Label constructor.
	@param label The text of the label, or <code>null</code> if there should be no label.
	*/
	public Label(final String label)
	{
		this(label, null);	//construct the class with no icon
	}

	/**Label and icon constructor.
	@param label The text of the label, or <code>null</code> if there should be no label.
	@param icon The icon URI, which may be a resource URI, or <code>null</code> if there is no icon URI.
	*/
	public Label(final String label, final URI icon)
	{
		this(new DefaultInfoModel(label, icon));	//construct the class  with a default info model and the given label text
	}

	/**Info model constructor.
	@param infoModel The component info model.
	@throws NullPointerException if the given info model is <code>null</code>.
	*/
	public Label(final InfoModel infoModel)
	{
		super(infoModel);	//construct the parent class
	}

	/**Prototype constructor.
	@param labelPrototype The prototype on which this component should be based.
	@throws NullPointerException if the given prototype is <code>null</code>.
	*/
	public Label(final LabelPrototype labelPrototype)
	{
		this((InfoModel)labelPrototype);	//use the label prototype as the needed model
	}

}
