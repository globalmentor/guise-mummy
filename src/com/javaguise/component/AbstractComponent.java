package com.javaguise.component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import com.garretwilson.beans.BoundPropertyObject;
import com.garretwilson.event.EventListenerManager;
import com.garretwilson.lang.ObjectUtilities;
import com.javaguise.component.layout.Orientation;
import com.javaguise.context.GuiseContext;
import com.javaguise.controller.Controller;
import com.javaguise.model.Model;
import com.javaguise.session.GuiseSession;
import com.javaguise.validator.ValidationsException;
import com.garretwilson.util.EmptyIterator;

import static com.garretwilson.lang.CharSequenceUtilities.*;
import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;

/**An abstract implementation of a component.
@author Garret Wilson
*/
public class AbstractComponent<C extends Component<C>> extends BoundPropertyObject implements Component<C>
{

	/**The character used when building absolute IDs.*/
//TODO del when works	protected final static char ABSOLUTE_ID_SEGMENT_DELIMITER=':';

	/**Extra characters allowed in the ID, verified for URI safeness.*/
	protected final static String ID_EXTRA_CHARACTERS="-_";

	/**The object managing event listeners.*/
	private final EventListenerManager eventListenerManager=new EventListenerManager();

		/**@return The object managing event listeners.*/
		protected EventListenerManager getEventListenerManager() {return eventListenerManager;}

	/**@return A reference to this instance, cast to the generic self type.*/
	@SuppressWarnings("unchecked")
	protected final C getThis() {return (C)this;}
		
	/**@return Whether this component has children. This implementation returns <code>false</code>.*/
	public boolean hasChildren() {return false;}

	/**@return An iterator to child components. This implementation returns an empty iterator.*/
	public Iterator<Component<?>> iterator() {return new EmptyIterator<Component<?>>();}

	/**@return An iterator to contained components in reverse order.*/
	public Iterator<Component<?>> reverseIterator() {return new EmptyIterator<Component<?>>();}

	/**The controller installed in this component, or <code>null</code> if no controller is installed.*/
	private Controller<? extends GuiseContext<?>, ? super C> controller=null;

		/**@return The model used by this component.*/
		public Controller<? extends GuiseContext<?>, ? super C> getController() {return controller;}

		/**Sets the controller used by this component.
		This is a bound property.
		@param newController The new controller to use.
		@see Component#CONTROLLER_PROPERTY
		*/
		public void setController(final Controller<? extends GuiseContext<?>, ? super C> newController)
		{
			if(newController!=controller)	//if the value is really changing
			{
				final Controller<? extends GuiseContext<?>, ? super C> oldController=controller;	//get a reference to the old value
				controller=newController;	//actually change values
				firePropertyChange(CONTROLLER_PROPERTY, oldController, newController);	//indicate that the value changed				
			}
		}

	/**The thread-safe list of errors.*/
	private final List<Throwable> errorList=new CopyOnWriteArrayList<Throwable>();

		/**@return An iterable interface to all errors associated with this component.*/
		public Iterable<Throwable> getErrors() {return errorList;}

		/**@return <code>true</code> if there is at least one error associated with this component.*/
		public boolean hasErrors() {return !errorList.isEmpty();}

		/**Adds an error to the component.
		@param error The error to add.
		*/
		public void addError(final Throwable error) {errorList.add(error);}

		/**Adds errors to the component.
		@param errors The errors to add.
		*/
		public void addErrors(final Collection<? extends Throwable> errors) {errorList.addAll(errors);}

		/**Removes a specific error from this component.
		@param error The error to remove.
		*/
		public void removeError(final Throwable error) {errorList.remove(error);}

		/**Clears all errors associated with this component.*/
		public void clearErrors() {errorList.clear();}

	/**The component identifier*/
	private final String id;

		/**@return The component identifier.*/
		public String getID() {return id;}

