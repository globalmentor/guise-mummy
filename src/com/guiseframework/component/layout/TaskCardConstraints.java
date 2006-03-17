package com.guiseframework.component.layout;

import static com.garretwilson.lang.ClassUtilities.*;

import com.guiseframework.GuiseSession;

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

	/**Session constructor.
	@param session The Guise session that owns this model.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public TaskCardConstraints(final GuiseSession session)
	{
		this(session, true);	//construct the class with no label
	}

	/**Session and enabled constructor.
	@param session The Guise session that owns this model.
	@param enabled Whether the card is enabled.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public TaskCardConstraints(final GuiseSession session, final boolean enabled)
	{
		this(session, (String)null, enabled);	//construct the class with no label
	}

	/**Session and label constructor.
	@param session The Guise session that owns this model.
	@param label The text of the label.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public TaskCardConstraints(final GuiseSession session, final String label)
	{
		this(session, label, true);	//construct the class, defaulting to enabled
	}

	/**Session, label, and enabled constructor.
	@param session The Guise session that owns this model.
	@param label The text of the label.
	@param enabled Whether the card is enabled.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public TaskCardConstraints(final GuiseSession session, final String label, final boolean enabled)
	{
		this(session, new DefaultLabelModel(session, label), enabled);	//construct the class with a default label model
	}

	/**Session and label model.
	@param session The Guise session that owns this model.
	@param labelModel The label model representing the card label.
	@exception NullPointerException if the given session and/or label model is <code>null</code>.
	*/
	public TaskCardConstraints(final GuiseSession session, final LabelModel labelModel)
	{
		this(session, labelModel, true);	//construct the class, defaulting to enabled
	}

	/**Session, label model, and enabled constructor.
	@param session The Guise session that owns this model.
	@param labelModel The label model representing the card label.
	@param enabled Whether the card is enabled.
	@exception NullPointerException if the given session and/or label model is <code>null</code>.
	*/
	public TaskCardConstraints(final GuiseSession session, final LabelModel labelModel, final boolean enabled)
	{
		super(session, labelModel, enabled);	//construct the parent class
	}

}
