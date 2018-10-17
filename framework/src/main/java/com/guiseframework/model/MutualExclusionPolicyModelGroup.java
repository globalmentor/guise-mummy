/*
 * Copyright © 2005-2013 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package com.guiseframework.model;

import java.beans.PropertyVetoException;
import java.util.*;

import com.globalmentor.beans.GenericPropertyChangeEvent;

/**
 * A group that ensures that only one boolean model in the group is set to <code>true</code> at the same time.
 * <p>
 * This class is declared final because it represents a particular defined semantics, no more and no less. This allows controllers to make assumptions about
 * models with only this listener, and offload mutual exclusion to client that have this capability built in.
 * </p>
 * <p>
 * The current implementation will only update the selected model for models that are already a part of the group.
 * </p>
 * @author Garret Wilson.
 */
public final class MutualExclusionPolicyModelGroup extends ValuePolicyModelGroup<Boolean> { //TODO improve class to update the selected model when models are added or removed

	/**
	 * Default constructor. Provided to prevent array of generic types warning.
	 */
	@SuppressWarnings("unchecked")
	public MutualExclusionPolicyModelGroup() {
		this(new ValueModel[0]);
	}

	/**
	 * Model constructor.
	 * @param models Zero or more models with which to initially place in the group.
	 * @throws NullPointerException if one of the models is <code>null</code>.
	 */
	public MutualExclusionPolicyModelGroup(final ValueModel<Boolean>... models) {
		super(models); //construct the parent class
	}

	/** The currently selected model. */
	private ValueModel<Boolean> selectedModel = null;

	/** @return The currently selected model. */
	public ValueModel<Boolean> getSelectedModel() {
		return selectedModel;
	}

	@Override
	public void propertyChange(final GenericPropertyChangeEvent<Boolean> propertyChangeEvent) {
		if(Boolean.TRUE.equals(propertyChangeEvent.getNewValue())) { //if this model is changing to true, change the other models to false
			final ValueModel<Boolean> source = (ValueModel<Boolean>)propertyChangeEvent.getSource(); //see which model changed TODO verify, improve cast
			selectedModel = source; //update the selected model
			final Set<ValueModel<Boolean>> modelSet = getModelSet(); //get the set of models in the group
			for(final ValueModel<Boolean> valueModel : modelSet) { //for each model in the group
				if(valueModel != source) { //if this is not the source (the source model should keep the value of true)
					try {
						valueModel.setValue(Boolean.FALSE); //set the values of the other value models to false (which will fire other events, but will be ignored by this class because the value is false)
					} catch(final PropertyVetoException propertyVetoException) { //if the change was vetoed, ignore the exception
					}
				}
			}
		}
	}

}
