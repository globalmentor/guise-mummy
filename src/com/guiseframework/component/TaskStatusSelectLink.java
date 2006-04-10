package com.guiseframework.component;

import java.text.MessageFormat;

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

	/**Default constructor.*/
	public TaskStatusSelectLink()
	{
		this(new DefaultLabelModel(), new DefaultActionModel(), new DefaultValueModel<TaskStatus>(TaskStatus.class), new DefaultEnableable());	//construct the class with default models
	}
	
	/**Label model, action model, value model, and enableable object constructor.
	@param labelModel The component label model.
	@param actionModel The component action model.
	@param valueModel The component value model.
	@param enableable The enableable object in which to store enabled status.
	@exception NullPointerException if the given label model, action model, and/or enableable object is <code>null</code>.
	*/
	public TaskStatusSelectLink(final LabelModel labelModel, final ActionModel actionModel, final ValueModel<TaskStatus> valueModel, final Enableable enableable)
	{
		super(labelModel, actionModel, valueModel, enableable);	//construct the parent class		
		setSelectedIconResourceKey(SELECT_ACTION_SELECTED_ICON_RESOURCE_KEY);
		setUnselectedIconResourceKey(SELECT_ACTION_UNSELECTED_ICON_RESOURCE_KEY);
		setValueIconResourceKey(null, MessageFormat.format(TASK_STATUS_ICON_RESOURCE_KEY_FORMAT_PATTERN, ""));	//set the icon resource for no task status
		for(final TaskStatus taskStatus:TaskStatus.values())	//for each task status
		{
			setValueIconResourceKey(taskStatus, MessageFormat.format(TASK_STATUS_ICON_RESOURCE_KEY_FORMAT_PATTERN, taskStatus.toString()));	//set the icon resource for this task status
		}	
	}

}
