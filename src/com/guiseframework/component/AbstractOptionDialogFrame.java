package com.guiseframework.component;

import java.util.Set;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.layout.*;
import com.guiseframework.model.ValueModel;

/**Abstract implementation of a frame for communication of an option.
An option frame defaults to a single composite child panel with a row of options along the bottom.
The contents of an option dialog frame should be accessed by {@link #getOptionContent()} and {@link #setOptionContent(Component)}.
This implementation does not allow its frame content to be changed.
@param <O> The type of options available.
@author Garret Wilson
*/
public abstract class AbstractOptionDialogFrame<O, C extends OptionDialogFrame<O, C>> extends AbstractDialogFrame<O, C> implements OptionDialogFrame<O, C>
{

	/**Sets the single child component.
	This implementation throws an exception because the frame content is not allowed to be changed.
	@param newContent The single child component, or <code>null</code> if this frame does not have a child component.
	@exception IllegalArgumentException if any different content is provided.
	*/
	public void setContent(final Component<?> newContent)
	{
		if(newContent!=getContent())	//if the content is changing
		{
			throw new IllegalArgumentException("Option dialog frame content cannot be changed.");
		}
	}

	/**@return The container component used to hold content, including the option child component.*/
	protected Container<?> getContentContainer() {return (Container<?>)super.getContent();}

	/**@return The component representing option contents, or <code>null</code> if this frame does not have an option contents component.*/ 
	public Component<?> getOptionContent()
	{
		return ((RegionLayout)getContentContainer().getLayout()).getComponent(Region.CENTER);	//return the center component, if there is one
	}

	/**Sets the component representing option contents.
	This implementation adds the option content component to the center region of the child container.
	@param newOptionContent The single option contents component, or <code>null</code> if this frame does not have an option contents component.
	*/
	public void setOptionContent(final Component<?> newOptionContent)
	{
		final Component<?> oldOptionContents=getOptionContent();	//get the current component
		if(oldOptionContents!=newOptionContent)	//if the value is really changing
		{
			final Container<?> contentsContainer=getContentContainer();	//get our container
			if(oldOptionContents!=null)	//if an old content component was present
			{
				contentsContainer.remove(oldOptionContents);	//remove the old component
			}
			if(newOptionContent!=null)	//if a new content component is given
			{
				contentsContainer.add(newOptionContent, new RegionConstraints(getSession(), Region.CENTER));	//add the component to the center of the container
			}
		}
	}

	/**The container containing the options.*/
	private final Container<?> optionContainer;

		/**@return The container containing the options.*/
		public Container<?> getOptionContainer() {return optionContainer;}

	/**The set of available options.*/
	private final Set<O> options;

		/**@return The set of available options.*/
		public Set<O> getOptions() {return options;}

	/**Session, ID, model, component, and options constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param component The component representing the content of the option dialog frame, or <code>null</code> if there is no content component.
	@param options The set of available options.
	@exception NullPointerException if the given session, model, and/or options is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractOptionDialogFrame(final GuiseSession session, final String id, final ValueModel<O> model, final Component<?> component, final Set<O> options)
	{
		super(session, id, model, new LayoutPanel(session, new RegionLayout(session)));	//construct the parent class using a layout panel as a container
		this.options=checkNull(options, "Options cannot be null.");	//save the options
		setOptionContent(component);	//set the component, if there is one
		optionContainer=createOptionContainer();	//create the option container
		getContentContainer().add(optionContainer, new RegionConstraints(session, Region.PAGE_END));	//add the option container at the bottom
		initializeOptionContainer(optionContainer, options);	//initialize the option container
	}

	/**Creates a container for holding the options.
	This implementation creates a horizontal layout panel.
	@return a container for holding the options.
	*/
	protected Container<?> createOptionContainer()
	{
		return new LayoutPanel(getSession(), createID("optionContainer"), new FlowLayout(getSession(), Flow.LINE));	//create a horizontal layout panel
	}

	/**Initializes the option container with the available options.
	@param optionContainer The container to the options.
	@param options The available options.
	*/
	protected abstract void initializeOptionContainer(final Container<?> optionContainer, final Set<O> options);
}
