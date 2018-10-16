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

/**
 * A tree node model that represents a MAQRO group.
 * @author Garret Wilson
 */
public class GroupTreeNodeModel extends AbstractGroupTreeNodeModel<Group> {

	/** Default constructor with no initial value. */
	public GroupTreeNodeModel() {
		this(null); //construct the class with no initial value
	}

	/**
	 * Initial value constructor.
	 * @param initialValue The initial value, which will not be validated.
	 */
	public GroupTreeNodeModel(final Group initialValue) {
		this(null, initialValue); //construct the class with a null initial value
	}

	/**
	 * Followup subject and initial value constructor.
	 * @param followupEvaluation The followup evaluation which considers this interaction a followup in this context, or <code>null</code> if there is no followup
	 *          evaluation subject in this context.
	 * @param initialValue The initial value, which will not be validated.
	 */
	public GroupTreeNodeModel(final FollowupEvaluation followupEvaluation, final Group initialValue) {
		super(Group.class, followupEvaluation, initialValue); //construct the parent class
	}

}
