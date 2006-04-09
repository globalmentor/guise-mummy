package com.guiseframework.component.layout;

import static com.garretwilson.lang.ClassUtilities.*;

import com.guiseframework.model.DefaultLabelModel;
import com.guiseframework.model.LabelModel;
import com.guiseframework.model.TaskStatus;

/**Constraints on an individual component representing a task in a card layout.
@author Garret Wilson
*/
public class TaskCardConstraints extends CardConstraints
{

	/**The task status bound property.*/
	public final static String TASK_STATUS_PROPERTY=getPropertyName(TaskCardConstraints.class, "taskStatus");

	/**The current task status of this card, or <code>null</code> if no task has been started in relation to this card.*/
	private TaskStatus taskStatus=null;

		/**@return The current task status of this card, or <code>null</code> if no task has been started in relation to this card.*/
		public TaskStatus getTaskStatus() {return taskStatus;}

		/**Sets the task status of the card.
		This is a bound property.
		@param newTaskStatus <code>true</code> if the corresponding card can be selected.
		@see #TASK_STATUS_PROPERTY
		*/
		public void setTaskStatus(final TaskStatus newTaskStatus)
		{
			if(taskStatus!=newTaskStatus)	//if the value is really changing
			{
				final TaskStatus oldTaskStatus=taskStatus;	//get the old value
				taskStatus=newTaskStatus;	//actually change the value
				firePropertyChange(TASK_STATUS_PROPERTY, oldTaskStatus, newTaskStatus);	//indicate that the value changed
			}			
		}

	/**Default constructor.*/
	public TaskCardConstraints()
	{
		this(true);	//construct the class, defaulting to enabled
	}

	/**Enabled constructor.
	@param enabled Whether the card is enabled.
	*/
	public TaskCardConstraints(final boolean enabled)
	{
		this((String)null, enabled);	//construct the class with no label
	}

	/**Label constructor.
	@param label The text of the label.
	*/
	public TaskCardConstraints(final String label)
	{
		this(label, true);	//construct the class, defaulting to enabled
	}

	/**Label and enabled constructor.
	@param label The text of the label.
	@param enabled Whether the card is enabled.
	*/
	public TaskCardConstraints(final String label, final boolean enabled)
	{
		this(new DefaultLabelModel(label), enabled);	//construct the class with a default label model
	}

	/**Label model constructor.
	@param labelModel The label model representing the card label.
	@exception NullPointerException if the given label model is <code>null</code>.
	*/
	public TaskCardConstraints(final LabelModel labelModel)
	{
		this(labelModel, true);	//construct the class, defaulting to enabled
	}

	/**Label model and enabled constructor.
	@param labelModel The label model representing the card label.
	@param enabled Whether the card is enabled.
	@exception NullPointerException if the given label model is <code>null</code>.
	*/
	public TaskCardConstraints(final LabelModel labelModel, final boolean enabled)
	{
		super(labelModel, enabled);	//construct the parent class
	}

}
