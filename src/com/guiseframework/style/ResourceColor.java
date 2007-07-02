package com.guiseframework.style;

import com.guiseframework.Guise;
import com.guiseframework.GuiseSession;

import static com.garretwilson.lang.ObjectUtilities.*;

/**A color that retrieves its values from the Guise session resources based upon a resource key.
When color-related access methods are called the calling thread must have access to the current Guise session.
@see GuiseSession#getColorResource(String)
@author Garret Wilson
*/
public class ResourceColor implements Color
{

	/**The key to accessing the color defined in the resources.*/
	private final String resourceKey;

		/**@return The key to accessing the color defined in the resources.*/
		public String getResourceKey () {return resourceKey;}

	/**Resource key constructor.
	@param resourceKey The key to accessing the color defined in the resources.
	@exception NullPointerException if the given resource key is <code>null</code>.
	*/
	public ResourceColor(final String resourceKey)
	{
		this.resourceKey=checkInstance(resourceKey, "Resource key cannot be null.");
	}

	/**Returns the color in the HSL color space.
	This implementation retrieves a color from the Guise session resources and delegates to that object.
	@return The color in the HSL color space.
	*/
  public HSLColor asHSL()
  {
  	return Guise.getInstance().getGuiseSession().getColorResource(getResourceKey()).asHSL();	//delegate to a color from the resources
  }

	/**Returns the color in the RGB color space.
	This implementation retrieves a color from the Guise session resources and delegates to that object.
	@return The color in the RGB color space.
	*/
  public RGBColor asRGB()
  {
  	return Guise.getInstance().getGuiseSession().getColorResource(getResourceKey()).asRGB();	//delegate to a color from the resources
  }

	/**@return A hash code value for the object.*/
	public int hashCode()
	{
		return getResourceKey().hashCode();	//calculate a hash code from the resource key
	}

	/**Indicates whether some other object is "equal to" this one.
	This implementation returns whether the object is another resource color with the same resource key.
	@param object The reference object with which to compare.
	@return <code>true</code> if this object is equivalent to the given object.
	@see #getResourceKey()
	*/
	public boolean equals(final Object object)
	{
		return object instanceof ResourceColor && ((ResourceColor)object).getResourceKey().equals(getResourceKey());	//see if the object is a resource color with the same resource key
	}

}
