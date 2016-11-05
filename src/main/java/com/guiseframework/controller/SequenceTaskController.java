/*
 * Copyright © 2000-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package com.guiseframework.controller;

import java.beans.PropertyVetoException;

import static java.util.Objects.*;

import com.globalmentor.beans.*;

import static com.globalmentor.java.Classes.*;
import com.globalmentor.model.*;

import com.guiseframework.prototype.*;

import static com.guiseframework.theme.Theme.*;

/**
 * Abstract base class for managing progression of a sequence.
 * @author Garret Wilson
 */
public class SequenceTaskController extends BoundPropertyObject {

	/** The bound property of whether navigation should be confirmed, of type {@link Boolean}. */
	public static final String CONFIRM_NAVIGATION_PROPERTY = getPropertyName(SequenceTaskController.class, "confirmNavigation");

	/** The action prototype for starting the sequence. */
	private final ActionPrototype startActionPrototype;

	/** @return The action prototype for starting the sequence. */
	public ActionPrototype getStartActionPrototype() {
		return startActionPrototype;
	}

	/** The action prototype for going to the previous step. */
	private final ActionPrototype previousActionPrototype;

	/** @return The action prototype for going to the previous step. */
	public ActionPrototype getPreviousActionPrototype() {
		return previousActionPrototype;
	}

	/** The action prototype for going to the next step. */
	private final ActionPrototype nextActionPrototype;

	/** @return The action prototype for going to the next step. */
	public ActionPrototype getNextActionPrototype() {
		return nextActionPrototype;
	}

	/** The action prototype for finishing the sequence. */
	private final ActionPrototype finishActionPrototype;

	/** @return The action prototype for finishing the sequence. */
	public ActionPrototype getFinishActionPrototype() {
		return finishActionPrototype;
	}

	/** The action prototype for confirming an action. */
	private final ActionPrototype confirmActionPrototype;

	/** @return The action prototype for confirming an action. */
	public ActionPrototype getConfirmActionPrototype() {
		return confirmActionPrototype;
	}

	/** The action prototype for advancing; serves as a proxy for the start, next, and finish actions, depending on the state of the sequence. */
	private final ProxyActionPrototype advanceActionPrototype;

	/**
	 * @return The action prototype for advancing; serves as a proxy for the start, next, and finish actions, depending on the state of the sequence.
	 * @see #getStartActionPrototype()
	 * @see #getNextActionPrototype()
	 * @see #getFinishActionPrototype()
	 */
	public ProxyActionPrototype getAdvanceActionPrototype() {
		return advanceActionPrototype;
	}

	/** The length of time, in milliseconds, to wait for confirmation when applicable. */
	protected static final int CONFIRM_DELAY = 5000;

	/** The timer that allows confirmation only within a specified time. */
	//TODO fix	private final Timer confirmTimer;

	/** @return The timer that allows confirmation only within a specified time. */
	//TODO del if not needed		protected Timer getConfirmTimer() {return confirmTimer;}

	/** The action prototype currently being confirmed and which, if confirmed, will be performed. */
	private ActionPrototype confirmingActionProtype;

	/** @return The action prototype currently being confirmed and which, if confirmed, will be performed. */
	public ActionPrototype getConfirmingActionPrototype() {
		return confirmingActionProtype;
	}

	/**
	 * Starts the confirmation timer and, if confirmation is received within the required amount of time, the given action is taken. Alternatively, if no action
	 * is given, the confirmation process is stopped. If the action is already waiting for confirmation, no action is taken.
	 * @param newConfirmingActionPrototype The action to perform if confirmation is received, or <code>null</code> if no action should be pending confirmation.
	 */
	public void setConfirmingActionPrototype(final ActionPrototype newConfirmingActionPrototype) {
		final ActionPrototype oldConfirmingActionPrototype = confirmingActionProtype; //get the action currently waiting for confirmation
		if(oldConfirmingActionPrototype != newConfirmingActionPrototype) { //if the pending action is really changing
			//TODO fix				confirmTimer.stop();	//stop any confirmations currently pending
			confirmingActionProtype = newConfirmingActionPrototype; //update the confirming action prototype
			if(newConfirmingActionPrototype != null) { //if there is a new action waiting to be confirmed
				//TODO fix					confirmTimer.restart();	//start the confirmation countdown				
			}
			//TODO fix				updateStatus();	//update the status to show whether an action is waiting to be confirmed
		}
	}

