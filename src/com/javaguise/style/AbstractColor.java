package com.javaguise.style;

import java.util.Arrays;

import com.garretwilson.lang.ObjectUtilities;

/**Abstract encapsulation of a color value.
@param <C> The type of color component for this color space.
@author Garret Wilson
@see http://www.neuro.sfc.keio.ac.jp/~aly/polygon/info/color-space-faq.html
@see http://www.color.org/
*/
public abstract class AbstractColor<C extends Enum<?>> implements Color<C>
{
	/**The color component values.*/
	private final float[] values;

	/**The precalculated hash code of the color.*/
	private final int hashCode;

	/**Constructs a color with the given components.
	@param values The values of components of the color in the correct color space, in the order of the component ordinals.
	@exception NullPointerException if the components is <code>null</code>.
TODO fix	@exception IllegalArgumentException if the number of component values do not equal the number of components.
	*/
	public AbstractColor(final float... values)
	{
		this.values=new float[values.length];	//create a new array of values
		for(int i=values.length-1; i>=0; --i)	//for each value
		{
			this.values[i]=checkComponentValue(values[i]);	//check and store this color component
		}
		this.hashCode=Arrays.hashCode(this.values);	//precalculate the hash code
	}

  /**Checks the range of a given color component.
	@param value The value to check.
	@return The checked value.
	@exception IllegalArgumentException if the given component is outside the range (0.0-1.0).
	*/
  protected static float checkComponentValue(final float value)
  {
  	if(value<0.0f || value>1.0f)	//if this value is outside the color component range
  	{
  		throw new IllegalArgumentException("Invalid color component value: "+value);
  	}
  	return value;	//return the value, as it passed the test
  }

  /**Determines the value of the given color component.
	@param component The color component for which a value should be retrieved.
	@return The value of the requested color component.
	*/
  public float getComponent(final C component)
  {
  	return values[component.ordinal()];	//look up the color component in the array
  }

  /**Determines the absolute value of the given color component with the given bit depth.
	For example, retrieving a component with value 0.5 and a bit depth of 16 would produce 128 or 0x80.
	@param component The color component for which a value should be retrieved.
	@param bitDepth The number of bits to use for the given color component.
	@return The absolute value of the requested color component at the given bit depth.
	@see #getComponent(Enum)
	*/
  public int getAbsoluteComponent(final C component, final int bitDepth)
  {
  	return Math.round(getComponent(component)*((1<<bitDepth)-1));	//multiply the component value by the range of values at the given bit depth
  }

	/**@return A hash code value for the object.*/
	public int hashCode()
	{
		return hashCode;	//return the precalculated hash code
	}

	/**Indicates whether some other object is "equal to" this one.
	This implementation returns whether the objects are of the same class with identical color component values.
	@param object The reference object with which to compare.
	@return <code>true</code> if this object is equivalent to the given object.
	*/
	public boolean equals(final Object object)
	{
		return object!=null && getClass().equals(object.getClass()) && Arrays.equals(values, ((AbstractColor)object).values);	//see if the classes and the component values are the same
	}

}
