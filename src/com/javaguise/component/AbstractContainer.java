package com.javaguise.component;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import com.garretwilson.util.ReverseIterator;
import com.javaguise.component.layout.*;
import com.javaguise.session.GuiseSession;

import static com.garretwilson.lang.ObjectUtilities.*;

/**Abstract implementation of a container component.
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

	/**@return An iterator to contained components in reverse order.*/
	public Iterator<Component<?>> reverseIterator() {return new ReverseIterator<Component<?>>(componentList.listIterator(componentList.size()));}

	/**@return Whether this component has children. This implementation delegates to the component list.*/
	public boolean hasChildren() {return !componentList.isEmpty();}

	/**Determines whether this container contains the given component.
	@param component The component to check.
	@return <code>true</code> if this container contains the given component.
	*/
	public boolean contains(final Component<?> component) {return componentList.contains(component);}

	/**Adds a component to the container with default constraints.
	@param component The component to add.
	@exception IllegalArgumentException if the component already has a parent.
	@exception IllegalStateException the installed layout does not support default constraints.
	*/
	public void add(final Component<?> component)
	{
		add(component, null);	//add the component, indicating default constraints should be used
	}

	/**Adds a component to the container along with constraints.
	@param component The component to add.
	@param constraints The constraints for the layout, or <code>null</code> if default constraints should be used.
	@exception IllegalArgumentException if the component already has a parent.
	@exception ClassCastException if the provided constraints are not appropriate for the installed layout.
	@exception IllegalStateException if no constraints were provided and the installed layout does not support default constraints.
	*/
	public void add(final Component<?> component, final Layout.Constraints constraints)
	{
		add(component, getLayout(), constraints);	//TODO comment
	}

	/**Adds a component to the container.
	@param component The component to add.
	@param layout The currently installed layout.
	@param constraints The constraints for the layout, or <code>null</code> if default constraints should be used.
	@exception IllegalArgumentException if the component already has a parent.
	@exception ClassCastException if the provided constraints are not appropriate for the installed layout.
	@exception IllegalStateException if no constraints were provided and the installed layout does not support default constraints.
	*/
	protected <T extends Layout.Constraints> void add(final Component<?> component, final Layout<T> layout, final Object constraints)	//TODO check ClassCastException---it will not be thrown here
	{
		if(component.getParent()!=null)	//if this component has already been added to container
		{
			throw new IllegalArgumentException("Component "+component+" is already a member of a container, "+component.getParent()+".");
		}
		final T layoutConstraints=constraints!=null ? (T)constraints : layout.createDefaultConstraints(component);	//create default constraints if we need to TODO use the layout constraints class to cast the value
		componentList.add(component);	//add the component to the list
		component.setParent(this);	//tell the component who its parent is
		layout.setConstraints(component, layoutConstraints);	//tell the layout the constraints
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
		getLayout().removeConstraints(component);	//remove the constraints for this component
		componentList.remove(component);	//remove the component to the list
		component.setParent(null);	//tell the component it no longer has a parent
	}

	/**The layout definition for the container.*/
	private final Layout<?> layout;

		/**@return The layout definition for the container.*/
		public Layout<?> getLayout() {return layout;}

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
		this(session, id, new FlowLayout(Orientation.Flow.PAGE));	//default to flowing vertically
	}

	/**Session and layout constructor.
	@param session The Guise session that owns this component.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given session and/or layout is <code>null</code>.
	*/
	public AbstractContainer(final GuiseSession<?> session, final Layout<?> layout)
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
	public AbstractContainer(final GuiseSession<?> session, final String id, final Layout<?> layout)
	{
		super(session, id);	//construct the parent class
		this.layout=checkNull(layout, "Layout cannot be null.");	//save the layout
	}

}