	/**The internationalization orientation of the component's contents, or <code>null</code> if the default orientation should be used.*/
	private Orientation orientation=null;

		/**Returns this component's requested orientation.
		To resolve the orientation up the hierarchy, {@link #getComponentOrientation()} should be used.
		@return The internationalization orientation of the component's contents, or <code>null</code> if the default orientation should be used.
		@see #getComponentOrientation()
		*/
		public Orientation getOrientation() {return orientation=null;}

		/**Determines the internationalization orientation of the component's contents.
		This method returns the local orientation value, if there is one.
		If there is no orientation specified for this component, the request is deferred to this component's parent.
		If there is no parent component, a default orientation is retrieved from the current session.
		@return The internationalization orientation of the component's contents.
		@see #getOrientation()
		@see GuiseSession#getOrientation()
		*/
		public Orientation getComponentOrientation()
		{
			final Orientation orientation=getOrientation();	//get this component's orientation
			if(orientation!=null)	//if an orientation is explicitly set for this component
			{
				return orientation;	//return this component's orientation
			}
			else	//otherwise, try to defer to the parent
			{
				final Component<?> parent=getParent();	//get this component's parent
				if(parent!=null)	//if we have a parent
				{
					return parent.getComponentOrientation();	//return the parent's orientation
				}
				else	//if we don't have a parent
				{
					return getSession().getOrientation();	//return the session's default orientation
				}
			}
		}

		/**Sets the orientation.
		This is a bound property
		@param newOrientation The new internationalization orientation of the component's contents, or <code>null</code> if default orientation should be determined based upon the session's locale.
		@see Component#ORIENTATION_PROPERTY
		*/
		public void setOrientation(final Orientation newOrientation)
		{
			if(!ObjectUtilities.equals(orientation, newOrientation))	//if the value is really changing
			{
				final Orientation oldOrientation=orientation;	//get the old value
				orientation=newOrientation;	//actually change the value
				firePropertyChange(ORIENTATION_PROPERTY, oldOrientation, newOrientation);	//indicate that the value changed
			}
		}

		/**@return An identifier unique within this component's parent, if any.*/
/*TODO del when works
		public String getUniqueID()
		{
			final Component<?> parent=getParent();	//get this component's parent
			return parent!=null ? parent.getUniqueID(this) : getID();	//if we have a parent, ask it for our unique ID; otherwise, our ID is already unique
		}
*/

		/**@return An identifier unique up this component's hierarchy.*/
/*TODO del when works
		public String getAbsoluteUniqueID()
		{
			final Component<?> parent=getParent();	//get this component's parent
			return parent!=null ? parent.getAbsoluteUniqueID(this) : getUniqueID();	//if we have a parent component, ask it for our absolute ID; otherwise, return our local unique ID
		}
*/

		/**Determines the unique ID of the provided child component within this component.
		If the child component's ID is already unique, that ID will be used.
		This method is typically called by child components when determining their own unique IDs.
		@param childComponent A component within this component.
		@return An identifier of the given component unique within this component.
		@exception IllegalArgumentException if the given component is not a child of this component.
		*/
/*TODO del when works
		public String getUniqueID(final Component<?> childComponent)
		{
			final String childID=childComponent.getID();	//get the child component's preferred ID
			boolean idClashes=false;	//we'll start out assuming that the child's preferred ID doesn't class with any of the other child IDs
			int childIndex=-1;	//we'll ensure that the child is actually one of our children by setting this variable to a value greater than or equal to zero
			int i=-1;	//we'll find the index of this component within this component; currently we haven't looked at any child components
			for(final Component<?> component:this)	//for each component in the component
			{
				++i;	//show that we're looking at another child component
				if(component==childComponent)	//if this child is the provided child component
				{
					assert childIndex<0 : "Unexpectedly found component listed as a child more than once in this component.";
					childIndex=i;	//store the child index of this component
				}
				else if(!idClashes)	//if this is another child and we haven't had an ID clash, yet
				{
					if(childID.equals(component.getID()))	//if the child component's preferred ID clashes with this component's preferred ID
					{
						idClashes=true;	//indicate that there is an ID clash
					}
				}
				if(childIndex>=0 && idClashes)	//if we've located the child component in the component, and we've already found an ID clash, there's no point in looking any further
				{
					break;	//stop looking; there's no new information we can find
				}
			}
			if(childIndex>=0)	//if we found the child component in the component
			{
				return idClashes ? childID+childIndex : childID;	//if there was an ID clash, append the child component's index within this component; otherwise, just use the child component's preferred ID
			}
			throw new IllegalArgumentException("Component "+childComponent+" is not a child of component "+this);
		}
*/

