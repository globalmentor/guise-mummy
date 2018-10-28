/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.component;

import static java.util.Objects.*;

import io.guise.framework.component.layout.*;
import io.guise.framework.event.*;
import io.guise.framework.model.*;
import io.guise.framework.prototype.*;

/**
 * An abstract menu component. This implementation initially closes any child menu added to this menu.
 * @author Garret Wilson
 */
public abstract class AbstractMenu extends AbstractContainerControl implements Menu {

	@Override
	public MenuLayout getLayout() {
		return (MenuLayout)super.getLayout();
	} //a menu can only have a menu layout

	/** The action model used by this component. */
	private final ActionModel actionModel;

	/** @return The action model used by this component. */
	protected ActionModel getActionModel() {
		return actionModel;
	}

	/** Whether the icon is displayed. */
	private boolean iconDisplayed = true;

	@Override
	public boolean isIconDisplayed() {
		return iconDisplayed;
	}

	@Override
	public void setIconDisplayed(final boolean newIconDisplayed) {
		if(iconDisplayed != newIconDisplayed) { //if the value is really changing
			final boolean oldIconDisplayed = iconDisplayed; //get the current value
			iconDisplayed = newIconDisplayed; //update the value
			firePropertyChange(ICON_DISPLAYED_PROPERTY, Boolean.valueOf(oldIconDisplayed), Boolean.valueOf(newIconDisplayed));
		}
	}

	/** Whether the label is displayed. */
	private boolean labelDisplayed = true;

	@Override
	public boolean isLabelDisplayed() {
		return labelDisplayed;
	}

	@Override
	public void setLabelDisplayed(final boolean newLabelDisplayed) {
		if(labelDisplayed != newLabelDisplayed) { //if the value is really changing
			final boolean oldLabelDisplayed = labelDisplayed; //get the current value
			labelDisplayed = newLabelDisplayed; //update the value
			firePropertyChange(LABEL_DISPLAYED_PROPERTY, Boolean.valueOf(oldLabelDisplayed), Boolean.valueOf(newLabelDisplayed));
		}
	}

	/** Whether the menu is open. */
	private boolean open = true;

	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	public void setOpen(final boolean newOpen) {
		if(open != newOpen) { //if the value is really changing
			final boolean oldOpen = open; //get the old value
			open = newOpen; //actually change the value
			firePropertyChange(OPEN_PROPERTY, Boolean.valueOf(oldOpen), Boolean.valueOf(newOpen)); //indicate that the value changed
		}
	}

	/** Whether the component is in a rollover state. */
	private boolean rollover = false;

	@Override
	public boolean isRollover() {
		return rollover;
	}

	@Override
	public void setRollover(final boolean newRollover) {
		if(rollover != newRollover) { //if the value is really changing
			final boolean oldRollover = rollover; //get the current value
			rollover = newRollover; //update the value
			firePropertyChange(ROLLOVER_PROPERTY, Boolean.valueOf(oldRollover), Boolean.valueOf(newRollover));
		}
	}

	/** Whether the menu children will be shown during rollover. */
	private boolean rolloverOpenEnabled = false;

	@Override
	public boolean isRolloverOpenEnabled() {
		return rolloverOpenEnabled;
	}

	@Override
	public void setRolloverOpenEnabled(final boolean newRolloverOpenEnabled) {
		if(rolloverOpenEnabled != newRolloverOpenEnabled) { //if the value is really changing
			final boolean oldRolloverOpenEnabled = rolloverOpenEnabled; //get the current value
			rolloverOpenEnabled = newRolloverOpenEnabled; //update the value
			firePropertyChange(ROLLOVER_OPEN_ENABLED_PROPERTY, Boolean.valueOf(oldRolloverOpenEnabled), Boolean.valueOf(newRolloverOpenEnabled));
		}
	}

