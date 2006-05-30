package com.guiseframework.component.transfer;

import javax.mail.internet.*;

import static com.garretwilson.io.ContentTypeConstants.*;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.util.ArrayUtilities.*;

/**A abstract transferable that carries a Java object.
@param <S> The source of the transfer.
@param <T> The type of Java object to be transferred.
@author Garret Wilson
*/
public abstract class AbstractObjectTransferable<S, T> extends AbstractTransferable<S>
{
	/**The class representing the type of object to be transferred.*/
	private final Class<T> objectClass;

		/**@return The class representing the type of object to be transferred.*/
		public Class<T> getObjectClass() {return objectClass;}

	/**Source and object class constructor.
	@param source The source of the transferable data.
	@param objectClass The class indicating the type of object to be transferred.
	@exception NullPointerException if the provided source and/or object class is <code>null</code>.
	*/
	public AbstractObjectTransferable(final S source, final Class<T> objectClass)
	{
		super(source);	//construct the parent class
		this.objectClass=checkInstance(objectClass, "Object class cannot be null.");	//store the object class
	}

	/**Determines the content types available for this transfer.
	This implementation returns a content type in the form <code>application/x-java-class;class=<var>package.Class</var></code>.
	@return The content types available for this transfer.
	*/
	public ContentType[] getContentTypes()
	{
		try
		{
			final ContentType contentType=new ContentType(APPLICATION, X_JAVA_OBJECT, new ParameterList("class="+getObjectClass().getName()));	//create a content type appropriate for this object class TODO save a copy of this TODO use a constant
			return createArray(contentType);	//return the Java object content type
		}
		catch(final ParseException parseException)	//there should never be a parse exception, as we construct the content type from parameters with known syntax
		{
			throw new AssertionError(parseException);
		}
	}
}
