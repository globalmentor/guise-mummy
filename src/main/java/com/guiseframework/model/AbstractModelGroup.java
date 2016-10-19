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

package com.guiseframework.model;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.globalmentor.java.Objects.*;

/**
 * An abstract implementation of a group of similar models for providing such functions as communication or mutual exclusion. This class is thread safe.
 * @param <M> The type of model contained in the group.
 * @author Garret Wilson.
 */
public abstract class AbstractModelGroup<M extends Model> implements ModelGroup<M> {

	/** The set of models. */
	private final Set<M> modelSet = new CopyOnWriteArraySet<M>(); //create a thread-safe set that is very efficient on reads because it works on a copy of the set (which we don't mind; changing to the set occurs infrequently)

	/** @return The set of models. */
	protected Set<M> getModelSet() {
		return modelSet;
	}

	/**
	 * Determines whether this group contains the given model.
	 * @param model The model being checked for group inclusion.
	 * @return <code>true</code> if the model is contained in this group, else <code>false</code>.
	 * @throws NullPointerException if the given model is <code>null</code>.
	 */
	public boolean contains(final Model model) {
		return modelSet.contains(checkInstance(model, "Model cannot be null.")); //see if the set of models contains this model TODO check for class cast exception
	}

	/**
	 * Adds a model to the group. If the model is already included in the group, no action occurs. This version delegates to {@link #addImpl(Model)}.
	 * @param model The model to add to the group.
	 * @throws NullPointerException if the given model is <code>null</code>.
	 */
	public void add(final M model) {
		if(!contains(model)) { //if the group doesn't already contain the model
			addImpl(model); ///actually add the model to the model set
		}
	}

	/**
	 * Actual implementation of adding a model to the group.
	 * @param model The model to add to the group.
	 * @throws NullPointerException if the given model is <code>null</code>.
	 */
	protected void addImpl(final M model) {
		modelSet.add(checkInstance(model, "Model cannot be null.")); //add this model to the model set
	}

	/**
	 * Removes a model from the group. If the model is not included in this group, no action occurs. This version delegates to {@link #removeImpl(Model)}.
	 * @param model The model to remove from the group.
	 * @throws NullPointerException if the given model is <code>null</code>.
	 */
	public void remove(final M model) {
		if(contains(model)) { //if the group contains the model
			removeImpl(model); ///actually remove the model from the model set
		}
	}

	/**
	 * Actual implementation of removing a model from the group.
	 * @param model The model to remove from the group.
	 * @throws NullPointerException if the given model is <code>null</code>.
	 */
	protected void removeImpl(final M model) {
		modelSet.remove(checkInstance(model, "Model cannot be null.")); //remove this model from the model set
	}

	/**
	 * Model constructor.
	 * @param models Zero or more models with which to initially place in the group.
	 * @throws NullPointerException if one of the models is <code>null</code>.
	 */
	public AbstractModelGroup(final M... models) {
		for(final M model : models) { //for each model
			add(model); //add this model to the group
		}
	}
}
