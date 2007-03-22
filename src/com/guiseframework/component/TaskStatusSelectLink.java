package com.guiseframework.component;

import java.net.URI;
import java.text.MessageFormat;

import static com.guiseframework.Resources.*;

import com.guiseframework.Resources;
import com.guiseframework.model.*;

/**Selectable link that stores a task status.
The link uses selected and unselected icons from the resources using resouce keys
	<code>select.action.selected.glyph</code> and <code>select.action.unselected.glyph</code>, respectively.
The link uses task status icons from the resouces using resouce keys
	<code>task.status.<var>taskStatus</var>.glyph</code>,
	where <var>taskStatus</var> represents the task status enum value such as {@value TaskStatus#INCOMPLETE} in its resource key form
	such as <code>task.status.incomplete.glyph</code>,
	and <code>task.status..glyph</code> for the <code>null</code> task status value.
@author Garret Wilson
@see Resources#getResourceKeyName(Enum)
*/
public class TaskStatusSelectLink extends ValueSelectLink<TaskStatus>
{
	
	/**The resource URI for the selected icon.*/
	public final static URI SELECT_ACTION_SELECTED_GLYPH_RESOURCE_URI=createURIResourceReference("theme.select.action.selected.glyph");
	/**The resource URI for the unselected icon.*/
	public final static URI SELECT_ACTION_UNSELECTED_GLYPH_RESOURCE_URI=createURIResourceReference("theme.select.action.unselected.glyph");


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
		setSelectedIcon(SELECT_ACTION_SELECTED_GLYPH_RESOURCE_URI);
		setUnselectedIcon(SELECT_ACTION_UNSELECTED_GLYPH_RESOURCE_URI);
		setValueIcon(null, createURIResourceReference(MessageFormat.format(TaskStatus.GLYPH_RESOURCE_KEY_FORMAT_PATTERN, "")));	//set the icon resource for no task status
		for(final TaskStatus taskStatus:TaskStatus.values())	//for each task status
		{
			setValueIcon(taskStatus, createURIResourceReference(MessageFormat.format(TaskStatus.GLYPH_RESOURCE_KEY_FORMAT_PATTERN, Resources.getResourceKeyName(taskStatus))));	//set the icon resource for this task status
		}
	}

}