		/**Determines the absolute unique ID of the provided child component up the component's hierarchy.
		This method is typically called by child components when determining their own absolute unique IDs.
		@param childComponent A component within this component.
		@return An absolute identifier of the given component unique up the component's hierarchy.
		@exception IllegalArgumentException if the given component is not a child of this component.
		*/
/*TODO del when works
		public String getAbsoluteUniqueID(final Component<?> childComponent)
		{
			return getAbsoluteUniqueID(getUniqueID(childComponent));	//return the absolute form of the unique ID of the child component
		}
*/

		/**Determines the absolute unique ID up the component's hierarchy for the given local unique ID.
		This method is useful for generating radio button group identifiers, for example.
		@param uniqueID An identifier unique within this component.
		@return An absolute form of the given identifier unique up the component's hierarchy.
		*/
/*TODO del when works
		protected String getAbsoluteUniqueID(final String uniqueID)
		{
			return getAbsoluteUniqueID()+getAbsoluteIDSegmentDelimiter()+uniqueID;	//concatenate our own absolute unique ID and the local unique ID of the child, separated by the correct delimiter character		
		}
*/

		/**Determines if the given string is a valid component ID.
		A valid component ID begins with a letter and is composed only of letters, digits, and/or the characters '-' and '_'.
		@param string The string to check for component identifier compliance.
		@return <code>true</code> if the string is a valid component ID, else <code>false</code>.
		@exception NullPointerException if the given string is <code>null</code>.
		*/ 		
		public static boolean isValidComponentID(final String string)
		{
			return string.length()>0 && Character.isLetter(string.charAt(0)) && isLettersDigitsCharacters(string, ID_EXTRA_CHARACTERS);	//make sure the string has characters; that the first character is a letter; and that the remaining characters are letters, digits, and/or the extra ID characters
		}

		/**Checks to ensure that the given string is a valid component identifier, throwing an exception if not.
		@param string The string to check for component identifier compliance.
		@return The component identifier after being checked for compliance.
		@exception IllegalArgumentException if the given string is not a valid component ID.
		@exception NullPointerException if the given string is <code>null</code>.
		@see #isValidComponentID(String)
		*/
		public static String checkValidComponentID(final String string)
		{
			if(!isValidComponentID(string))	//if the string is not a valid component ID
			{
				throw new IllegalArgumentException("Invalid component ID: \""+string+"\".");
			}
			return string;	//return the string; it passed the test
		}

		/**Creates a default identifier for this component.
		This implementation creates an identifier by transforming the simple class name to a variable name.
		@return A default identifier for this component.
		*/
/*TODO del if not needed
		protected String getDefaultID()
		{
			return getVariableName(getClass());	//create an ID by transforming the simple class name to a variable name
		}
*/

	/**The parent of this component, or <code>null</code> if this component does not have a parent.*/
	private Component<?> parent=null;

		/**@return The parent of this component, or <code>null</code> if this component does not have a parent.*/
		public Component<?> getParent() {return parent;}

