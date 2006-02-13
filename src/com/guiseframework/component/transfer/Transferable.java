package com.guiseframework.component.transfer;

import javax.mail.internet.ContentType;

/**An object that can be transferred, such as between components using drag and drop.
@param <S> The source of the transfer.
@author Garret Wilson
*/
public interface Transferable<S>
{

	/**@return The source of the transferable data.*/
	public S getSource();

	/**@return The content types available for this transfer.*/
	public ContentType[] getContentTypes();

	/**Determines whether this transferable can transfer data with the given content type.
	@param contentType The type of data requested, which may include wildcards.
	@return <code>true</code> if this object can transfer data with the requested content type.
	*/
	public boolean canTransfer(final ContentType contentType);

	/**Transfers data using the given content type.
	@param contentType The type of data expected.
	@return The transferred data, which may be <code>null</code>.
	@exception IllegalArgumentException if the given content type is not supported.
	*/
	public Object transfer(final ContentType contentType);
}
