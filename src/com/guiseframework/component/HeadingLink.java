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

	/**Label model constructor.
	@param labelModel The component label model.
	@exception NullPointerException if the given label model is <code>null</code>.
	*/
	public HeadingLink(final LabelModel labelModel)
	{
		this(labelModel, NO_HEADING_LEVEL);	//construct the class with no heading level
	}

	/**Label model, action model, and enableable object constructor.
	@param labelModel The component label model.
	@param actionModel The component action model.
	@param enableable The enableable object in which to store enabled status.
	@exception NullPointerException if the given label model, action model, and/or enableable object is <code>null</code>.
	*/
	public HeadingLink(final LabelModel labelModel, final ActionModel actionModel, final Enableable enableable)
	{
		this(labelModel, actionModel, enableable, NO_HEADING_LEVEL);	//construct the class with no heading level
	}

	/**Label and navigation path convenience constructor.
	A {@link NavigateActionListener} will be installed to navigate to the provided navigation path.
	@param label The label to use in the link, or <code>null</code> if there should be no label.
	@param navigationPath The destination path that will be used for navigation when the link is selected.
	@exception NullPointerException if the given navigation path is <code>null</code>.
	*/
	public HeadingLink(final String label, final URIPath navigationPath)
	{
		this(label, NO_HEADING_LEVEL, navigationPath);	//construct the class with no heading level
	}

	/**Label and navigation URI convenience constructor.
	A {@link NavigateActionListener} will be installed to navigate to the provided navigation URI.
	@param label The label to use in the link, or <code>null</code> if there should be no label.
	@param navigationURI The destination URI that will be used for navigation when the link is selected.
	@exception NullPointerException if the given navigation URI is <code>null</code>.
	*/
	public HeadingLink(final String label, final URI navigationURI)
	{
		this(label, NO_HEADING_LEVEL, navigationURI);	//construct the class with no heading level
	}

	/**Prototype constructor.
	@param actionPrototype The prototype on which this component should be based.
	@exception NullPointerException if the given prototype is <code>null</code>.
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
		this(new DefaultLabelModel(label), level);	//construct the class with a default label model
	}

	/**Label model constructor.
	@param labelModel The component label model.
	@param level The zero-based level of the heading, or {@link HeadingComponent#NO_HEADING_LEVEL} if no level is specified.
	@exception NullPointerException if the given label model is <code>null</code>.
	*/
	public HeadingLink(final LabelModel labelModel, final int level)
	{
		super(labelModel, new DefaultActionModel(), new DefaultEnableable());	//construct the parent class with the given label model and default other models
		this.level=level;	//save the level
	}

	/**Label model, action model, and enableable object constructor.
	@param labelModel The component label model.
	@param actionModel The component action model.
	@param enableable The enableable object in which to store enabled status.
	@param level The zero-based level of the heading, or {@link HeadingComponent#NO_HEADING_LEVEL} if no level is specified.
	@exception NullPointerException if the given label model, action model, and/or enableable object is <code>null</code>.
	*/
	public HeadingLink(final LabelModel labelModel, final ActionModel actionModel, final Enableable enableable, final int level)
	{
		super(labelModel, actionModel, enableable);	//construct the parent class
		this.level=level;	//save the level
	}

	/**Label and navigation path convenience constructor.
	A {@link NavigateActionListener} will be installed to navigate to the provided navigation path.
	@param label The label to use in the link, or <code>null</code> if there should be no label.
	@param level The zero-based level of the heading, or {@link HeadingComponent#NO_HEADING_LEVEL} if no level is specified.
	@param navigationPath The destination path that will be used for navigation when the link is selected.
	@exception NullPointerException if the given navigation path is <code>null</code>.
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
	@exception NullPointerException if the given navigation URI is <code>null</code>.
	*/
	public HeadingLink(final String label, final int level, final URI navigationURI)
	{
		this(label, level);	//construct the class with default models and the label
		addActionListener(new NavigateActionListener(navigationURI));	//add an action listener to navigate to the indicated location
	}

	/**Prototype constructor.
	@param actionPrototype The prototype on which this component should be based.
	@param level The zero-based level of the heading, or {@link HeadingComponent#NO_HEADING_LEVEL} if no level is specified.
	@exception NullPointerException if the given prototype is <code>null</code>.
	*/
	public HeadingLink(final ActionPrototype actionPrototype, final int level)
	{
		this(actionPrototype, actionPrototype, actionPrototype);	//use the action prototype as every needed model
	}

}
