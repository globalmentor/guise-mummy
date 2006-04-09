package com.guiseframework.component;

import static com.garretwilson.lang.ObjectUtilities.*;

import java.util.Iterator;

import com.guiseframework.component.layout.*;
import com.guiseframework.event.ActionEvent;
import com.guiseframework.event.ActionListener;
import com.guiseframework.event.MouseEvent;
import com.guiseframework.event.MouseListener;
import com.guiseframework.event.PostponedActionEvent;
import com.guiseframework.geometry.Point;
import com.guiseframework.geometry.Rectangle;
import com.guiseframework.model.ActionModel;

/**An abstract menu component.
This implementation initially closes any child menu added to this menu.
@author Garret Wilson
*/
public abstract class AbstractMenu<C extends Menu<C>> extends AbstractContainerControl<C> implements Menu<C>  
{

	/**@return The layout definition for the menu.*/
	public MenuLayout getLayout() {return (MenuLayout)super.getLayout();}	//a menu can only have a menu layout

	/**The action model used by this component.*/
	private final ActionModel actionModel;

		/**@return The action model used by this component.*/
		protected ActionModel getActionModel() {return actionModel;}

	/**Whether the menu is open.*/
	private boolean open=true;

		/**@return Whether the menu is open.*/
		public boolean isOpen() {return open;}

		/**Sets whether the menu is open.
		This is a bound property of type <code>Boolean</code>.
		@param newOpen <code>true</code> if the menu should be open.
		@see #OPEN_PROPERTY
		*/
		public void setOpen(final boolean newOpen)
		{
			if(open!=newOpen)	//if the value is really changing
			{
				final boolean oldOpen=open;	//get the old value
				open=newOpen;	//actually change the value
				firePropertyChange(OPEN_PROPERTY, Boolean.valueOf(oldOpen), Boolean.valueOf(newOpen));	//indicate that the value changed
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

	/**Menu layout and action model constructor.
	@param layout The layout definition for the container.
	@param actionModel The component action model.
	@exception NullPointerException if the given layout and/or action model is <code>null</code>.
	*/
	public AbstractMenu(final MenuLayout layout, final ActionModel actionModel)
	{
		super(layout);	//construct the parent class
		this.actionModel=checkInstance(actionModel, "Action model cannot be null.");	//save the action model
		this.actionModel.addActionListener(new ActionListener()	//create an action repeater to forward events to this component's listeners TODO create a common method to create a forwarding listener, if we can
				{
					public void actionPerformed(final ActionEvent actionEvent)	//if the action is performed
					{
						fireAction();	//fire an action with this component as the source
					}
				});
	}

	/**Adds an action listener.
	@param actionListener The action listener to add.
	*/
	public void addActionListener(final ActionListener actionListener)
	{
		getEventListenerManager().add(ActionListener.class, actionListener);	//add the listener
	}

	/**Removes an action listener.
	@param actionListener The action listener to remove.
	*/
	public void removeActionListener(final ActionListener actionListener)
	{
		getEventListenerManager().remove(ActionListener.class, actionListener);	//remove the listener
	}

	/**@return all registered action listeners.*/
	@SuppressWarnings("unchecked")
	public Iterator<ActionListener> getActionListeners()
	{
		return (Iterator<ActionListener>)(Object)getEventListenerManager().getListeners(ActionListener.class);	//remove the listener TODO find out why we have to use the double cast for JDK 1.5 to compile
	}

	/**Fires an action to all registered action listeners.
	@see ActionListener
	@see ActionEvent
	*/
	public void fireAction()
	{
		if(getEventListenerManager().hasListeners(ActionListener.class))	//if there are action listeners registered
		{
			final ActionEvent actionEvent=new ActionEvent(this);	//create a new action event
			getSession().queueEvent(new PostponedActionEvent(getEventListenerManager(), actionEvent));	//tell the Guise session to queue the event
		}
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
			((Menu<?>)component).setOpen(false);	//close the child menu
		}
	}

}
