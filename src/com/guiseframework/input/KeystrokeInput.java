package com.guiseframework.input;

import com.garretwilson.lang.Objects;
import static com.garretwilson.lang.Objects.*;

/**An encapsulation of key stroke input.
@author Garret Wilson
*/
public class KeystrokeInput extends AbstractGestureInput implements KeyboardInput
{

	/**The key that was pressed.*/
	private final Key key;

		/**The key that was pressed.*/
		public Key getKey() {return key;}

	/**Key and keys constructor.
	@parma key The key that was pressed.
	@param keys The keys that were pressed when this input occurred.
	@exception NullPointerException if the given key and/or keys is <code>null</code>.
	*/
	public KeystrokeInput(final Key key, final Key... keys)
	{
		super(keys);	//construct the parent class
		this.key=checkInstance(key, "Key cannot be null.");	//save the key
	}

	/**Returns the hash code of this object.
	This version extends the hash code of the underlying objects with the the key.
	@return The hash code of this object.
	*/
	public int hashCode()
	{
		return Objects.hashCode(super.hashCode(), key);	//extend the hash code with the key
	}
	
	/**Determines if this object equals another object.
	Besides the default checks, this version ensures that the keys are equal.
	@param object The object to compare with this object.
	@return <code>true</code> if the given object is considered equal to this object.
	*/
	public boolean equals(final Object object)
	{
		return super.equals(object) && getKey()==((KeystrokeInput)object).getKey();	//if the default checks pass, the object is of the correct type; compare keys
	}

	/**@return A string representation of this object.*/
	public String toString()
	{
		return getKey().toString()+' '+super.toString();	//add the key representation to the string
	}
}
