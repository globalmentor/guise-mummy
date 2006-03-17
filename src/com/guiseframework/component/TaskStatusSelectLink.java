package com.guiseframework.component;

import java.net.URI;

import com.guiseframework.GuiseSession;
import com.guiseframework.model.*;

/**Selectable link that stores a task status.
@author Garret Wilson
*/
public class TaskStatusSelectLink extends ValueSelectLink<TaskStatus>
{

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
			//TODO use resource keys
		setSelectedIcon(URI.create("guise/images/hand_point_right.gif"));
		setUnselectedIcon(URI.create("guise/images/blank.gif"));
		setValueIcon(null, URI.create("guise/images/blank.gif"));
		setValueIcon(TaskStatus.INCOMPLETE, URI.create("guise/images/question.gif"));
		setValueIcon(TaskStatus.ERROR, URI.create("guise/images/exclamation.gif"));
		setValueIcon(TaskStatus.COMPLETE, URI.create("guise/images/accept.gif"));
	}

}
