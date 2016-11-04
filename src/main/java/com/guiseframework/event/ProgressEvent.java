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

package com.guiseframework.event;

import static java.util.Objects.*;

import com.globalmentor.model.TaskState;

/**
 * An event used to notify interested parties that progress has been made for a particular task.
 * @param <P> The type of progress being made.
 * @author Garret Wilson
 * @see ProgressListener
 */
public class ProgressEvent<P> extends AbstractGuiseEvent {

	/** The task being performed, or <code>null</code> if not indicated. */
	private String task;

	/** @return The task being performed, or <code>null</code> if not indicated. */
	public String getTask() {
		return task;
	}

	/** The state of the task. */
	private TaskState taskState;

	/** @return The state of the task. */
	public TaskState getTaskState() {
		return taskState;
	}

	/** The current progress, or <code>null</code> if not known. */
	private final P progress;

	/** @return The current progress, or <code>null</code> if not known. */
	public P getProgress() {
		return progress;
	}

	/** The goal, or <code>null</code> if not known. */
	private final P completion;

	/** @return The goal, or <code>null</code> if not known. */
	public P getCompletion() {
		return completion;
	}

	/**
	 * Task state constructor with no known value or maximum value.
	 * @param source The object on which the event initially occurred.
	 * @param taskState The state of the task.
	 * @throws NullPointerException if the given task state is <code>null</code>.
	 */
	public ProgressEvent(final Object source, final TaskState taskState) {
		this(source, null, taskState); //construct the class with no task indicated		
	}

	/**
	 * Task state and value constructor with no known maximum value.
	 * @param source The object on which the event initially occurred.
	 * @param taskState The state of the task.
	 * @param value The current progress, or <code>null</code> if not known.
	 * @throws NullPointerException if the given task state is <code>null</code>.
	 */
	public ProgressEvent(final Object source, final TaskState taskState, final P value) {
		this(source, null, taskState, value); //construct the class with no task indicated
	}

	/**
	 * Task state, value, and maximum constructor.
	 * @param source The object on which the event initially occurred.
	 * @param taskState The state of the task.
	 * @param progress The current progress, or <code>null</code> if not known.
	 * @param completion The goal, or <code>null</code> if not known.
	 * @throws NullPointerException if the given task state is <code>null</code>.
	 */
	public ProgressEvent(final Object source, final TaskState taskState, final P progress, final P completion) {
		this(source, null, taskState, progress, completion); //construct the class with no task indicated
	}

	/**
	 * Task and task state constructor with no known value or maximum value.
	 * @param source The object on which the event initially occurred.
	 * @param task The task being performed, or <code>null</code> if not indicated.
	 * @param taskState The state of the task.
	 * @throws NullPointerException if the given task state is <code>null</code>.
	 */
	public ProgressEvent(final Object source, final String task, final TaskState taskState) {
		this(source, task, taskState, null); //construct the class with no known value		
	}

	/**
	 * Task, task state, and value constructor with no known maximum value.
	 * @param source The object on which the event initially occurred.
	 * @param task The task being performed, or <code>null</code> if not indicated.
	 * @param taskState The state of the task.
	 * @param progress The current progress, or <code>null</code> if not known.
	 * @throws NullPointerException if the given task state is <code>null</code>.
	 */
	public ProgressEvent(final Object source, final String task, final TaskState taskState, final P progress) {
		this(source, task, taskState, progress, null); //construct the class with no known maximum value
	}

	/**
	 * Task, task state, value, and maximum constructor.
	 * @param source The object on which the event initially occurred.
	 * @param task The task being performed, or <code>null</code> if not indicated.
	 * @param taskState The state of the task.
	 * @param value The current progress, or <code>null</code> if not known.
	 * @param completion The goal, or <code>null</code> if not known.
	 * @throws NullPointerException if the given task state is <code>null</code>.
	 */
	public ProgressEvent(final Object source, final String task, final TaskState taskState, final P value, final P completion) {
		super(source); //construct the parent class
		this.task = task;
		this.taskState = requireNonNull(taskState, "Task state cannot be null.");
		this.progress = value;
		this.completion = completion;
	}

	/**
	 * Source copy constructor.
	 * @param source The object on which the event initially occurred.
	 * @param progressEvent The existing progress event the values of which will be copied to this object.
	 * @throws NullPointerException if the given progress event is <code>null</code>.
	 */
	public ProgressEvent(final Object source, final ProgressEvent<P> progressEvent) {
		this(source, progressEvent.getTask(), progressEvent.getTaskState(), progressEvent.getProgress(), progressEvent.getCompletion()); //construct the class with values from the given progress event
	}

}
