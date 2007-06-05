package com.guiseframework.component;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.garretwilson.util.Debug;
import com.guiseframework.component.layout.*;
import com.guiseframework.event.*;
import com.guiseframework.model.ActionModel;
import com.guiseframework.model.Enableable;
import com.guiseframework.model.LabelModel;
import com.guiseframework.prototype.*;

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

	/**Label model, action model, enableable, and menu layout constructor.
	@param labelModel The component label model.
	@param actionModel The component action model.
	@param enableable The enableable object in which to store enabled status.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given label model, action model, enableable, and/or layout is <code>null</code>.
	*/
	public AbstractMenu(final LabelModel labelModel, final ActionModel actionModel, final Enableable enableable, final MenuLayout layout)
	{
		super(labelModel, enableable, layout);	//construct the parent class
		this.actionModel=checkInstance(actionModel, "Action model cannot be null.");	//save the action model
		this.actionModel.addActionListener(new ActionListener()	//create an action repeater to forward events to this component's listeners TODO create a common method to create a forwarding listener, if we can
				{
					public void actionPerformed(final ActionEvent actionEvent)	//if the action is performed
					{
						fireActionPerformed(1, 0);	//fire an action with this component as the source TODO important---shouldn't we use a copy constructor, here?
					}
				});
		addMouseListener(new MouseAdapter()	//listen for the mouse over the menu
				{
					/**Called when the mouse enters the target.
					@param mouseEvent The event providing mouse information
					*/
					public void mouseEntered(final MouseEnterEvent mouseEvent)
					{
						if(getParent() instanceof Menu)
						{
							setRollover(true);	//turn on the rollover state
						}
					}
		
					/**Called when the mouse exits the target.
					@param mouseEvent The event providing mouse information
					*/
					public void mouseExited(final MouseExitEvent mouseEvent)
					{
						if(getParent() instanceof Menu)
						{
							setRollover(false);	//turn off the rollover state
						}
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
		getActionModel().performAction();	//delegate to the installed action model, which will fire an event which we will catch and queue for refiring
	}

	/**Performs the action with the given force and option.
	An {@link ActionEvent} is fired to all registered {@link ActionListener}s.
	@param force The zero-based force, such as 0 for no force or 1 for an action initiated by from a mouse single click.
	@param option The zero-based option, such as 0 for an event initiated by a mouse left button click or 1 for an event initiaged by a mouse right button click.
	*/
	public void performAction(final int force, final int option)
	{
		getActionModel().performAction(force, option);	//delegate to the installed action model, which will fire an event which we will catch and queue for refiring
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

	/**Creates a component appropriate for the context of this component from the given prototype.
	This implementation creates the following components, in order of priority:
	<dl>
		<dt>{@link ActionPrototype}</dt> <dd>{@link Link}</dd>
	</dl>
	@param prototype The prototype of the component to create.
	@return A new component based upon the given prototype.
	@exception IllegalArgumentException if no component can be created from the given prototype
	*/
	public Component<?> createComponent(final Prototype prototype)
	{
		if(prototype instanceof ActionPrototype)	//action prototypes
		{
			return new Link((ActionPrototype)prototype);
		}
		else if(prototype instanceof ValuePrototype)	//value prototypes
		{
			final Class<?> valueClass=((ValuePrototype<?>)prototype).getValueClass();	//get the type of value represented
			if(Boolean.class.isAssignableFrom(valueClass))	//if a boolean value is represented
			{
				return new BooleanSelectLink((ValuePrototype<Boolean>)prototype);	//TODO testing; add comment to method signature
			}
		}
		return super.createComponent(prototype);	//delegate to the parent class
	}

}
