package com.garretwilson.guise.component;

import java.io.IOException;

import com.garretwilson.beans.BoundPropertyObject;

import static com.garretwilson.lang.CharSequenceUtilities.*;
import static com.garretwilson.lang.ClassUtilities.*;
import com.garretwilson.event.EventListenerManager;
import com.garretwilson.guise.context.GuiseContext;
import com.garretwilson.guise.controller.Controller;

/**An abstract implementation of a component.
@author Garret Wilson
*/
public class AbstractComponent<C extends Component<C>> extends BoundPropertyObject implements Component<C>
{

	/**Extra characters allowed in the ID, verified for URI safeness.*/
	protected final static String ID_EXTRA_CHARACTERS="-_";

	/**The object managing event listeners.*/
	private final EventListenerManager eventListenerManager=new EventListenerManager();

		/**@return The object managing event listeners.*/
		protected EventListenerManager getEventListenerManager() {return eventListenerManager;}

	/**The controller installed in this component, or <code>null</code> if no controller is installed.*/
	private Controller<? extends GuiseContext, C> controller=null;

		/**@return The model used by this component.*/
		public Controller<? extends GuiseContext, C> getController() {return controller;}

		/**Sets the controller used by this component.
		This is a bound property.
		@param newController The new controller to use.
		@see Component#CONTROLLER_PROPERTY
		*/
		public void setController(final Controller<? extends GuiseContext, C> newController)
		{
			if(newController!=controller)	//if the value is really changing
			{
				final Controller<? extends GuiseContext, C> oldController=controller;	//get a reference to the old value
				controller=newController;	//actually change values
				firePropertyChange(CONTROLLER_PROPERTY, oldController, newController);	//indicate that the value changed				
			}
		}
	
	/**The component identifier*/
	private final String id;

		/**@return The component identifier.*/
		public String getID() {return id;}

		/**@return An identifier unique within this component's parent container, if any.*/
		public String getUniqueID()
		{
			final Container parent=getParent();	//get this component's parent
			return parent!=null ? parent.getUniqueID(this) : getID();	//if we have a parent, ask it for our unique ID; otherwise, our ID is already unique
		}

		/**@return An identifier unique up this component's hierarchy.*/
		public String getAbsoluteUniqueID()
		{
			final Container parent=getParent();	//get this component's parent
			return parent!=null ? parent.getAbsoluteUniqueID(this) : getUniqueID();	//if we have a parent container, ask it for our absolute ID; otherwise, return our local unique ID
		}

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

	/**The container parent of this component, or <code>null</code> if this component is not embedded in any container.*/
	private Container parent=null;

		/**@return The container parent of this component, or <code>null</code> if this component is not embedded in any container.*/
		public Container getParent() {return parent;}

		/**Retrieves the first ancestor of the given type.
		@param <C> The type of ancestor container requested.
		@param ancestorClass The class of ancestor container requested.
		@return The first ancestor container of the given type, or <code>null</code> if this component has no such ancestor.
		*/
		@SuppressWarnings("unchecked")	//we check to see if the ancestor is of the correct type before casting, so the cast is logically checked, though not syntactically checked
		public <A extends Container> A getAncestor(final Class<A> ancestorClass)
		{
			final Container parent=getParent();	//get this component's parent
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
		This method is managed by containers, and should usually never be called my other classes.
		In order to hinder inadvertent incorrect use, the parent must only be set after the component is added to the container, and only be unset after the component is removed from the container.
		If a component is given the same parent it already has, no action occurs.
		@param newParent The new parent for this component, or <code>null</code> if this component is being removed from a container.
		@exception IllegalStateException if a parent is provided and this component already has a parent.
		@exception IllegalStateException if no parent is provided and this component's old parent still recognizes this component as its child.
		@exception IllegalArgumentException if a parent is provided and the given parent does not already recognize this component as its child.
		*/
		public void setParent(final Container newParent)
		{
			final Container oldParent=parent;	//get the old parent
			if(oldParent!=newParent)	//if the parent is really changing
			{
				if(newParent!=null)	//if a parent is provided
				{
					if(oldParent!=null)	//if we already have a parent
					{
						throw new IllegalStateException("Component "+this+" already has parent: "+oldParent);
					}
					if(!newParent.contains(this))	//if the container is not really our parent
					{
						throw new IllegalArgumentException("Provided parent "+newParent+" is not really parent of component "+this);
					}
				}
				else	//if no parent is provided
				{
					if(oldParent!=null && oldParent.contains(this))	//if we had a parent before, and that parent still thinks this component is its child
					{
						throw new IllegalStateException("Old parent "+oldParent+" still thinks this component, "+this+", is a child."); 
					}
				}
				parent=newParent;	//this is really our parent; make a note of it
			}
		}

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

	/**Default constructor.*/
	public AbstractComponent()
	{
		this(null);	//construct the component, indicating that a default ID should be used
	}

	/**ID constructor.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractComponent(final String id)
	{
		if(id!=null)	//if an ID was provided
		{
			this.id=checkValidComponentID(id);	//save the ID, checking for compliance
		}
		else	//if an ID was not provided
		{
			this.id=getVariableName(getClass());	//create an ID by transforming the simple class name to a variable name
		}
	}

	/**Updates the view of this component.
	This method delegates to the isntalled controller, and if no controller is installed one is created.
	@param context Guise context information.
	@param component The component being rendered.
	@exception IOException if there is an error updating the view.
	@see #getController(GC, C)
	*/
	public <GC extends GuiseContext> void updateView(final GC context) throws IOException
	{
		final Controller<GC, C> controller=getController(context);	//get the controller
		controller.updateView(context, (C)this);	//tell the controller to update the view TODO testing; probably not correct, but works
	}

	/**Updates the model of this component.
	@param context Guise context information.
	@exception IOException if there is an error updating the model.
	*/
	public <GC extends GuiseContext> void updateModel(final GC context) throws IOException
	{
		final Controller<GC, C> controller=getController(context);	//get the controller
		controller.updateModel(context, (C)this);	//tell the controller to update the model
	}

	/**Determines the controller of this. If no controller is installed, one is created and installed.
	@param context Guise context information.
	@param component The component for which a controller should be retrieved.
	@exception NullPointerException if there is no controller installed and no appropriate controller registered with the Guise context.
	*/
	@SuppressWarnings("unchecked")
	protected <GC extends GuiseContext> Controller<GC, C> getController(final GC context)
	{
		Controller<GC, C> controller=(Controller<GC, C>)getController();	//get the installed controller TODO check
		if(controller==null)	//if no controller is installed
		{
			controller=context.getSession().getApplication().getController(this);	//ask the application for a controller
			if(controller!=null)	//if we found a controller
			{
				setController(controller);	//install the new controller
			}
			else	//if we don't have a render strategy
			{
				throw new NullPointerException("No registered controller for "+getClass().getName());	//TODO use a better error
			}
		}
		return controller;	//return the controller we found
	}

}