	/** Whether each navigation of the sequence must be confirmed. */
	private boolean confirmNavigation = false;

	/** @return <code>true</code> if each navigation should be confirmed. */
	public boolean isConfirmNavigation() {
		return confirmNavigation;
	}

	/**
	 * Sets whether each navigation must be confirmed. This is a bound property of type {@link Boolean}.
	 * @param newConfirmNavigation <code>true</code> if each navigation must be confirmed.
	 * @see #CONFIRM_NAVIGATION_PROPERTY
	 */
	public void setConfirmNavigation(final boolean newConfirmNavigation) {
		final boolean oldConfirmNavigation = confirmNavigation; //get the current index
		if(newConfirmNavigation != oldConfirmNavigation) { //if the confirm navigation is really changing
			confirmNavigation = newConfirmNavigation; //actually change the value
			firePropertyChange(CONFIRM_NAVIGATION_PROPERTY, oldConfirmNavigation, newConfirmNavigation);
		}
	}

	/** The sequence task being controlled. */
	private final SequenceTask task;

	/** @return The sequence task being controlled. */
	public SequenceTask getTask() {
		return task;
	}

	/**
	 * Sequence task constructor. This controller listens to bound properties of the task.
	 * @param task The sequence task being controlled.
	 * @throws NullPointerException if the given task is <code>null</code>.
	 */
	public SequenceTaskController(final SequenceTask task) {
		this.task = requireNonNull(task, "Task cannot be null.");
		startActionPrototype = new AbstractActionPrototype(LABEL_START, GLYPH_START) {

			@Override
			protected void action(final int force, final int option) {
				if(!isConfirmNavigation() || getConfirmingActionPrototype() == startActionPrototype) { //if this action is waiting to be confirmed
					getTask().goStart(); //start the sequence
					setConfirmingActionPrototype(null); //show that we're not waiting for confirmation on anything
				} else { //if we should confirm this action
					setConfirmingActionPrototype(startActionPrototype); //perform this action subject to confirmation
				}
			};
		};
		previousActionPrototype = new AbstractActionPrototype(LABEL_PREVIOUS, GLYPH_PREVIOUS) {

			@Override
			protected void action(final int force, final int option) {
				if(!isConfirmNavigation() || getConfirmingActionPrototype() == previousActionPrototype) { //if this action is waiting to be confirmed
					try {
						getTask().goPrevious(); //go to the previous step
					} catch(final PropertyVetoException propertyVetoException) {
						throw new IllegalStateException(propertyVetoException);
					}
					setConfirmingActionPrototype(null); //show that we're not waiting for confirmation on anything
				} else { //if we should confirm this action
					setConfirmingActionPrototype(previousActionPrototype); //perform this action subject to confirmation
				}

			};

		};
		nextActionPrototype = new AbstractActionPrototype(LABEL_NEXT, GLYPH_NEXT) {

			@Override
			protected void action(final int force, final int option) {
				if(!isConfirmNavigation() || getConfirmingActionPrototype() == nextActionPrototype) { //if this action is waiting to be confirmed
					try {
						getTask().goNext(); //go to the next step
					} catch(final PropertyVetoException propertyVetoException) {
						throw new IllegalStateException(propertyVetoException);
					}
					setConfirmingActionPrototype(null); //show that we're not waiting for confirmation on anything
				} else { //if we should confirm this action
					setConfirmingActionPrototype(nextActionPrototype); //perform this action subject to confirmation
				}

			};

		};
		finishActionPrototype = new AbstractActionPrototype(LABEL_FINISH, GLYPH_FINISH) {

			@Override
			protected void action(final int force, final int option) {
				if(!isConfirmNavigation() || getConfirmingActionPrototype() == finishActionPrototype) { //if this action is waiting to be confirmed
					getTask().goFinish(); //try to finish the sequence
					setConfirmingActionPrototype(null); //show that we're not waiting for confirmation on anything
				} else { //if we should confirm this action
					setConfirmingActionPrototype(finishActionPrototype); //perform this action subject to confirmation
				}
			}

		};

		advanceActionPrototype = new ProxyActionPrototype(startActionPrototype); //the advance action prototype will initially proxy the start action prototype
		confirmActionPrototype = new AbstractActionPrototype(LABEL_CONFIRM, GLYPH_CONFIRM) {

			@Override
			protected void action(final int force, final int option) {
				final ActionPrototype confirmingActionProtype = getConfirmingActionPrototype(); //see if there is an action waiting to be confirmed
				if(confirmingActionProtype != null) { //if there is an action waiting to be confirmed
					//TODO fix							confirmTimer.stop();	//the action is confirmed; suspend waiting for confirmation
					confirmingActionProtype.performAction(); //perform the confirming action
				}
			}

		};
		/*TODO fix
				confirmTimer=new Timer(CONFIRM_DELAY, new ActionListener() {	//create a new action listener that will remove any confirming action after a delay
							public void actionPerformed(final ActionEvent actionEvent) {setConfirmingActionPrototype(null);}	//if the timer runs out, show that there is no confirmation action
						});
				confirmTimer.setRepeats(false);	//we only have one waiting period for confirmation
		*/
		confirmingActionProtype = null; //there is currently no action being confirmed
		task.addPropertyChangeListener(Task.STATE_PROPERTY, new AbstractGenericPropertyChangeListener<Task>() { //update the controller when the status changes

			@Override
			public void propertyChange(final GenericPropertyChangeEvent<Task> genericPropertyChangeEvent) {
				update();
			}

		});
		task.addPropertyChangeListener(SequenceTask.SEQUENCE_INDEX_PROPERTY, new AbstractGenericPropertyChangeListener<Integer>() { //update the controller when the sequence index changing

			@Override
			public void propertyChange(final GenericPropertyChangeEvent<Integer> genericPropertyChangeEvent) {
				update();
			}

		});
		update(); //update the controller with the initial state
	}

