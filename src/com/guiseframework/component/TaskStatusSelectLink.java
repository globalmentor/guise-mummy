package com.guiseframework.component;

import java.text.MessageFormat;

import com.guiseframework.GuiseSession;
import com.guiseframework.model.*;

/**Selectable link that stores a task status.
The link uses selected and unselected icons from the resources using resouce keys
	<code>select.action.selected.icon</code> and <code>select.action.unselected.icon</code>, respectively.
The link uses task status icons from the resouces using resouce keys
	<code>task.status.<var>taskStatus</var>.icon</code>,
	where <var>taskStatus</var> represents the task status enum value such as "INCOMPLETE",
	and <code>task.status..icon</code> for the <code>null</code> task status value.
@author Garret Wilson
*/
public class TaskStatusSelectLink extends ValueSelectLink<TaskStatus>
{
	
	/**The resource key for the selected icon.*/
	public final static String SELECT_ACTION_SELECTED_ICON_RESOURCE_KEY="select.action.selected.icon";
	/**The resource key for the unselected icon.*/
	public final static String SELECT_ACTION_UNSELECTED_ICON_RESOURCE_KEY="select.action.unselected.icon";

	/**The resource key format pattern for each task status.*/
	public final static String TASK_STATUS_ICON_RESOURCE_KEY_FORMAT_PATTERN="task.status.{0}.icon";

	/**Session constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public TaskStatusSelectLink(final GuiseSession session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used
	}

	/**Session, and model constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	*/
	public TaskStatusSelectLink(final GuiseSession session, final ValueModel<TaskStatus> model)
	{
		this(session, null, model);	//construct the component, indicating that a default ID should be used				
	}

	/**Session and ID constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public TaskStatusSelectLink(final GuiseSession session, final String id)
	{
		this(session, id, new DefaultValueModel<TaskStatus>(session, TaskStatus.class));	//construct the class with a default model
	}
	
	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public TaskStatusSelectLink(final GuiseSession session, final String id, final ValueModel<TaskStatus> model)
	{
		super(session, id, model);	//construct the parent class
		setSelectedIconResourceKey(SELECT_ACTION_SELECTED_ICON_RESOURCE_KEY);
		setUnselectedIconResourceKey(SELECT_ACTION_UNSELECTED_ICON_RESOURCE_KEY);
		setValueIconResourceKey(null, MessageFormat.format(TASK_STATUS_ICON_RESOURCE_KEY_FORMAT_PATTERN, ""));	//set the icon resource for no task status
		for(final TaskStatus taskStatus:TaskStatus.values())	//for each task status
		{
			setValueIconResourceKey(taskStatus, MessageFormat.format(TASK_STATUS_ICON_RESOURCE_KEY_FORMAT_PATTERN, taskStatus.toString()));	//set the icon resource for this task status
		}	
	}

}