		/**Retrieves the first ancestor of the given type.
		@param <C> The type of ancestor component requested.
		@param ancestorClass The class of ancestor component requested.
		@return The first ancestor component of the given type, or <code>null</code> if this component has no such ancestor.
		*/
		@SuppressWarnings("unchecked")	//we check to see if the ancestor is of the correct type before casting, so the cast is logically checked, though not syntactically checked
		public <A extends Component<?>> A getAncestor(final Class<A> ancestorClass)
		{
			final Component<?> parent=getParent();	//get this component's parent
			if(parent!=null)	//if there is a parent
			{
				return ancestorClass.isInstance(parent) ? (A)parent : parent.getAncestor(ancestorClass);	//if the parent is of the correct type, return it; otherwise, ask it to search its own ancestors
			}
			else	//if there is no parent
			{
				return null;	//there is no such ancestor
			}		
		}

		/**Sets the parent of this component.
		This method is managed by containers, and normally should not be called by applications.
		A component cannot be given a parent if it already has a parent.
		A component's parent cannot be removed if that parent is a container and this component is still a child of that container.
		A container's parent cannot be set to a container unless that container already recognizes this component as one of its children.
		If a component is given the same parent it already has, no action occurs.
		@param newParent The new parent for this component, or <code>null</code> if this component is being removed from a parent.
		@exception IllegalStateException if a parent is provided and this component already has a parent.
		@exception IllegalStateException if no parent is provided and this component's old parent is a container that still recognizes this component as its child.
		@exception IllegalArgumentException if a parent container is provided and the given parent container does not already recognize this component as its child.
		@see Container#add(Component)
		@see Container#remove(Component)
		*/
		public void setParent(final Component<?> newParent)
		{
			final Component<?> oldParent=parent;	//get the old parent
			if(oldParent!=newParent)	//if the parent is really changing
			{
				if(newParent!=null)	//if a parent is provided
				{
					if(oldParent!=null)	//if we already have a parent
					{
						throw new IllegalStateException("Component "+this+" already has parent: "+oldParent);
					}
					if(newParent instanceof Container && !((Container<?>)newParent).contains(this))	//if the new parent is a container that is not really our parent
					{
						throw new IllegalArgumentException("Provided parent container "+newParent+" is not really parent of component "+this);
					}
				}
				else	//if no parent is provided
				{
					if(oldParent instanceof Container && ((Container<?>)oldParent).contains(this))	//if we had a container parent before, and that container still thinks this component is its child
					{
						throw new IllegalStateException("Old parent container "+oldParent+" still thinks this component, "+this+", is a child."); 
					}
				}
				parent=newParent;	//this is really our parent; make a note of it
			}
		}

	/**The Guise session that owns this component.*/
	private final GuiseSession<?> session;

		/**@return The Guise session that owns this component.*/
		public GuiseSession<?> getSession() {return session;}

	/**The style identifier, or <code>null</code> if there is no style ID.*/
	private String styleID=null;

		/**@return The style identifier, or <code>null</code> if there is no style ID.*/
		public String getStyleID() {return styleID;}

		/**Identifies the style for the component.
		This is a bound property.
		@param newStyleID The style identifier, or <code>null</code> if there is no style ID.
		@see Component#STYLE_ID_PROPERTY
		*/
		public void setStyleID(final String newStyleID)
		{
			if(styleID!=newStyleID)	//if the value is really changing
			{
				final String oldStyleID=styleID;	//get the current value
				styleID=newStyleID;	//update the value
				firePropertyChange(STYLE_ID_PROPERTY, oldStyleID, newStyleID);
			}
		}

	/**Whether the component is visible.*/
	private boolean visible=true;

		/**@return Whether the component is visible.*/
		public boolean isVisible() {return visible;}

		/**Sets whether the component is visible.
		This is a bound property of type <code>Boolean</code>.
		@param newVisible <code>true</code> if the component should be visible, else <code>false</code>.
		@see Component#VISIBLE_PROPERTY
		*/
		public void setVisible(final boolean newVisible)
		{
			if(visible!=newVisible)	//if the value is really changing
			{
				final boolean oldVisible=visible;	//get the current value
				visible=newVisible;	//update the value
				firePropertyChange(VISIBLE_PROPERTY, Boolean.valueOf(oldVisible), Boolean.valueOf(newVisible));
			}
		}

