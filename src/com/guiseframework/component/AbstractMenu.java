package com.guiseframework.component;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.layout.*;
import com.guiseframework.event.MouseEvent;
import com.guiseframework.event.MouseListener;
import com.guiseframework.geometry.Point;
import com.guiseframework.geometry.Rectangle;
import com.guiseframework.model.MenuModel;

/**An abstract menu component.
This implementation initially closes any child menu added to this menu.
@author Garret Wilson
*/
public abstract class AbstractMenu<C extends Menu<C>> extends AbstractContainer<C> implements Menu<C>  
{

	/**@return The data model used by this component.*/
	public MenuModel getModel() {return (MenuModel)super.getModel();}

	/**@return The layout definition for the menu.*/
	public MenuLayout getLayout() {return (MenuLayout)super.getLayout();}	//a menu can only have a menu layout

	/**Whether the state of the control represents valid user input.*/
	private boolean valid=true;

		/**@return Whether the state of the control represents valid user input.*/
		public boolean isValid() {return valid;}

		/**Sets whether the state of the control represents valid user input
		This is a bound property of type <code>Boolean</code>.
		@param newValid <code>true</code> if user input should be considered valid
		@see Control#VALID_PROPERTY
		*/
		public void setValid(final boolean newValid)
		{
			if(valid!=newValid)	//if the value is really changing
			{
				final boolean oldValid=valid;	//get the current value
				valid=newValid;	//update the value
				firePropertyChange(VALID_PROPERTY, Boolean.valueOf(oldValid), Boolean.valueOf(newValid));
			}
		}

	/**Whether the component is in a rollover state.*/
	private boolean rollover=false;

		/**@return Whether the component is in a rollover state.*/
		public boolean isRollover() {return rollover;}

		/**Sets whether the component is in a rollover state.
		This is a bound property of type <code>Boolean</code>.
		@param newRollover <code>true</code> if the component should be in a rollover state, else <code>false</code>.
		@see Menu#ROLLOVER_PROPERTY
		*/
		public void setRollover(final boolean newRollover)
		{
			if(rollover!=newRollover)	//if the value is really changing
			{
				final boolean oldRollover=rollover;	//get the current value
				rollover=newRollover;	//update the value
				firePropertyChange(ROLLOVER_PROPERTY, Boolean.valueOf(oldRollover), Boolean.valueOf(newRollover));
			}
		}

	/**Whether the menu children will be shown during rollover.*/
	private boolean rolloverOpenEnabled=false;

		/**@return Whether the menu children will be shown during rollover.*/
		public boolean isRolloverOpenEnabled() {return rolloverOpenEnabled;}

		/**Sets whether the menu children will be shown during rollover.
		If rollover open is enabled, the open state will not actually be changed during rollover.
		This is a bound property of type <code>Boolean</code>.
		@param newRolloverOpenEnabled <code>true</code> if the component should allow display during rollover, else <code>false</code>.
		@see Menu#ROLLOVER_OPEN_ENABLED_PROPERTY
		*/
		public void setRolloverOpenEnabled(final boolean newRolloverOpenEnabled)
		{
			if(rolloverOpenEnabled!=newRolloverOpenEnabled)	//if the value is really changing
			{
				final boolean oldRolloverOpenEnabled=rolloverOpenEnabled;	//get the current value
				rolloverOpenEnabled=newRolloverOpenEnabled;	//update the value
				firePropertyChange(ROLLOVER_OPEN_ENABLED_PROPERTY, Boolean.valueOf(oldRolloverOpenEnabled), Boolean.valueOf(newRolloverOpenEnabled));
			}
		}

	/**Session, ID, layout, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param layout The layout definition for the container.
	@param model The component data model.
	@exception NullPointerException if the given session, layout, and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractMenu(final GuiseSession session, final String id, final MenuLayout layout, final MenuModel model)
	{
		super(session, id, layout, model);	//construct the parent class
	}

	/**Fires a mouse entered event to all registered mouse listeners.
	This implementation first sets the rollover state to <code>true</code>.
	@param componentBounds The absolute bounds of the component.
	@param viewportBounds The absolute bounds of the viewport.
	@param mousePosition The position of the mouse relative to the viewport.
	@exception NullPointerException if one or more of the arguments are <code>null</code>.
	@see MouseListener
	@see MouseEvent
	@see #setRollover(boolean)
	*/
	public void fireMouseEntered(final Rectangle componentBounds, final Rectangle viewportBounds, final Point mousePosition)
	{
		setRollover(true);	//turn on the rollover state
		super.fireMouseEntered(componentBounds, viewportBounds, mousePosition);	//fire the event normally
	}

	/**Fires a mouse exited event to all registered mouse listeners.
	This implementation first sets the rollover state to <code>false</code>.
	@param componentBounds The absolute bounds of the component.
	@param viewportBounds The absolute bounds of the viewport.
	@param mousePosition The position of the mouse relative to the viewport.
	@exception NullPointerException if one or more of the arguments are <code>null</code>.
	@see MouseListener
	@see MouseEvent
	@see #setRollover(boolean)
	*/
	public void fireMouseExited(final Rectangle componentBounds, final Rectangle viewportBounds, final Point mousePosition)
	{
		setRollover(false);	//turn off the rollover state
		super.fireMouseExited(componentBounds, viewportBounds, mousePosition);	//fire the event normally
	}

	/**Adds a child component.
	If this component is itself a menu, this version closes that menu. 
	@param component The component to add to this component.
	*/
	protected void addComponent(final Component<?> component)
	{
		super.addComponent(component);	//do the default adding
		if(component instanceof Menu)	//if the component is a menu
		{
			((Menu<?>)component).getModel().setOpen(false);	//close the child menu
		}
	}

}
