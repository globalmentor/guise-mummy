package com.guiseframework.model;

import static com.globalmentor.java.Classes.*;

/**An object that can be selected.
@author Garret Wilson
*/
public interface Selectable
{
	/**The bound property of whether the object is selected.*/
	public final static String SELECTED_PROPERTY=getPropertyName(Selectable.class, "selected");

	/**@return Whether the object is selected.*/
	public boolean isSelected();

	/**Sets whether the object is selected.
	This is a bound property of type <code>Boolean</code>.
	@param newSelected <code>true</code> if the object should be selected, else <code>false</code>.
	@see #SELECTED_PROPERTY
	*/
	public void setSelected(final boolean newSelected);

}
