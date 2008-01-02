package com.guiseframework.model;

import static com.globalmentor.java.ClassUtilities.*;

/**An object that can be displayed or made to be hidden.
@author Garret Wilson
*/
public interface Displayable
{

	/**The bound property of whether the object is displayed or has no representation, taking up no space.*/
	public final static String DISPLAYED_PROPERTY=getPropertyName(Displayable.class, "displayed");

	/**@return Whether the object is displayed or has no representation, taking up no space.*/
	public boolean isDisplayed();

	/**Sets whether the object is displayed or has no representation, taking up no space.
	This is a bound property of type <code>Boolean</code>.
	@param newDisplayed <code>true</code> if the object should be displayed, else <code>false</code> if the object should take up no space.
	@see #DISPLAYED_PROPERTY
	*/
	public void setDisplayed(final boolean newDisplayed);
}
