/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.component.layout;

import static com.globalmentor.java.Classes.*;

import com.globalmentor.model.TaskState;

import io.guise.framework.model.*;

/**
 * Constraints on an individual component representing a task in a card layout.
 * @author Garret Wilson
 */
public class TaskCardConstraints extends CardConstraints {

	/** The task state bound property. */
	public static final String TASK_STATE_PROPERTY = getPropertyName(TaskCardConstraints.class, "taskState");

	/** The current task state of this card, or <code>null</code> if no task has been started in relation to this card. */
	private TaskState taskState = null;

	/** @return The current task state of this card, or <code>null</code> if no task has been started in relation to this card. */
	public TaskState getTaskState() {
		return taskState;
	}

	/**
	 * Sets the task state of the card. This is a bound property.
	 * @param newTaskState <code>true</code> if the corresponding card can be selected.
	 * @see #TASK_STATE_PROPERTY
	 */
	public void setTaskState(final TaskState newTaskState) {
		if(taskState != newTaskState) { //if the value is really changing
			final TaskState oldTaskState = taskState; //get the old value
			taskState = newTaskState; //actually change the value
			firePropertyChange(TASK_STATE_PROPERTY, oldTaskState, newTaskState); //indicate that the value changed
		}
	}

	/** Default constructor. */
	public TaskCardConstraints() {
		this(true); //construct the class, defaulting to enabled
	}

	/**
	 * Enabled constructor.
	 * @param enabled Whether the card is enabled.
	 */
	public TaskCardConstraints(final boolean enabled) {
		this((String)null, enabled); //construct the class with no label
	}

	/**
	 * Label constructor.
	 * @param label The text of the label.
	 */
	public TaskCardConstraints(final String label) {
		this(label, true); //construct the class, defaulting to enabled
	}

	/**
	 * Label and enabled constructor.
	 * @param label The text of the label.
	 * @param enabled Whether the card is enabled.
	 */
	public TaskCardConstraints(final String label, final boolean enabled) {
		this(new DefaultInfoModel(label), enabled); //construct the class with a default info model
	}

	/**
	 * Info model constructor.
	 * @param infoModel The info model representing the card label.
	 * @throws NullPointerException if the given info model is <code>null</code>.
	 */
	public TaskCardConstraints(final InfoModel infoModel) {
		this(infoModel, true); //construct the class, defaulting to enabled
	}

	/**
	 * Info model and enabled constructor.
	 * @param infoModel The info model representing the card label.
	 * @param enabled Whether the card is enabled.
	 * @throws NullPointerException if the given info model is <code>null</code>.
	 */
	public TaskCardConstraints(final InfoModel infoModel, final boolean enabled) {
		super(infoModel, enabled); //construct the parent class
	}

}
