package com.garretwilson.guise.component;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import com.garretwilson.guise.component.layout.*;
import com.garretwilson.guise.session.GuiseSession;

import static com.garretwilson.lang.ObjectUtilities.*;

/**Abstract implementation of a container component.
This implementation uses a lazily-created list of child components, making empty containers lightweight.
Iterating over child components is thread safe.
@author Garret Wilson
*/
public abstract class AbstractContainer<C extends Container<C>> extends AbstractComponent<C> implements Container<C>
{

	/**The list of child components.*/ 
	private final List<Component<?>> componentList=new CopyOnWriteArrayList<Component<?>>();	//create a new component list, using a thread-safe array that takes into consideration that adding or removing children usually takes place up-front, and most later access will be only reads

		/**@return The list of child components.*/ 
		protected List<Component<?>> getComponentList() {return componentList;}

	/**@return An iterator to contained components.*/
	public Iterator<Component<?>> iterator() {return componentList.iterator();}

	/**Adds a component to the container.
	@param component The component to add.
	@exception IllegalArgumentException if the component already has a parent.
	*/
	public void add(final Component<?> component)
	{
		if(component.getParent()!=null)	//if this component has already been added to container
		{
			throw new IllegalArgumentException("Component "+component+" is already a member of a container, "+component.getParent()+".");
		}
		componentList.add(component);	//add the component to the list
		component.setParent(this);	//tell the component who its parent is
	}

	/**Removes a component from the container.
	@param component The component to remove.
	@exception IllegalArgumentException if the component is not a member of the container.
	*/
	public void remove(final Component<?> component)
	{
		if(component.getParent()!=this)	//if this component is not a member of this container
		{
			throw new IllegalArgumentException("Component "+component+" is not member of a container "+this+".");
		}
		componentList.remove(component);	//remove the component to the list
		component.setParent(null);	//tell the component it no longer has a parent
	}

	/**Determines whether this container contains the given component.
	@param component The component to check.
	@return <code>true</code> if this container contains the given component.
	*/
	public boolean contains(final Component<?> component) {return componentList.contains(component);}

	/**@return Whether this component has children. This implementation delegates to the component list.*/
	public boolean hasChildren() {return !componentList.isEmpty();}

	/**@return The child components of this component. This implementation returns this instance.*/
	public Iterable<Component<?>> getChildren() {return this;}

	/**The layout definition for the container.*/
	private final Layout layout;

		/**@return The layout definition for the container.*/
		public Layout getLayout() {return layout;}

	/**Session constructor with a default vertical flow layout.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractContainer(final GuiseSession<?> session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default vertical flow layout.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractContainer(final GuiseSession<?> session, final String id)
	{
		this(session, id, new FlowLayout(Axis.Y));	//default to flowing vertically
	}

	/**Session and layout constructor.
	@param session The Guise session that owns this component.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given session and/or layout is <code>null</code>.
	*/
	public AbstractContainer(final GuiseSession<?> session, final Layout layout)
	{
		this(session, null, layout);	//construct the component with the layout, indicating that a default ID should be used
	}

	/**Session, ID, and layout constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given session and/or layout is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractContainer(final GuiseSession<?> session, final String id, final Layout layout)
	{
		super(session, id);	//construct the parent class
		this.layout=checkNull(layout, "Layout cannot be null.");	//save the layout
	}

}
