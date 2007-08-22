package com.guiseframework.prototype;

import java.net.URI;

/**A boolean value prototype which allows toggling between two boolean states.
By default this prototype uses a default value of {@link Boolean#FALSE}.
@author Garret Wilson
*/
public class TogglePrototype extends ValuePrototype<Boolean> 
{

	/**Default constructor with a {@link Boolean#FALSE} default value.
	@param valueClass The class indicating the type of value held in the model.
	*/
	public TogglePrototype()
	{
		this(Boolean.FALSE);	//construct the class with a false default value
	}

	/**Default value constructor.
	@param valueClass The class indicating the type of value held in the model.
	@param defaultValue The default value, which will not be validated.
	*/
	public TogglePrototype(final Boolean defaultValue)
	{
		this(defaultValue, null);	//construct the class with no label
	}

	/**Label constructor with a {@link Boolean#FALSE} default value.
	@param valueClass The class indicating the type of value held in the model.
	@param label The text of the label, or <code>null</code> if there should be no label.
	*/
	public TogglePrototype(final String label)
	{
		this(Boolean.FALSE, label);	//construct the class with a false default value
	}

	/**Default value and label constructor.
	@param defaultValue The default value, which will not be validated.
	@param label The text of the label, or <code>null</code> if there should be no label.
	*/
	public TogglePrototype(final Boolean defaultValue, final String label)
	{
		this(defaultValue, label, null);	//construct the class with no icon
	}

	/**Label and icon constructor with a {@link Boolean#FALSE} default value.
	@param valueClass The class indicating the type of value held in the model.
	@param label The text of the label, or <code>null</code> if there should be no label.
	@param icon The icon URI, which may be a resource URI, or <code>null</code> if there is no icon URI.
	*/
	public TogglePrototype(final String label, final URI icon)
	{
		this(Boolean.FALSE, label, icon);	//construct the class with a false default value
	}

	/**Default value, label, and icon constructor.
	@param defaultValue The default value, which will not be validated.
	@param label The text of the label, or <code>null</code> if there should be no label.
	@param icon The icon URI, which may be a resource URI, or <code>null</code> if there is no icon URI.
	*/
	public TogglePrototype(final Boolean defaultValue, final String label, final URI icon)
	{
		super(Boolean.class, defaultValue, label, icon);	//construct the parent class
	}

}
