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

package com.guiseframework.component;

import static java.util.Objects.*;

import com.guiseframework.event.*;
import com.guiseframework.model.*;

/**
 * Abstract control with an action model.
 * @author Garret Wilson
 */
public abstract class AbstractActionControl extends AbstractControl implements ActionControl, LabelDisplayableComponent {

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

	/**
	 * Info model, action model, and enableable object constructor.
	 * @param infoModel The component info model.
	 * @param actionModel The component action model.
	 * @param enableable The enableable object in which to store enabled status.
	 * @throws NullPointerException if the given info model, action model, and/or enableable object is <code>null</code>.
	 */
	public AbstractActionControl(final InfoModel infoModel, final ActionModel actionModel, final Enableable enableable) {
		super(infoModel, enableable); //construct the parent class
		this.actionModel = requireNonNull(actionModel, "Action model cannot be null."); //save the action model
		this.actionModel.addActionListener(new ActionListener() { //create an action repeater to forward events to this component's listeners

			@Override
			public void actionPerformed(final ActionEvent actionEvent) { //if the action is performed
				final ActionEvent repeatActionEvent = new ActionEvent(AbstractActionControl.this, actionEvent); //copy the action event with this class as its source
				fireActionPerformed(repeatActionEvent); //fire the repeated action
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
		return getEventListenerManager().getListeners(ActionListener.class);
	}

	@Override
	public void performAction() {
		getActionModel().performAction(); //delegate to the installed action model, which will fire an event which we will catch and queue for refering
	}

	@Override
	public void performAction(final int force, final int option) {
		getActionModel().performAction(force, option); //delegate to the installed action model, which will fire an event which we will catch and refire
	}

	/**
	 * Fires an action event to all registered action listeners. This method delegates to {@link #fireActionPerformed(ActionEvent)}.
	 * @param force The zero-based force, such as 0 for no force or 1 for an action initiated by from a mouse single click.
	 * @param option The zero-based option, such as 0 for an event initiated by a mouse left button click or 1 for an event initiated by a mouse right button
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
}
