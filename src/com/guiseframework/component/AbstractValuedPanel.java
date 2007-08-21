package com.guiseframework.component;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.guiseframework.component.layout.*;

/**An abstract panel that represents a value.
@param <V> The type of value displayed within the component.
@author Garret Wilson
*/
public abstract class AbstractValuedPanel<V> extends AbstractPanel implements ValuedComponent<V>
{

	/**The class representing the type of value displayed within the component.*/
	private final Class<V> valueClass;

		/**@return The class representing the type of value displayed within the component.*/
		public Class<V> getValueClass() {return valueClass;}

	/**Value class and layout constructor.
	@param valueClass The class indicating the type of value displayed within the component.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given value class and/or layout is <code>null</code>.
	*/
	public AbstractValuedPanel(final Class<V> valueClass, final Layout<? extends Constraints> layout)
	{
		super(layout);	//construct the parent class
		this.valueClass=checkInstance(valueClass, "Value class cannot be null.");	//store the value class
	}

}
