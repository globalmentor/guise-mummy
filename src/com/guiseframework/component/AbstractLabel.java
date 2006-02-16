package com.guiseframework.component;

import javax.mail.internet.ContentType;

import static com.garretwilson.util.ArrayUtilities.*;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.transfer.*;
import com.guiseframework.model.*;

/**An abstract label component.
This component installs a default export strategy supporting export of the following content types:
<ul>
	<li>The label content type.</li>
</ul>
@author Garret Wilson
*/
public abstract class AbstractLabel<C extends LabelComponent<C>> extends AbstractComponent<C> implements LabelComponent<C>
{

	/**The default export strategy for this component type.*/
	protected final static ExportStrategy<LabelComponent> DEFAULT_EXPORT_STRATEGY=new ExportStrategy<LabelComponent>()
			{
				/**Exports data from the given component.
				@param component The component from which data will be transferred.
				@return The object to be transferred, or <code>null</code> if no data can be transferred.
				*/
				public Transferable<LabelComponent> exportTransfer(final LabelComponent component)
				{
					return new DefaultTransferable(component);	//return a default transferable for this component
				}
			};

	/**Session constructor with a default model.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractLabel(final GuiseSession session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default data model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractLabel(final GuiseSession session, final String id)
	{
		this(session, id, new DefaultModel(session));	//construct the class with a default model
	}

	/**Session and model constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	*/
	public AbstractLabel(final GuiseSession session, final Model model)
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
	public AbstractLabel(final GuiseSession session, final String id, final Model model)
	{
		super(session, id/*TODO update with data model, model*/);	//construct the parent class
		addExportStrategy(DEFAULT_EXPORT_STRATEGY);	//install a default export strategy 
	}

	/**The default transferable object for a label.
	@author Garret Wilson
	*/
	protected static class DefaultTransferable extends AbstractTransferable<LabelComponent>
	{
		/**Source constructor.
		@param source The source of the transferable data.
		@exception NullPointerException if the provided source is <code>null</code>.
		*/
		public DefaultTransferable(final LabelComponent source)
		{
			super(source);	//construct the parent class
		}

		/**Determines the content types available for this transfer.
		This implementation returns the content type of the label.
		@return The content types available for this transfer.
		*/
		public ContentType[] getContentTypes() {return createArray(getSource().getLabelContentType());}

		/**Transfers data using the given content type.
		@param contentType The type of data expected.
		@return The transferred data, which may be <code>null</code>.
		@exception IllegalArgumentException if the given content type is not supported.
		*/
		public Object transfer(final ContentType contentType)
		{
			final LabelComponent source=getSource();	//get the source of the transfer
			if(contentType.match(source.getLabelContentType()))	//if we have the content type requested
			{
				return source.getSession().determineString(source.getLabel(), source.getLabelResourceKey());	//return the label text
			}
			else	//if we don't support this content type
			{
				throw new IllegalArgumentException("Content type not supported: "+contentType);
			}
		}
	}
}
