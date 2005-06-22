package com.garretwilson.guise.component;

import static com.garretwilson.lang.ObjectUtilities.*;

import java.io.IOException;

import com.garretwilson.event.EventListenerManager;
import com.garretwilson.guise.context.GuiseContext;
import com.garretwilson.guise.controller.Controller;
import com.garretwilson.util.BoundPropertyObject;

/**An abstract implementation of a component.
@author Garret Wilson
*/
public class AbstractComponent extends BoundPropertyObject implements Component
{

	/**The controller installed in this component, or <code>null</code> if no controller is installed.*/
	private Controller<? extends GuiseContext, ? extends Component> controller=null;

		/**@return The model used by this component.*/
		@SuppressWarnings("unchecked")	//we'll assume a correct controller was installed, and wait until later for class cast problems to arise
		public <GC extends GuiseContext, C extends Component> Controller<GC, C> getController() {return (Controller<GC, C>)controller;}

		/**Sets the controller used by this component.
		This is a bound property.
		@param newController The new controller to use.
		@see Component#CONTROLLER_PROPERTY
		*/
		public <GC extends GuiseContext, C extends Component> void setController(final Controller<GC, C> newController)
		{
			if(newController!=controller)	//if the value is really changing
			{
				final Controller<? extends GuiseContext, ? extends Component> oldController=controller;	//get a reference to the old value
				controller=newController;	//actually change values
				firePropertyChange(CONTROLLER_PROPERTY, oldController, newController);	//indicate that the value changed				
			}
		}
	
	/**The object managing event listeners.*/
	private final EventListenerManager eventListenerManager=new EventListenerManager();

		/**@return The object managing event listeners.*/
		protected EventListenerManager getEventListenerManager() {return eventListenerManager;}

	/**The component identifier*/
	private final String id;

		/**@return The component identifier.*/
		public String getID() {return id;}

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

	/**ID constructor.
	@param id The component identifier.
	@exception NullPointerException if the given identifier is <code>null</code>.
	*/
	public AbstractComponent(final String id)
	{
		this.id=checkNull(id, "Component identifier cannot be null.");	//save the ID
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
		final Controller<GC, AbstractComponent> controller=getController(context);	//get the controller
		controller.updateView(context, this);	//tell the controller to update the view TODO testing; probably not correct, but works
	}

	/**Updates the model of this component.
	@param context Guise context information.
	@exception IOException if there is an error updating the model.
	*/
	public <GC extends GuiseContext> void updateModel(final GC context) throws IOException
	{
		final Controller<GC, AbstractComponent> controller=getController(context);	//get the controller
		controller.updateModel(context, this);	//tell the controller to update the model
	}

	/**Determines the controller of this. If no controller is installed, one is created and installed.
	@param context Guise context information.
	@param component The component for which a controller should be retrieved.
	@exception NullPointerException if there is no controller installed and no appropriate controller registered with the Guise context.
	*/
	protected <GC extends GuiseContext> Controller<GC, AbstractComponent> getController(final GC context)
	{
		Controller<GC, AbstractComponent> controller=getController();	//get the installed controller
		if(controller==null)	//if no controller is installed
		{
			controller=(Controller<GC, AbstractComponent>)context.getRenderStrategy(this);	//ask the context for a controller TODO check; overall generics needs to be improved
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
