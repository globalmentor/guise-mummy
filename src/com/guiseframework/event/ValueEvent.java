package com.guiseframework.event;

/**An event reporting a value.
@param <V> The type of value to be reported.
@author Garret Wilson
*/
public class ValueEvent<V> extends AbstractGuiseEvent
{

	/**The value being reported.*/
	private final V value;

		/**@return The value being reported.*/
		public V getValue() {return value;}

	/**Source and value constructor.
	@param source The object on which the event initially occurred.
	@param value The value being reported.
	@exception NullPointerException if the given source is <code>null</code>.
	*/
	public ValueEvent(final Object source, final V value)
	{
		super(source);	//construct the parent class
		this.value=value;	//save the value
	}
}