	/**
	 * Updates the condition of the controller based upon the state. This method is a convenience method for complex components that would like to perform
	 * wholesale updates any prototypes, enabled/disabled status, proxied actions, etc. This implementation updates the states of action prototypes.
	 */
	protected void update() {
		final SequenceTask task = getTask(); //get the task
		final TaskState taskState = task.getState(); //get the state of the task
		getStartActionPrototype().setEnabled(taskState == TaskState.UNSTARTED); //only allow starting if we haven't started, yet
		getPreviousActionPrototype().setEnabled(task.hasPrevious()); //only allow going backwards if we have a previous step
		getNextActionPrototype().setEnabled(task.hasNext()); //only allow going backwards if we have a next step
		getFinishActionPrototype().setEnabled(taskState == TaskState.INCOMPLETE && !task.hasNext()); //only allow finishing if the task is in progress and there is no next step
		getConfirmActionPrototype().setEnabled(isConfirmNavigation() && getConfirmingActionPrototype() != null); //only allow confirmation if confirmation is enabled and there is an action waiting to be confirmed
		if(taskState != TaskState.UNSTARTED) { //if we've already started
			//determine if advancing should go to the next item in the sequence or finish
			getAdvanceActionPrototype().setProxiedPrototype(task.hasNext() ? getNextActionPrototype() : getFinishActionPrototype());
		}
		final ActionPrototype confirmingActionPrototype = getConfirmingActionPrototype(); //see if there is an action waiting to be confirmed
		if(confirmingActionPrototype != null) { //if there is an action waiting to be confirmed
			confirmingActionPrototype.setEnabled(false); //disable the confirming action, because it will be accessed indirectly through the confirmation action
		}
	}

}