	/**Whether the component has dragging enabled.*/
	private boolean dragEnabled=false;

		/**@return Whether the component has dragging enabled.*/
		public boolean isDragEnabled() {return dragEnabled;}

		/**Sets whether the component is has dragging enabled.
		This is a bound property of type <code>Boolean</code>.
		@param newDragEnabled <code>true</code> if the component should allow dragging, else false, else <code>false</code>.
		@see Component#DRAG_ENABLED_PROPERTY
		*/
		public void setDragEnabled(final boolean newDragEnabled)
		{
			if(dragEnabled!=newDragEnabled)	//if the value is really changing
			{
				final boolean oldDragEnabled=dragEnabled;	//get the current value
				dragEnabled=newDragEnabled;	//update the value
				firePropertyChange(DRAG_ENABLED_PROPERTY, Boolean.valueOf(oldDragEnabled), Boolean.valueOf(newDragEnabled));
			}
		}

	/**Session constructor.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractComponent(final GuiseSession<?> session)
	{
		this(session, null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractComponent(final GuiseSession<?> session, final String id)
	{
		this.session=checkNull(session, "Session cannot be null");	//save the session
		if(id!=null)	//if an ID was provided
		{
			this.id=checkValidComponentID(id);	//save the ID, checking for compliance
		}
		else	//if an ID was not provided
		{
			this.id=getVariableName(getClass());	//create an ID by transforming the simple class name to a variable name
		}
	}

	/**Determines whether the models of this component and all of its child components are valid.
	This version returns <code>true</code> if all its child components are valid.
	@return Whether the models of this component and all of its child components are valid.
	*/
	public boolean isValid()
	{
		if(!getController().isValid())	//if the controller isn't valid
		{
			return false;	//although the model may be valid, its view representation is not
		}
		for(final Component<?> childComponent:this)	//for each child component
		{
			if(!childComponent.isValid())	//if this child component isn't valid
			{
				return false;	//indicate that this component is consequently not valid
			}
		}
		return true;	//indicate that all child components are valid
	}

	/**@return The character used by this component when building absolute IDs.*/
/*TODO del when works
	public char getAbsoluteIDSegmentDelimiter()
	{
		return ABSOLUTE_ID_SEGMENT_DELIMITER;	//return our absolute segment connector character		
	}
*/

	/**Collects the current data from the view of this component.
	This method should not normally be called directly by applications.
	This method delegates to the installed controller, and if no controller is installed one is created and installed.
	@param context Guise context information.
	@exception IOException if there is an error querying the view.
	@see GuiseContext.State#QUERY_VIEW
	@see #getController(GC, C)
	*/
	public <GC extends GuiseContext<?>> void queryView(final GC context) throws IOException
	{
		final Controller<? super GC, ? super C> controller=getController(context);	//get the controller
		controller.queryView(context, getThis());	//tell the controller to query the view
	}

	/**Decodes the data of the view of this component.
	This method should not normally be called directly by applications.
	This method delegates to the installed controller, and if no controller is installed one is created and installed.
	@param context Guise context information.
	@exception IOException if there is an error decoding the view.
	@exception ValidationsException if the view information is in an invalid format and cannot be decoded.
	@see #getController(GC, C)
	@see GuiseContext.State#DECODE_VIEW
	*/
	public <GC extends GuiseContext<?>> void decodeView(final GC context) throws IOException, ValidationsException
	{
		final Controller<? super GC, ? super C> controller=getController(context);	//get the controller
		controller.decodeView(context, getThis());	//tell the controller to decode the view
	}

