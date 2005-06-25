package com.garretwilson.guise.validator;

import com.garretwilson.guise.component.Component;

/**Base exception class for all validation errors.
@author Garret Wilson
*/
public class ValidationException extends Exception
{

	/**The component for which validation failed, or <code>null</code> if the component is not known.*/
	private final Component<?> component;

		/**@return The component for which validation failed, or <code>null</code> if the component is not known.*/
		public Component<?> getComponent() {return component;}

	/**The value being validated, which may be of any type, or <code>null</code> if the value being validated is not available.*/
	private final Object value;

		/**@return The value being validated, which may be of any type, or <code>null</code> if the value being validated is not available.*/
		public Object getValue() {return value;}

	/**Constructs a new exception with <code>null</code> as its detail message.
	The cause is not initialized, and may subsequently be initialized by a call to {@link #initCause}.
	*/
	public ValidationException()
	{
		this((Component<?>)null);	//construct the class with no component
	}

	/**Constructs a new exception with the given component and <code>null</code> as its detail message.
	The cause is not initialized, and may subsequently be initialized by a call to {@link #initCause}.
	@param component The component for which validation failed, or <code>null</code> if the component is not known.
	*/
	public ValidationException(final Component<?> component)
	{
		this(component, (String)null);	//construct the class with no message
	}

	/**Constructs a new exception with the specified detail message.
	The cause is not initialized, and may subsequently be initialized by a call to {@link #initCause}.
	@param message The detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
	*/
	public ValidationException(final String message)
	{
		this(message, null);	//construct the class with the message and no value
	}

	/**Constructs a new exception with the specified component and detail message.
	The cause is not initialized, and may subsequently be initialized by a call to {@link #initCause}.
	@param component The component for which validation failed, or <code>null</code> if the component is not known.
	@param message The detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
	*/
	public ValidationException(final Component<?> component, final String message)
	{
		this(component, message, null);	//construct the class with the message and no value
	}

	/**Constructs a new exception with the specified detail message.
	The cause is not initialized, and may subsequently be initialized by a call to {@link #initCause}.
	@param message The detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
	@param value The value being validated, which may be of any type, or <code>null</code> if the value being validated is not available.
	*/
	public ValidationException(final String message, final Object value)
	{
		this(null, message, value);	//construct the class without a component
	}

	/**Constructs a new exception with the specified component and detail message.
	The cause is not initialized, and may subsequently be initialized by a call to {@link #initCause}.
	@param component The component for which validation failed, or <code>null</code> if the component is not known.
	@param message The detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
	@param value The value being validated, which may be of any type, or <code>null</code> if the value being validated is not available.
	*/
	public ValidationException(final Component<?> component, final String message, final Object value)
	{
		super(message);	//construct the parent class with the message
		this.component=component;	//save the component
		this.value=value;	//save the value
	}

	/**Constructs a new exception with the specified detail message and cause.
	@param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method).
	@param cause The cause (which is saved for later retrieval by the {@link #getCause()} method), or <code>null</code> if the cause is nonexistent or unknown.
	*/
	public ValidationException(final String message, final Throwable cause)
	{
		this(null, message, cause);	//construct the class without a component
	}

	/**Constructs a new exception with the specified component, detail message, and cause.
	@param component The component for which validation failed, or <code>null</code> if the component is not known.
	@param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method).
	@param cause The cause (which is saved for later retrieval by the {@link #getCause()} method), or <code>null</code> if the cause is nonexistent or unknown.
	*/
	public ValidationException(final Component<?> component, final String message, final Throwable cause)
	{
		super(message, cause);	//construct the parent class with the message and cause
		this.component=component;	//save the component
		value=null;	//show that we do not have a value
	}

	/**Constructs a new exception with the specified cause and a detail message of <code>(cause==null ? null : cause.toString())</code>.
	@param cause The cause (which is saved for later retrieval by the {@link #getCause()} method), or <code>null</code> if the cause is nonexistent or unknown.
	*/
	public ValidationException(final Throwable cause)
	{
		this((Component<?>)null, cause);	//construct the class without a component
	}

	/**Constructs a new exception with a component, the specified cause, and a detail message of <code>(cause==null ? null : cause.toString())</code>.
	@param component The component for which validation failed, or <code>null</code> if the component is not known.
	@param cause The cause (which is saved for later retrieval by the {@link #getCause()} method), or <code>null</code> if the cause is nonexistent or unknown.
	*/
	public ValidationException(final Component<?> component, final Throwable cause)
	{
		super(cause);	//construct the parent class with the cause
		this.component=component;	//save the component
		value=null;	//show that we do not have a value
	}

}
