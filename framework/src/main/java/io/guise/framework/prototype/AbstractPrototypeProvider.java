/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.prototype;

import java.util.*;

import static java.util.Collections.*;

import com.globalmentor.beans.BoundPropertyObject;

/**
 * An abstract implementation of a prototype provider.
 * @author Garret Wilson
 */
public abstract class AbstractPrototypeProvider extends BoundPropertyObject implements PrototypeProvider {

	/** The prototype provisions currently provided by this provider. */
	private Set<PrototypeProvision<?>> prototypeProvisions = emptySet();

	@Override
	public Set<PrototypeProvision<?>> getPrototypeProvisions() {
		return prototypeProvisions;
	}

	/**
	 * Sets the prototype provisions currently provided by this provider. This is a bound property.
	 * @param newPrototypeProvisions The new prototype provisions currently provided by this provider.
	 * @see #PROTOTYPE_PROVISIONS_PROPERTY
	 */
	protected void setPrototypeProvisions(Set<PrototypeProvision<?>> newPrototypeProvisions) {
		if(!Objects.equals(prototypeProvisions, newPrototypeProvisions)) { //if the value is really changing
			if(newPrototypeProvisions != EMPTY_SET) { //if the new prototype provisions is not the empty set
				newPrototypeProvisions = unmodifiableSet(new HashSet<PrototypeProvision<?>>(newPrototypeProvisions)); //create an immutable copy of the set
			}
			final Set<PrototypeProvision<?>> oldPrototypeProvisions = prototypeProvisions; //get the old value
			prototypeProvisions = newPrototypeProvisions; //actually change the value
			firePropertyChange(PROTOTYPE_PROVISIONS_PROPERTY, oldPrototypeProvisions, newPrototypeProvisions); //indicate that the value changed
		}
	}

	/**
	 * Provides prototype provisions. This method is usually used internally to provide prototype provisions to be set using {@link #setPrototypeProvisions(Set)}.
	 * Subclasses may override this method to add or modify the provided prototype provisions.
	 * @return A mutable set of prototype provisions.
	 * @see #setPrototypeProvisions(Set)
	 */
	protected abstract Set<PrototypeProvision<?>> providePrototypes();

	/**
	 * Updates the available prototype provisions.
	 * @see #providePrototypes()
	 * @see #setPrototypeProvisions(Set)
	 */
	protected final void updatePrototypeProvisions() {
		setPrototypeProvisions(providePrototypes()); //update the prototype provisions			
	}

}
