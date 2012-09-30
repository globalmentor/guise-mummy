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

import org.urframework.maqro.*;

/**A tree node model that represents a general interaction.
@param <V> The type of value contained in the tree node.
@author Garret Wilson
*/
public class DefaultInteractionTreeNodeModel<V extends Interaction> extends AbstractInteractionTreeNodeModel<V>
{

	/**Default constructor with no initial value.*/
	public DefaultInteractionTreeNodeModel()
	{
		this(null);	//construct the class with no initial value
	}

	/**Value class constructor with no initial value.
	@param valueClass The class indicating the type of value held in the model.
	*/
	public DefaultInteractionTreeNodeModel(final Class<V> valueClass)
	{
		this(valueClass, null);	//construct the class with no initial value
	}

	/**Initial value constructor.
	@param valueClass The class indicating the type of value held in the model.
	@param initialValue The initial value, which will not be validated.
	*/
	public DefaultInteractionTreeNodeModel(final Class<V> valueClass, final V initialValue)
	{
		this(valueClass, null, initialValue);	//construct the class with no property
	}

	/**Property and initial value constructor.
	@param valueClass The class indicating the type of value held in the model.
	@param followupEvaluation The followup evaluation which considers this interaction a followup in this context, or <code>null</code> if there is no followup evaluation subject in this context.
	@param initialValue The initial value, which will not be validated.
	*/
	public DefaultInteractionTreeNodeModel(final Class<V> valueClass, final FollowupEvaluation followupEvaluation, final V initialValue)
	{
		super(valueClass, followupEvaluation, initialValue);	//construct the parent class
	}

	/**Dynamically determines whether this node is a leaf.
	This version determines if there is a followup evaluation with a followup interaction, or a followup interaction in a list of followups.
	@return Whether this node should be considered a leaf with no children.
	*/
/*TODO del if not needed
	protected boolean determineLeaf()
	{
		if(!super.determineLeaf())	//if the base class doesn't think this is a leaf
		{
			return false;	//this isn't a leaf
		}
		final Question question=getValue();	//get the question
		if(question!=null)	//if we have a question
		{
			final RDFListResource evaluationList=question.getEvaluations();	//get the evaluations
			if(evaluationList!=null)	//if there are evaluations
			{
				for(final RDFResource evaluationResource:evaluationList)	//for each evaluation
				{
					if(evaluationResource instanceof FollowupEvaluation)	//if this is a followup evaluation
					{
						final FollowupEvaluation followupEvaluation=(FollowupEvaluation)evaluationResource;	//cast the evaluation to a followup evaluation
						final Interaction followup=followupEvaluation.getFollowup();	//get the followup interaction
						if(followup!=null)	//if this evaluation has a followup
						{
							return false;	//there is an evaluation with a followup, so this is not a leaf
						}
					}
				}
			}
			final RDFListResource followupList=question.getFollowups();	//get the followups
			if(followupList!=null && !followupList.isEmpty())	//if there are followups
			{
				return false;	//there is at least one followup, so this is not a leaf
			}
		}
		return true;	//we couldn't find any followup interactions, so this is a leaf 
	}
*/

	/**Dynamically determines children.
	This version includes followup evaluation interactions and followups listed as general followups. 
	@return The dynamically loaded list of children.
	*/
/*TODO del if not needed
	protected List<TreeNodeModel<?>> determineChildren()
	{
		final List<TreeNodeModel<?>> children=super.determineChildren();	//determine the default children
		final Question question=getValue();	//get the question
		if(question!=null)	//if we have a question
		{
			final Set<Interaction> followups=new HashSet<Interaction>();	//create a set to keep track of which interactions we've added as followups
			final RDFListResource evaluationList=question.getEvaluations();	//get the evaluations
			if(evaluationList!=null)	//if there are evaluations
			{
				for(final RDFResource evaluationResource:evaluationList)	//for each evaluation
				{
					if(evaluationResource instanceof FollowupEvaluation)	//if this is a followup evaluation
					{
						final FollowupEvaluation followupEvaluation=(FollowupEvaluation)evaluationResource;	//cast the evaluation to a followup evaluation
						final Interaction followup=followupEvaluation.getFollowup();	//get the followup interaction
						if(followup!=null)	//if this evaluation has a followup
						{
							followups.add(followup);	//add this followup to our set so that we won't duplicate it
							children.add(createFollowupInteractionTreeNode(followupEvaluation, followup));	//add a new followup node to the list, specifying its subject followup evaluation
						}
					}
				}
			}
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
		}
		return children;	//return the determined children
	}
*/
}