	/**
	 * Info model, action model, enableable, and menu layout constructor.
	 * @param infoModel The component info model.
	 * @param actionModel The component action model.
	 * @param enableable The enableable object in which to store enabled status.
	 * @param layout The layout definition for the container.
	 * @throws NullPointerException if the given info model, action model, enableable, and/or layout is <code>null</code>.
	 */
	public AbstractMenu(final InfoModel infoModel, final ActionModel actionModel, final Enableable enableable, final MenuLayout layout) {
		super(infoModel, enableable, layout); //construct the parent class
		this.actionModel = requireNonNull(actionModel, "Action model cannot be null."); //save the action model
		this.actionModel.addActionListener(new ActionListener() { //create an action repeater to forward events to this component's listeners TODO create a common method to create a forwarding listener, if we can

					public void actionPerformed(final ActionEvent actionEvent) { //if the action is performed
						fireActionPerformed(1, 0); //fire an action with this component as the source TODO important---shouldn't we use a copy constructor, here?
					}
				});
		addMouseListener(new MouseAdapter() { //listen for the mouse over the control

			@Override
			public void mouseEntered(final MouseEnterEvent mouseEvent) {
				if(getParent() instanceof Menu) {
					setRollover(true); //turn on the rollover state
				}
			}

			@Override
			public void mouseExited(final MouseExitEvent mouseEvent) {
				if(getParent() instanceof Menu) {
					setRollover(false); //turn off the rollover state
				}
			}
		});
	}

	@Override
	public void addActionListener(final ActionListener actionListener) {
		getEventListenerManager().add(ActionListener.class, actionListener); //add the listener
	}

	@Override
	public void removeActionListener(final ActionListener actionListener) {
		getEventListenerManager().remove(ActionListener.class, actionListener); //remove the listener
	}

	@Override
	public Iterable<ActionListener> getActionListeners() {
		return getEventListenerManager().getListeners(ActionListener.class); //remove the listener
	}

	@Override
	public void performAction() {
		getActionModel().performAction(); //delegate to the installed action model, which will fire an event which we will catch and queue for refiring
	}

	@Override
	public void performAction(final int force, final int option) {
		getActionModel().performAction(force, option); //delegate to the installed action model, which will fire an event which we will catch and queue for refiring
	}

	/**
	 * Fires an action event to all registered action listeners. This method delegates to {@link #fireActionPerformed(ActionEvent)}.
	 * @param force The zero-based force, such as 0 for no force or 1 for an action initiated by from a mouse single click.
	 * @param option The zero-based option, such as 0 for an event initiated by a mouse left button click or 1 for an event initiaged by a mouse right button
	 *          click.
	 * @see ActionListener
	 * @see ActionEvent
	 */
	protected void fireActionPerformed(final int force, final int option) {
		if(getEventListenerManager().hasListeners(ActionListener.class)) { //if there are action listeners registered
			fireActionPerformed(new ActionEvent(this, force, option)); //create and fire a new action event
		}
	}

	/**
	 * Fires a given action event to all registered action listeners.
	 * @param actionEvent The action event to fire.
	 */
	protected void fireActionPerformed(final ActionEvent actionEvent) {
		for(final ActionListener actionListener : getEventListenerManager().getListeners(ActionListener.class)) { //for each action listener
			actionListener.actionPerformed(actionEvent); //dispatch the action to the listener
		}
	}

	@Override
	protected void addComponent(final int index, final Component childComponent) {
		super.addComponent(index, childComponent); //do the default adding
		if(childComponent instanceof Menu) { //if the component is a menu
			((Menu)childComponent).setOpen(false); //close the child menu
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Creates a component appropriate for the context of this component from the given prototype. This implementation creates the following components, in order
	 * of priority:
	 * </p>
	 * <dl>
	 * <dt>{@link ActionPrototype}</dt>
	 * <dd>{@link Link}</dd>
	 * </dl>
	 */
	@Override
	public Component createComponent(final Prototype prototype) {
		if(prototype instanceof ActionPrototype && !(prototype instanceof MenuPrototype)) { //action prototypes (don't create a link for menus, even though they are also action prototypes)
			return new Link((ActionPrototype)prototype);
		} else if(prototype instanceof ValuePrototype) { //value prototypes
			final Class<?> valueClass = ((ValuePrototype<?>)prototype).getValueClass(); //get the type of value represented
			if(Boolean.class.isAssignableFrom(valueClass)) { //if a boolean value is represented
				return new BooleanSelectLink((ValuePrototype<Boolean>)prototype); //TODO testing; add comment to method signature
			}
		}
		return super.createComponent(prototype); //delegate to the parent class
	}

}
