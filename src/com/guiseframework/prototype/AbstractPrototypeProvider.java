package com.guiseframework.prototype;

import java.util.*;

import static java.util.Collections.*;

import com.garretwilson.beans.BoundPropertyObject;

import com.globalmentor.java.Objects;

/**An abstract implementation of a prototype provider.
@author Garret Wilson
*/
public abstract class AbstractPrototypeProvider extends BoundPropertyObject implements PrototypeProvider
{

	/**The prototype provisions currently provided by this provider.*/
	private Set<PrototypeProvision<?>> prototypeProvisions=emptySet();
	
		/**Returns the prototypes currentl provisions provided by this provider.
		This is a read-only bound property.
		@return The prototype provisions currently provided by this provider.
		@see #PROTOTYPE_PROVISIONS_PROPERTY
		*/
		public Set<PrototypeProvision<?>> getPrototypeProvisions() {return prototypeProvisions;}

		/**Sets the prototype provisions currently provided by this provider.
		This is a bound property.
		@param newPrototypeProvisions The new prototype provisions currently provided by this provider.
		@see #PROTOTYPE_PROVISIONS_PROPERTY
		*/
		protected void setPrototypeProvisions(Set<PrototypeProvision<?>> newPrototypeProvisions)
		{
			if(!Objects.equals(prototypeProvisions, newPrototypeProvisions))	//if the value is really changing
			{
				if(newPrototypeProvisions!=EMPTY_SET)	//if the new prototype provisions is not the empty set
				{
					newPrototypeProvisions=unmodifiableSet(new HashSet<PrototypeProvision<?>>(newPrototypeProvisions));	//create an immutable copy of the set
				}
				final Set<PrototypeProvision<?>> oldPrototypeProvisions=prototypeProvisions;	//get the old value
				prototypeProvisions=newPrototypeProvisions;	//actually change the value
				firePropertyChange(PROTOTYPE_PROVISIONS_PROPERTY, oldPrototypeProvisions, newPrototypeProvisions);	//indicate that the value changed
			}
		}

	/**Provides prototype provisions.
	This method is usually used internally to provide prototype provisions to be set using {@link #setPrototypeProvisions(Set)}.
	Subclasses may override this method to add or modify the provided prototype provisions.
	@return A mutable set of prototype provisions.
	@see #setPrototypeProvisions(Set)
	*/
	protected abstract Set<PrototypeProvision<?>> providePrototypes();

	/**Updates the available prototype provisions.
	@see #providePrototypes()
	@see #setPrototypeProvisions(Set)
	*/
	protected final void updatePrototypeProvisions()
	{
		setPrototypeProvisions(providePrototypes());	//update the prototype provisions			
	}

}
