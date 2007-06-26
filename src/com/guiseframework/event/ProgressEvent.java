package com.guiseframework.event;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.guiseframework.model.TaskState;

/**An event used to notify interested parties that progress has been made for a	particular task.
@author Garret Wilson
@see ProgressListener
*/
public class ProgressEvent extends AbstractGuiseEvent
{

	/**The task being performed, or <code>null</code> if not indicated.*/
	private String task;

		/**@return The task being performed, or <code>null</code> if not indicated.*/
		public String getTask() {return task;}

	/**The state of the task.*/
	private TaskState taskState;

		/**@return The state of the task.*/
		public TaskState getTaskState() {return taskState;}

	/**The current progress, or <code>-1</code> if not known.*/
	private long value=-1;

		/**@return The current progress, or <code>-1</code> if not known.*/
		public long getValue() {return value;}

	/**The goal, or <code>-1</code> if not known.*/
	private long maximumValue=-1;

		/**@return The goal, or <code>-1</code> if not known.*/
		public long getMaximumValue() {return maximumValue;}

	/**Task state constructor with no known value or maximum value.
	@param source The object on which the event initially occurred.
	@param taskState The state of the task.
	@exception NullPointerException if the given task state is <code>null</code>.
	*/
	public ProgressEvent(final Object source, final TaskState taskState)
	{
		this(source, null, taskState);	//construct the class with no task indicated		
	}

	/**Task state and value constructor with no known maximum value.
	@param source The object on which the event initially occurred.
	@param taskState The state of the task.
	@param value The current progress, or <code>-1</code> if not known.
	@exception NullPointerException if the given task state is <code>null</code>.
	*/
	public ProgressEvent(final Object source, final TaskState taskState, final long value)
	{
		this(source, null, taskState, value);	//construct the class with no task indicated
	}

	/**Task state, value, and maximum constructor.
	@param source The object on which the event initially occurred.
	@param taskState The state of the task.
	@param value The current progress, or <code>-1</code> if not known.
	@param maximumValue The goal, or <code>-1</code> if not known.
	@exception NullPointerException if the given task state is <code>null</code>.
	*/
	public ProgressEvent(final Object source, final TaskState taskState, final long value, final long maximumValue)
	{
		this(source, null, taskState, value, maximumValue);	//construct the class with no task indicated
	}

	/**Task and task state constructor with no known value or maximum value.
	@param source The object on which the event initially occurred.
	@param task The task being performed, or <code>null</code> if not indicated.
	@param taskState The state of the task.
	@exception NullPointerException if the given task state is <code>null</code>.
	*/
	public ProgressEvent(final Object source, final String task, final TaskState taskState)
	{
		this(source, task, taskState, -1);	//construct the class with no known value		
	}

	/**Task, task state, and value constructor with no known maximum value.
	@param source The object on which the event initially occurred.
	@param task The task being performed, or <code>null</code> if not indicated.
	@param taskState The state of the task.
	@param value The current progress, or <code>-1</code> if not known.
	@exception NullPointerException if the given task state is <code>null</code>.
	*/
	public ProgressEvent(final Object source, final String task, final TaskState taskState, final long value)
	{
		this(source, task, taskState, value, -1);	//construct the class with no known maximum value
	}

	/**Task, task state, value, and maximum constructor.
	@param source The object on which the event initially occurred.
	@param task The task being performed, or <code>null</code> if not indicated.
	@param taskState The state of the task.
	@param value The current progress, or <code>-1</code> if not known.
	@param maximumValue The goal, or <code>-1</code> if not known.
	@exception NullPointerException if the given task state is <code>null</code>.
	*/
	public ProgressEvent(final Object source, final String task, final TaskState taskState, final long value, final long maximumValue)
	{
		super(source);	//construct the parent class
		this.task=task;
		this.taskState=checkInstance(taskState, "Task state cannot be null.");
		this.value=value;
		this.maximumValue=maximumValue;
	}

	/**Source copy constructor.
	@param source The object on which the event initially occurred.
	@param progressEvent The existing progress event the values of which will be copied to this object.
	@exception NullPointerException if the given progress event is <code>null</code>.
	*/
	public ProgressEvent(final Object source, final ProgressEvent progressEvent)
	{
		this(source, progressEvent.getTask(), progressEvent.getTaskState(), progressEvent.getValue(), progressEvent.getMaximumValue());	//construct the class with values from the given progress event
	}

}
