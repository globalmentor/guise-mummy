package com.guiseframework.model.rdf.maqro;

import com.garretwilson.rdf.maqro.*;

/**A tree node model that represents a MAQRO group.
@author Garret Wilson
*/
public class GroupTreeNodeModel extends AbstractGroupTreeNodeModel<Group>
{

	/**Default constructor with no initial value.*/
	public GroupTreeNodeModel()
	{
		this(null);	//construct the class with no initial value
	}

	/**Initial value constructor.
	@param initialValue The initial value, which will not be validated.
	*/
	public GroupTreeNodeModel(final Group initialValue)
	{
		this(null, initialValue);	//construct the class with a null initial value
	}

	/**Followup subject and initial value constructor.
	@param followupEvaluation The followup evaluation which considers this interaction a followup in this context, or <code>null</code> if there is no followup evaluation subject in this context.
	@param initialValue The initial value, which will not be validated.
	*/
	public GroupTreeNodeModel(final FollowupEvaluation followupEvaluation, final Group initialValue)
	{
		super(Group.class, followupEvaluation, initialValue);	//construct the parent class
	}

}
