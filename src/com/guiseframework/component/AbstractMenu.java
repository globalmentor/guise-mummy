package com.guiseframework.component;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.guiseframework.component.layout.*;
import com.guiseframework.event.*;
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

	/**Whether the icon is displayed.*/
	private boolean iconDisplayed=true;

		/**@return Whether the icon is displayed.*/
		public boolean isIconDisplayed() {return iconDisplayed;}

		/**Sets whether the icon is displayed.
		This is a bound property of type <code>Boolean</code>.
		@param newIconDisplayed <code>true</code> if the icon should be displayed, else <code>false</code> if the icon should not be displayed and take up no space.
		@see #ICON_DISPLAYED_PROPERTY
		*/
		public void setIconDisplayed(final boolean newIconDisplayed)
		{
			if(iconDisplayed!=newIconDisplayed)	//if the value is really changing
			{
				final boolean oldIconDisplayed=iconDisplayed;	//get the current value
				iconDisplayed=newIconDisplayed;	//update the value
				firePropertyChange(ICON_DISPLAYED_PROPERTY, Boolean.valueOf(oldIconDisplayed), Boolean.valueOf(newIconDisplayed));
			}
		}

	/**Whether the label is displayed.*/
	private boolean labelDisplayed=true;

		/**@return Whether the label is displayed.*/
		public boolean isLabelDisplayed() {return labelDisplayed;}

		/**Sets whether the label is displayed.
		This is a bound property of type <code>Boolean</code>.
		@param newLabelDisplayed <code>true</code> if the label should be displayed, else <code>false</code> if the label should not be displayed and take up no space.
		@see #LABEL_DISPLAYED_PROPERTY
		*/
		public void setLabelDisplayed(final boolean newLabelDisplayed)
		{
			if(labelDisplayed!=newLabelDisplayed)	//if the value is really changing
			{
				final boolean oldLabelDisplayed=labelDisplayed;	//get the current value
				labelDisplayed=newLabelDisplayed;	//update the value
				firePropertyChange(LABEL_DISPLAYED_PROPERTY, Boolean.valueOf(oldLabelDisplayed), Boolean.valueOf(newLabelDisplayed));
			}
		}

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
						fireActionPerformed(1, 0);	//fire an action with this component as the source
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
	public Iterable<ActionListener> getActionListeners()
	{
		return getEventListenerManager().getListeners(ActionListener.class);	//remove the listener
	}

	/**Performs the action with default force and default option.
	An {@link ActionEvent} is fired to all registered {@link ActionListener}s.
	This method delegates to {@link #performAction(int, int)}.
	*/
	public void performAction()
	{
		performAction(1, 0);	//fire an event saying that the action has been performed with the default force and option
	}

	/**Performs the action with the given force and option.
	An {@link ActionEvent} is fired to all registered {@link ActionListener}s.
	@param force The zero-based force, such as 0 for no force or 1 for an action initiated by from a mouse single click.
	@param option The zero-based option, such as 0 for an event initiated by a mouse left button click or 1 for an event initiaged by a mouse right button click.
	*/
	public void performAction(final int force, final int option)
	{
		fireActionPerformed(force, option);	//fire an event saying that the action has been performed with the given force and option
	}

	/**Fires an action event to all registered action listeners.
	This method delegates to {@link #fireActionPerformed(ActionEvent)}.
	@param force The zero-based force, such as 0 for no force or 1 for an action initiated by from a mouse single click.
	@param option The zero-based option, such as 0 for an event initiated by a mouse left button click or 1 for an event initiaged by a mouse right button click.
	@see ActionListener
	@see ActionEvent
	*/
	protected void fireActionPerformed(final int force, final int option)
	{
		final EventListenerManager eventListenerManager=getEventListenerManager();	//get event listener support
		if(eventListenerManager.hasListeners(ActionListener.class))	//if there are action listeners registered
		{
			fireActionPerformed(new ActionEvent(this, force, option));	//create and fire a new action event
		}
	}

	/**Fires a given action event to all registered action listeners.
	@param actionEvent The action event to fire.
	*/
	protected void fireActionPerformed(final ActionEvent actionEvent)
	{
		for(final ActionListener actionListener:getEventListenerManager().getListeners(ActionListener.class))	//for each action listener
		{
			actionListener.actionPerformed(actionEvent);	//dispatch the action to the listener
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
	@return <code>true</code> if the child components changed as a result of the operation.
	*/
	protected boolean addComponent(final Component<?> component)
	{
		if(super.addComponent(component))	//do the default adding; if a change occurred
		{
			if(component instanceof Menu)	//if the component is a menu
			{
				((Menu<?>)component).setOpen(false);	//close the child menu
			}
			return true;	//indicate that the child components changed
		}
		else	//if the component list did not change
		{
			return false;	//indicate that the chidl components did not change
		}
	}

}