	/**Validates the view of this component.
	This method should not normally be called directly by applications.
	This method delegates to the installed controller, and if no controller is installed one is created and installed.
	@param context Guise context information.
	@exception IOException if there is an error validating the view.
	@exception ValidationsException if the view information is not valid to store in the model.
	@see #getController(GC, C)
	@see GuiseContext.State#VALIDATE_VIEW
	*/
	public <GC extends GuiseContext<?>> void validateView(final GC context) throws IOException, ValidationsException
	{
		final Controller<? super GC, ? super C> controller=getController(context);	//get the controller
		controller.validateView(context, getThis());	//tell the controller to update the view
	}

	/**Updates the model of this component.
	This method should not normally be called directly by applications.
	This method delegates to the installed controller, and if no controller is installed one is created and installed.
	@param context Guise context information.
	@exception IOException if there is an error updating the model.
	@exception ValidationsException if the view information is not valid to store in the model.
	@see #getController(GC, C)
	@see GuiseContext.State#UPDATE_MODEL
	*/
	public <GC extends GuiseContext<?>> void updateModel(final GC context) throws IOException, ValidationsException
	{
		final Controller<? super GC, ? super C> controller=getController(context);	//get the controller
		controller.updateModel(context, getThis());	//tell the controller to update the model
	}

	/**Collects the current data from the model of this component.
	This method should not normally be called directly by applications.
	This method delegates to the installed controller, and if no controller is installed one is created and installed.
	@param context Guise context information.
	@exception IOException if there is an error querying the model.
	@see #getController(GC, C)
	@see GuiseContext.State#QUERY_MODEL
	*/
	public <GC extends GuiseContext<?>> void queryModel(final GC context) throws IOException
	{
		final Controller<? super GC, ? super C> controller=getController(context);	//get the controller
		controller.queryModel(context, getThis());	//tell the controller to query the model
	}

	/**Encodes the data of the model of this component.
	This method should not normally be called directly by applications.
	This method delegates to the installed controller, and if no controller is installed one is created and installed.
	@param context Guise context information.
	@exception IOException if there is an error encoding the model.
	@see #getController(GC, C)
	@see GuiseContext.State#ENCODE_MODEL
	*/
	public <GC extends GuiseContext<?>> void encodeModel(final GC context) throws IOException
	{
		final Controller<? super GC, ? super C> controller=getController(context);	//get the controller
		controller.encodeModel(context, getThis());	//tell the controller to encode the model
	}

	/**Updates the view of this component.
	This method should not normally be called directly by applications.
	This method delegates to the installed controller, and if no controller is installed one is created and installed.
	@param context Guise context information.
	@exception IOException if there is an error updating the view.
	@see #getController(GC, C)
	@see GuiseContext.State#UPDATE_VIEW
	*/
	public <GC extends GuiseContext<?>> void updateView(final GC context) throws IOException
	{
		final Controller<? super GC, ? super C> controller=getController(context);	//get the controller
		controller.updateView(context, getThis());	//tell the controller to update the view
	}

	/**Determines the controller of this. If no controller is installed, one is created and installed.
	@param context Guise context information.
	@exception NullPointerException if there is no controller installed and no appropriate controller registered with the Guise context.
	*/
	@SuppressWarnings("unchecked")	//because of erasure, we must assume that any controller instantiated from a class object is of the correct generic type
	protected <GC extends GuiseContext<?>> Controller<? super GC, ? super C> getController(final GC context)
	{
		Controller<? super GC, ? super C> controller=(Controller<? super GC, ? super C>)getController();	//get the installed controller
		if(controller==null)	//if no controller is installed
		{
			controller=context.getSession().getApplication().getController(context, getThis());	//ask the application for a controller
			if(controller!=null)	//if we found a controller
			{
				setController((Controller<? extends GuiseContext<?>, ? super C>)controller);	//install the new controller
			}
			else	//if we don't have a controller
			{
				throw new NullPointerException("No registered controller for "+getClass().getName());	//TODO use a better error
			}
		}
		return controller;	//return the controller we found
	}

}
