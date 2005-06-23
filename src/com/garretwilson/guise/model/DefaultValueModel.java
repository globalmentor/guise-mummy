package com.garretwilson.guise.model;

import com.garretwilson.lang.ObjectUtilities;
import static com.garretwilson.lang.ObjectUtilities.*;

/**A default implementation of a model for user input.
@author Garret Wilson
*/
public class DefaultValueModel<V> extends AbstractModel implements ValueModel<V>
{

	/**The input value, or <code>null</code> if there is no value.*/
	private V value=null;

		/**@return The input value, or <code>null</code> if there is no input value.*/
		public V getValue() {return value;}

		/**Sets the input value.
		This is a bound property that only fires a change event when the new value is different via the <code>equals()</code> method.
		@param newValue The input value of the model.
		@see ValueModel#VALUE_PROPERTY
		*/
		public void setValue(final V newValue)
		{
			if(!ObjectUtilities.equals(value, newValue))	//if the value is really changing (compare their values, rather than identity)
			{
				final V oldValue=value;	//get the old value
				value=newValue;	//actually change the value
				firePropertyChange(VALUE_PROPERTY, oldValue, newValue);	//indicate that the value changed
			}			
		}

	/**The class representing the type of value this model can hold.*/
	private final Class<V> valueClass;

		/**@return The class representing the type of value this model can hold.*/
		public Class<V> getValueClass() {return valueClass;}

	/**Constructs an input model indicating the type of value it can hold.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given class object is <code>null</code>.
	*/
	public DefaultValueModel(final Class<V> valueClass)
	{
		this.valueClass=checkNull(valueClass);	//store the value class
	}
}
