package com.garretwilson.guise.component;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import com.garretwilson.guise.component.layout.*;
import com.garretwilson.guise.session.GuiseSession;

import static com.garretwilson.lang.ObjectUtilities.*;
import com.garretwilson.util.EmptyIterator;

/**Abstract implementation of a container component.
This implementation uses a lazily-created list of child components, making empty containers lightweight.
Iterating over child components is thread safe.
@author Garret Wilson
*/
public class AbstractContainer extends AbstractComponent<Container> implements Container
{

	/**The character used when building absolute IDs.*/
	protected final static char ABSOLUTE_ID_SEGMENT_DELIMITER=':';

	/**The list of child components.*/ 
	private final List<Component<?>> componentList=new CopyOnWriteArrayList<Component<?>>();	//create a new component list, using a thread-safe array that takes into consideration that adding or removing children usually takes place up-front, and most later access will be only reads

		/**@return The list of child components.*/ 
		protected List<Component<?>> getComponentList() {return componentList;}

	/**@return An iterator to contained components.*/
	public Iterator<Component<?>> iterator() {return componentList.iterator();}

	/**Adds a component to the container.
	@param component The component to add.
	@exception IllegalArgumentException if the component is already a member of a container.
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

	/**Determines whether this container contains the given component.
	@param component The component to check.
	@return <code>true</code> if this container contains the given component.
	*/
	public boolean contains(final Component<?> component) {return componentList.contains(component);}

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

	/**@return The character used by this container when building absolute IDs.*/
	public char getAbsoluteIDSegmentDelimiter()
	{
		return ABSOLUTE_ID_SEGMENT_DELIMITER;	//return our absolute segment connector character		
	}

	/**Determines the unique ID of the provided child component within this container.
	If the child component's ID is already unique, that ID will be used.
	This method is typically called by child components when determining their own unique IDs.
	@param childComponent A component within this container.
	@return An identifier of the given component unique within this container.
	@exception IllegalArgumentException if the given component is not a child of this container.
	*/
	public String getUniqueID(final Component<?> childComponent)
	{
		final String childID=childComponent.getID();	//get the child component's preferred ID
		boolean idClashes=false;	//we'll start out assuming that the child's preferred ID doesn't class with any of the other child IDs
		int childIndex=-1;	//we'll ensure that the child is actually one of our children by setting this variable to a value greater than or equal to zero
		int i=-1;	//we'll find the index of this component within this container; currently we haven't looked at any child components
		for(final Component<?> component:this)	//for each component in the container
		{
			++i;	//show that we're looking at another child component
			if(component==childComponent)	//if this child is the provided child component
			{
				assert childIndex<0 : "Unexpectedly found component listed as a child more than once in this container.";
				childIndex=i;	//store the child index of this component
			}
			else if(!idClashes)	//if this is another child and we haven't had an ID clash, yet
			{
				if(childID.equals(component.getID()))	//if the child component's preferred ID clashes with this component's preferred ID
				{
					idClashes=true;	//indicate that there is an ID clash
				}
			}
			if(childIndex>=0 && idClashes)	//if we've located the child component in the container, and we've already found an ID clash, there's no point in looking any further
			{
				break;	//stop looking; there's no new information we can find
			}
		}
		if(childIndex>=0)	//if we found the child component in the container
		{
			return idClashes ? childID+childIndex : childID;	//if there was an ID clash, append the child component's index within this container; otherwise, just use the child component's preferred ID
		}
		throw new IllegalArgumentException("Component "+childComponent+" is not a child of container "+this);
	}

	/**Determines the absolute unique ID of the provided child component up the component's hierarchy.
	This method is typically called by child components when determining their own absolute unique IDs.
	@param childComponent A component within this container.
	@return An absolute identifier of the given component unique up the component's hierarchy.
	@exception IllegalArgumentException if the given component is not a child of this container.
	*/
	public String getAbsoluteUniqueID(final Component<?> childComponent)
	{
		return getAbsoluteUniqueID(getUniqueID(childComponent));	//return the absolute form of the unique ID of the child component
	}

	/**Determines the absolute unique ID up the component's hierarchy for the given local unique ID.
	This method is useful for generating radio button group identifiers, for example.
	@param uniqueID An identifier unique within this container.
	@return An absolute form of the given identifier unique up the component's hierarchy.
	*/
	protected String getAbsoluteUniqueID(final String uniqueID)
	{
		return getAbsoluteUniqueID()+getAbsoluteIDSegmentDelimiter()+uniqueID;	//concatenate our own absolute unique ID and the local unique ID of the child, separated by the correct delimiter character		
	}
}
