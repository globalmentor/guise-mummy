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

import com.globalmentor.net.URIPath;
import com.guiseframework.event.NavigateActionListener;
import com.guiseframework.model.*;
import com.guiseframework.prototype.ActionPrototype;

/**Control with an action model rendered as a heading link.
@author Garret Wilson
*/
public class HeadingLink extends AbstractLinkControl implements HeadingComponent
{

	/**The zero-based level of the heading, or {@link HeadingComponent#NO_HEADING_LEVEL} if no level is specified.*/
	private int level;

		/**@return The zero-based level of the heading, or {@link HeadingComponent#NO_HEADING_LEVEL} if no level is specified.*/
		public int getLevel() {return level;}

		/**Sets the level of the heading.
		This is a bound property of type <code>Integer</code>.
		@param newLevel The new zero-based heading level, or {@link HeadingComponent#NO_HEADING_LEVEL} if no level is specified.
		@see HeadingComponent#LEVEL_PROPERTY
		*/
		public void setLevel(final int newLevel)
		{
			if(level!=newLevel)	//if the value is really changing
			{
				final int oldLevel=level;	//get the old value
				level=newLevel;	//actually change the value
				firePropertyChange(LEVEL_PROPERTY, oldLevel, newLevel);	//indicate that the value changed
			}			
		}

	/**Default constructor.*/
	public HeadingLink()
	{
		this(NO_HEADING_LEVEL);	//construct the class with no heading level
	}

	/**Label convenience constructor.
	@param label The label to use in the link, or <code>null</code> if there should be no label.
	*/
	public HeadingLink(final String label)
	{
		this(label, NO_HEADING_LEVEL);	//construct the class with no heading level
	}

	/**Info model constructor.
	@param infoModel The component info model.
	@throws NullPointerException if the given info model is <code>null</code>.
	*/
	public HeadingLink(final InfoModel infoModel)
	{
		this(infoModel, NO_HEADING_LEVEL);	//construct the class with no heading level
	}

	/**Info model, action model, and enableable object constructor.
	@param infoModel The component info model.
	@param actionModel The component action model.
	@param enableable The enableable object in which to store enabled status.
	@throws NullPointerException if the given info model, action model, and/or enableable object is <code>null</code>.
	*/
	public HeadingLink(final InfoModel infoModel, final ActionModel actionModel, final Enableable enableable)
	{
		this(infoModel, actionModel, enableable, NO_HEADING_LEVEL);	//construct the class with no heading level
	}

	/**Label and navigation path convenience constructor.
	A {@link NavigateActionListener} will be installed to navigate to the provided navigation path.
	@param label The label to use in the link, or <code>null</code> if there should be no label.
	@param navigationPath The destination path that will be used for navigation when the link is selected.
	@throws NullPointerException if the given navigation path is <code>null</code>.
	*/
	public HeadingLink(final String label, final URIPath navigationPath)
	{
		this(label, NO_HEADING_LEVEL, navigationPath);	//construct the class with no heading level
	}

	/**Label and navigation URI convenience constructor.
	A {@link NavigateActionListener} will be installed to navigate to the provided navigation URI.
	@param label The label to use in the link, or <code>null</code> if there should be no label.
	@param navigationURI The destination URI that will be used for navigation when the link is selected.
	@throws NullPointerException if the given navigation URI is <code>null</code>.
	*/
	public HeadingLink(final String label, final URI navigationURI)
	{
		this(label, NO_HEADING_LEVEL, navigationURI);	//construct the class with no heading level
	}

	/**Prototype constructor.
	@param actionPrototype The prototype on which this component should be based.
	@throws NullPointerException if the given prototype is <code>null</code>.
	*/
	public HeadingLink(final ActionPrototype actionPrototype)
	{
		this(actionPrototype, NO_HEADING_LEVEL);	//construct the class with no heading level
	}

	/**Heading level constructor.
	@param level The zero-based level of the heading, or {@link HeadingComponent#NO_HEADING_LEVEL} if no level is specified.
	*/
	public HeadingLink(final int level)
	{
		this((String)null, level);	//construct the class with no label
	}

	/**Label convenience constructor.
	@param label The label to use in the link, or <code>null</code> if there should be no label.
	@param level The zero-based level of the heading, or {@link HeadingComponent#NO_HEADING_LEVEL} if no level is specified.
	*/
	public HeadingLink(final String label, final int level)
	{
		this(new DefaultInfoModel(label), level);	//construct the class with a default info model
	}

	/**Info model constructor.
	@param labelModel The component info model.
	@param level The zero-based level of the heading, or {@link HeadingComponent#NO_HEADING_LEVEL} if no level is specified.
	@throws NullPointerException if the given info model is <code>null</code>.
	*/
	public HeadingLink(final InfoModel labelModel, final int level)
	{
		super(labelModel, new DefaultActionModel(), new DefaultEnableable());	//construct the parent class with the given info model and default other models
		this.level=level;	//save the level
	}

	/**Info model, action model, and enableable object constructor.
	@param labelModel The component info model.
	@param actionModel The component action model.
	@param enableable The enableable object in which to store enabled status.
	@param level The zero-based level of the heading, or {@link HeadingComponent#NO_HEADING_LEVEL} if no level is specified.
	@throws NullPointerException if the given info model, action model, and/or enableable object is <code>null</code>.
	*/
	public HeadingLink(final InfoModel labelModel, final ActionModel actionModel, final Enableable enableable, final int level)
	{
		super(labelModel, actionModel, enableable);	//construct the parent class
		this.level=level;	//save the level
	}

	/**Label and navigation path convenience constructor.
	A {@link NavigateActionListener} will be installed to navigate to the provided navigation path.
	@param label The label to use in the link, or <code>null</code> if there should be no label.
	@param level The zero-based level of the heading, or {@link HeadingComponent#NO_HEADING_LEVEL} if no level is specified.
	@param navigationPath The destination path that will be used for navigation when the link is selected.
	@throws NullPointerException if the given navigation path is <code>null</code>.
	*/
	public HeadingLink(final String label, final int level, final URIPath navigationPath)
	{
		this(label, level);	//construct the class with default models and the label
		addActionListener(new NavigateActionListener(navigationPath));	//add an action listener to navigate to the indicated location
	}

	/**Label and navigation URI convenience constructor.
	A {@link NavigateActionListener} will be installed to navigate to the provided navigation URI.
	@param label The label to use in the link, or <code>null</code> if there should be no label.
	@param level The zero-based level of the heading, or {@link HeadingComponent#NO_HEADING_LEVEL} if no level is specified.
	@param navigationURI The destination URI that will be used for navigation when the link is selected.
	@throws NullPointerException if the given navigation URI is <code>null</code>.
	*/
	public HeadingLink(final String label, final int level, final URI navigationURI)
	{
		this(label, level);	//construct the class with default models and the label
		addActionListener(new NavigateActionListener(navigationURI));	//add an action listener to navigate to the indicated location
	}

	/**Prototype constructor.
	@param actionPrototype The prototype on which this component should be based.
	@param level The zero-based level of the heading, or {@link HeadingComponent#NO_HEADING_LEVEL} if no level is specified.
	@throws NullPointerException if the given prototype is <code>null</code>.
	*/
	public HeadingLink(final ActionPrototype actionPrototype, final int level)
	{
		this(actionPrototype, actionPrototype, actionPrototype);	//use the action prototype as every needed model
	}

}
