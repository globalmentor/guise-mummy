/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework.component;

import javax.mail.internet.ContentType;

import static com.globalmentor.util.Arrays.*;

import com.guiseframework.component.transfer.*;
import com.guiseframework.model.*;

/**A abstract component the label of which comprises the main content of the component.
This component installs a default export strategy supporting export of the following content types:
<ul>
	<li>The label content type.</li>
</ul>
@author Garret Wilson
*/
public abstract class AbstractLabel extends AbstractComponent implements LabelComponent
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

	/**Default constructor with a default label model.*/
	public AbstractLabel()
	{
		this(new DefaultLabelModel());	//construct the class with a default label model
	}

	/**Label model constructor.
	@param labelModel The component label model.
	@exception NullPointerException if the given label or model is <code>null</code>.
	*/
	public AbstractLabel(final LabelModel labelModel)
	{
		super(labelModel);	//construct the parent class
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
				final String label=source.getLabel();	//get the label
				return label!=null ? source.getSession().dereferenceString(source.getLabel()) : null;	//return the label text, if any
			}
			else	//if we don't support this content type
			{
				throw new IllegalArgumentException("Content type not supported: "+contentType);
			}
		}
	}
}
