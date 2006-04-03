package com.guiseframework.component.transfer;

import static com.garretwilson.lang.ObjectUtilities.*;

import javax.mail.internet.ContentType;

import com.guiseframework.component.Component;

/**An abstract object that can be transferred, such as between components using drag and drop.
@param <T> The type of component this transferable supports.
@author Garret Wilson
*/
public abstract class AbstractTransferable<T extends Component<?>> implements Transferable<T>
{

	/**The source of the transferable data.*/
	private final T source;

		/**@return The source of the transferable data.*/
		public T getSource() {return source;}

	/**Determines whether this transferable can transfer data with the given content type.
	This implementation calls {@link Transferable#getContentTypes()}.
	@param contentType The type of data requested, which may include wildcards.
	@return <code>true</code> if this object can transfer data with the requested content type.
	*/
	public boolean canTransfer(final ContentType contentType)
	{
		for(final ContentType transferContentType:getContentTypes())	//for each content type
		{
			if(contentType.match(transferContentType))	//if this content type matches
			{
				return true;	//indicate that we found a match
			}
		}
		return false;	//indicate that there is no matching content type
	}

	/**Source constructor.
	@param source The source of the transferable data.
	@exception NullPointerException if the provided source is <code>null</code>.
	*/
	public AbstractTransferable(final T source)
	{
		this.source=checkInstance(source, "Source cannot be null.");
	}
}
