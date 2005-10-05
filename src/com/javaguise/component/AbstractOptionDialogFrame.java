package com.javaguise.component;

import java.util.Iterator;
import java.util.Set;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.garretwilson.util.EmptyIterator;
import com.garretwilson.util.ObjectIterator;
import com.javaguise.component.layout.*;
import com.javaguise.model.ValueModel;
import com.javaguise.session.GuiseSession;

/**Abstract implementation of a frame for communication of an option.
An option frame defaults to a single composite child panel with a row of options along the bottom.
A center content component within the child panel may be specified.
@param <O> The type of options available.
@author Garret Wilson
*/
public abstract class AbstractOptionDialogFrame<O, C extends OptionDialogFrame<O, C>> extends AbstractDialogFrame<O, C> implements OptionDialogFrame<O, C>
{

	/**@return The container component used to hold content, including the child component.*/
	public Container<?> getContainer() {return (Container<?>)super.getComponent();}

	/**The container containing the options.*/
	private final Container<?> optionContainer;

	/**@return The container containing the options.*/
	public Container<?> getOptionContainer() {return optionContainer;}

	/**@return The single child component, or <code>null</code> if this frame does not have a child component.
	This implementation returns the center component of the container.
	@see #getContainer()
	*/
	public Component<?> getComponent()
	{
		return ((RegionLayout)getContainer().getLayout()).getComponent(Region.CENTER);	//return the center component, if there is one
	}

	/**@return Whether this component has children. This implementation returns <code>true</code> because an option pane always has children.*/
	public boolean hasChildren() {return true;}	//TODO fix these methods

	/**Retrieves the child component with the given ID.
	@param id The ID of the component to return.
	@return The child component with the given ID, or <code>null</code> if there is no child component with the given ID. 
	*/
	public Component<?> getComponent(final String id)
	{
		final Container<?> container=getContainer();	//get the container, if there is one
		return id.equals(container.getID()) ? container : null;	//return the container if it has the correct ID
	}

	/**@return An iterator to the single child component, if there is one.*/
	public Iterator<Component<?>> iterator()
	{
		return new ObjectIterator<Component<?>>(getContainer());
	}

	
	
	
	
	
	
	
	
	

	/**Sets the single child component.
	This is a bound property.
	This implementation adds the content component to the center region of the child container.
	@param newComponent The single child component, or <code>null</code> if this frame does not have a child component.
	@see Frame#COMPONENT_PROPERTY
	@see #getContainer()
	*/
	public void setComponent(final Component<?> newComponent)
	{
		final Component<?> oldComponent=getComponent();	//get the current component
		if(oldComponent!=newComponent)	//if the value is really changing
		{
			final Container<?> container=getContainer();	//get our container
			if(newComponent!=null)	//if a content component is given
			{
				container.add(newComponent, RegionLayout.CENTER_CONSTRAINTS);	//add the component to the center of the container
			}
			else if(oldComponent!=null)	//no component was given but an old content component was present
			{
				container.remove(oldComponent);	//remove the old component
			}
			firePropertyChange(COMPONENT_PROPERTY, oldComponent, newComponent);	//indicate that the value changed
		}
	}

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
		setComponent(component);	//set the component, if there is one
		optionContainer=createOptionContainer();	//create the option container
		getContainer().add(optionContainer, RegionLayout.PAGE_END_CONSTRAINTS);	//add the option container at the bottom
		initializeOptionContainer(optionContainer, options);	//initialize the option container
	}

	/**Creates a container for holding the options.
	This implementation creates a horizontal layout panel.
	@return a container for holding the options.
	*/
	protected Container<?> createOptionContainer()
	{
		return new LayoutPanel(getSession(), createID("optionContainer"), new FlowLayout(getSession(), Orientation.Flow.LINE));	//create a horizontal layout panel
	}

	/**Initializes the option container with the available options.
	@param optionContainer The container to the options.
	@param options The available options.
	*/
	protected abstract void initializeOptionContainer(final Container<?> optionContainer, final Set<O> options);
}
