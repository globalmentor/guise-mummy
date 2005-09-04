package com.javaguise.component.transfer;

import static com.garretwilson.lang.ObjectUtilities.*;

import javax.mail.internet.ContentType;

import com.javaguise.component.Component;

/**An abstract object that can be transferred, such as between components using drag and drop.
@author Garret Wilson
*/
public abstract class AbstractTransferable implements Transferable	//TODO decide what to do with this class
{

	/**The source of the transferable data.*/
	private final Component<?> source;

		/**@return The source of the transferable data.*/
		public Component<?> getSource() {return source;}

	/**The content types available for this transfer.*/
	private ContentType[] transferContentTypes;

		/**@return The content types available for this transfer.*/
		public ContentType[] getTransferContentTypes() {return transferContentTypes.clone();}	//return a copy of the transfer content types array

	/**Source constructor.
	@param source The source of the transferable data.
	@param transferContentTypes The content types available for this transfer.
	@exception NullPointerException if the provided source or content types is <code>null</code>.
	*/
	public AbstractTransferable(final Component<?> source, final ContentType... transferContentTypes)
	{
		this.source=checkNull(source, "Source cannot be null");
		this.transferContentTypes=checkNull(transferContentTypes, "Transfer content types cannot be null.");
	}
}
