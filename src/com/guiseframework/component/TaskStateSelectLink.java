package com.guiseframework.component;

import java.net.URI;

import static com.guiseframework.Resources.*;

import com.guiseframework.Resources;
import com.guiseframework.model.*;

/**Selectable link that stores a task state.
The link uses selected and unselected icons from the resources using resouce keys
	<code>select.action.selected.glyph</code> and <code>select.action.unselected.glyph</code>, respectively.
The link uses task state icons from the resouces using resouce keys
	<code>task.state.<var>taskState</var>.glyph</code>,
	where <var>taskState</var> represents the task state enum value such as {@value TaskState#INCOMPLETE} in its resource key form
	such as <code>task.state.incomplete.glyph</code>,
	and <code>task.state..glyph</code> for the <code>null</code> task state value.
@author Garret Wilson
@see Resources#getResourceKeyName(Enum)
*/
public class TaskStateSelectLink extends ValueSelectLink<TaskState>
{
	
	/**The resource URI for the selected icon.*/
	public final static URI SELECT_ACTION_SELECTED_GLYPH_RESOURCE_URI=createURIResourceReference("theme.select.action.selected.glyph");
	/**The resource URI for the unselected icon.*/
	public final static URI SELECT_ACTION_UNSELECTED_GLYPH_RESOURCE_URI=createURIResourceReference("theme.select.action.unselected.glyph");


	/**Default constructor.*/
	public TaskStateSelectLink()
	{
		this(new DefaultLabelModel(), new DefaultActionModel(), new DefaultValueModel<TaskState>(TaskState.class), new DefaultEnableable());	//construct the class with default models
	}
	
	/**Label model, action model, value model, and enableable object constructor.
	@param labelModel The component label model.
	@param actionModel The component action model.
	@param valueModel The component value model.
	@param enableable The enableable object in which to store enabled status.
	@exception NullPointerException if the given label model, action model, and/or enableable object is <code>null</code>.
	*/
	public TaskStateSelectLink(final LabelModel labelModel, final ActionModel actionModel, final ValueModel<TaskState> valueModel, final Enableable enableable)
	{
		super(labelModel, actionModel, valueModel, enableable);	//construct the parent class		
		setSelectedIcon(SELECT_ACTION_SELECTED_GLYPH_RESOURCE_URI);
		setUnselectedIcon(SELECT_ACTION_UNSELECTED_GLYPH_RESOURCE_URI);
		setValueIcon(null, TaskState.getNoGlyph());	//set the icon resource for no task state
		for(final TaskState taskState:TaskState.values())	//for each task status
		{
			setValueIcon(taskState, taskState.getGlyph());	//set the icon resource for this task state
		}
	}

}
