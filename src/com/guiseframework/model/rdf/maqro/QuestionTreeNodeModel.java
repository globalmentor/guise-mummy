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

package com.guiseframework.model.rdf.maqro;

import java.util.*;

import com.globalmentor.rdf.*;
import com.globalmentor.rdf.maqro.*;
import com.guiseframework.model.TreeNodeModel;
import com.guiseframework.model.rdf.AbstractRDFResourceTreeNodeModel;

/**A tree node model that represents a MAQRO question.
@author Garret Wilson
*/
public class QuestionTreeNodeModel extends AbstractInteractionTreeNodeModel<Question>
{

	/**Default constructor with no initial value.*/
	public QuestionTreeNodeModel()
	{
		this(null);	//construct the class with no initial value
	}

	/**Initial value constructor.
	@param initialValue The initial value, which will not be validated.
	*/
	public QuestionTreeNodeModel(final Question initialValue)
	{
		this(null, initialValue);	//construct the class with a null initial value
	}

	/**Followup subject and initial value constructor.
	@param followupEvaluation The followup evaluation which considers this interaction a followup in this context, or <code>null</code> if there is no followup evaluation subject in this context.
	@param initialValue The initial value, which will not be validated.
	*/
	public QuestionTreeNodeModel(final FollowupEvaluation followupEvaluation, final Question initialValue)
	{
		super(Question.class, followupEvaluation, initialValue);	//construct the parent class
	}

	/**Dynamically determines whether this node is a leaf.
	This version determines if there is a followup evaluation with a followup interaction, or a followup interaction in a list of followups.
	@return Whether this node should be considered a leaf with no children.
	*/
	protected boolean determineLeaf()
	{
		if(!super.determineLeaf())	//if the base class doesn't think this is a leaf
		{
			return false;	//this isn't a leaf
		}
		final Question question=getValue();	//get the question
		if(question!=null)	//if we have a question
		{
			final RDFListResource<?> evaluationList=question.getEvaluations();	//get the evaluations
			if(evaluationList!=null)	//if there are evaluations
			{
				for(final RDFObject evaluationObject:evaluationList)	//for each evaluation
				{
					if(evaluationObject instanceof FollowupEvaluation)	//if this is a followup evaluation
					{
						final FollowupEvaluation followupEvaluation=(FollowupEvaluation)evaluationObject;	//cast the evaluation to a followup evaluation
						final Interaction followup=followupEvaluation.getFollowup();	//get the followup interaction
						if(followup!=null)	//if this evaluation has a followup
						{
							return false;	//there is an evaluation with a followup, so this is not a leaf
						}
					}
				}
			}
/*TODO del when works
			final RDFListResource followupList=question.getFollowups();	//get the followups
			if(followupList!=null && !followupList.isEmpty())	//if there are followups
			{
				return false;	//there is at least one followup, so this is not a leaf
			}
*/
		}
		return true;	//we couldn't find any followup interactions, so this is a leaf 
	}

	/**Dynamically determines children.
	This version includes followup evaluation interactions and followups listed as general followups. 
	@return The dynamically loaded list of children.
	*/
	protected List<TreeNodeModel<?>> determineChildren()
	{
		final List<TreeNodeModel<?>> children=super.determineChildren();	//determine the default children
		final Question question=getValue();	//get the question
		if(question!=null)	//if we have a question
		{
			final Set<Interaction> followups=new HashSet<Interaction>();	//create a set to keep track of which interactions we've added as followups
			final RDFListResource<?> evaluationList=question.getEvaluations();	//get the evaluations
			if(evaluationList!=null)	//if there are evaluations
			{
				for(final RDFObject evaluationObject:evaluationList)	//for each evaluation
				{
					if(evaluationObject instanceof FollowupEvaluation)	//if this is a followup evaluation
					{
						final FollowupEvaluation followupEvaluation=(FollowupEvaluation)evaluationObject;	//cast the evaluation to a followup evaluation
						final Interaction followup=followupEvaluation.getFollowup();	//get the followup interaction
						if(followup!=null)	//if this evaluation has a followup
						{
							followups.add(followup);	//add this followup to our set so that we won't duplicate it
							children.add(createFollowupInteractionTreeNode(followupEvaluation, followup));	//add a new followup node to the list, specifying its subject followup evaluation
						}
					}
				}
			}
/*TODO del when works
			final RDFListResource followupList=question.getFollowups();	//get the followups
			if(followupList!=null)	//if there are followups
			{
				for(final RDFResource followupResource:followupList)	//for each followup
				{
					if(followupResource instanceof Interaction)	//if this is a followup interaction
					{
						if(!followups.contains(followupResource))	//if we haven't already added this followup as the object of a followup evaluation
						{
							children.add(createFollowupInteractionTreeNode(null, (Interaction)followupResource));	//add a new followup node to the list with no followup evaluation
						}
					}
				}
			}
*/
		}
		return children;	//return the determined children
	}
	
	/**Creates a child node to represent an interaction and optional subject followup evaluation.
	This version returns a {@link GroupTreeNodeModel} if the given resource is a {@link Group}.
	This version returns a {@link QuestionTreeNodeModel} if the given resource is a {@link Question}.
	Otherwise, this method delegates to {@link #createRDFResourceTreeNode(RDFResource, RDFResource)}.
	@param followupEvaluation The followup evaluation which considers this interaction a followup in this context, or <code>null</code> if there is no followup evaluation subject in this context.
	@param interaction The interaction to represent in the new node.
	@return A child node to represent the given property object resource.
	*/
	protected AbstractRDFResourceTreeNodeModel createFollowupInteractionTreeNode(final FollowupEvaluation followupEvaluation, final Interaction interaction)
	{
		if(interaction instanceof Group)	//if the interaction is a group
		{
			return new GroupTreeNodeModel(followupEvaluation, (Group)interaction);	//create a group tree node model
		}
		else if(interaction instanceof Question)	//if the interaction is a question
		{
			return new QuestionTreeNodeModel(followupEvaluation, (Question)interaction);	//create a question tree node model
		}
		else	//if we don't have a specific type for this interaction
		{
			return new DefaultInteractionTreeNodeModel(interaction.getClass(), followupEvaluation, interaction);	//create a general interaction tree node TODO improve
		}
	}

}
