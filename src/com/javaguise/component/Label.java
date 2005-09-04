package com.javaguise.component;

import javax.mail.internet.ContentType;

import static com.garretwilson.util.ArrayUtilities.*;
import com.javaguise.component.transfer.*;
import com.javaguise.model.*;
import com.javaguise.session.GuiseSession;

/**A label component with a default transferable.
@author Garret Wilson
*/
public class Label extends AbstractModelComponent<LabelModel, Label>
{

	/**The default export strategy for this component type.*/
	protected final ExportStrategy DEFAULT_EXPORT_STRATEGY=new ExportStrategy<Label>()
			{
				/**Exports data from the given component.
				@param component The component from which data will be transferred.
				@return The object to be transferred, or <code>null</code> if no data can be transferred.
				*/
				public Transferable exportTransfer(final Label component)
				{
					return new DefaultTransferable();
				}
			};
	
	/**Session constructor with a default model.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public Label(final GuiseSession<?> session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default data model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Label(final GuiseSession<?> session, final String id)
	{
		this(session, id, new DefaultLabelModel(session));	//construct the class with a default model
	}

	/**Session and model constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	*/
	public Label(final GuiseSession<?> session, final LabelModel model)
	{
		this(session, null, model);	//construct the class, indicating that a default ID should be used
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Label(final GuiseSession<?> session, final String id, final LabelModel model)
	{
		super(session, id, model);	//construct the parent class
		addExportStrategy(new ExportStrategy<Label>()	//install an export strategy to return a default transferable 
			{
				/**Exports data from the given component.
				@param component The component from which data will be transferred.
				@return The object to be transferred, or <code>null</code> if no data can be transferred.
				*/
				public Transferable exportTransfer(final Label component)
				{
					return new DefaultTransferable();	//return a default transferable for labels
				}
			});
	}

	/**The default transferable object for a label.
	@author Garret Wilson
	*/
	protected class DefaultTransferable implements Transferable
	{
		/**Determines the content types available for this transfer.
		This implementation returns the content type of the label.
		@return The content types available for this transfer.
		*/
		public ContentType[] getTransferContentTypes() {return toArray(getModel().getLabelContentType());}

		/**Transfers data using the given content type.
		@param contentType The type of data expected.
		@return The transferred data.
		@exception IllegalArgumentException if the given content type is not supported.
		*/
		public Object transfer(final ContentType contentType)
		{
			final LabelModel labelModel=getModel();	//get the label model
			if(contentType.match(labelModel.getLabelContentType()))	//if we have the content type requested
			{
				return labelModel.getLabel();	//return the label
			}
			else	//if we don't support this content type
			{
				throw new IllegalArgumentException("Content type not supported: "+contentType);
			}
		}
	}

}
