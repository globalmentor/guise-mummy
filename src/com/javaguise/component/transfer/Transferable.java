package com.javaguise.component.transfer;

import javax.mail.internet.ContentType;

/**An object that can be transferred, such as between components using drag and drop.
@author Garret Wilson
*/
public interface Transferable
{

	/**@return The content types available for this transfer.*/
	public ContentType[] getTransferContentTypes();

	/**Transfers data using the given content type.
	@param contentType The type of data expected.
	@return The transferred data.
	@exception IllegalArgumentException if the given content type is not supported.
	*/
	public Object transfer(final ContentType contentType);
}
