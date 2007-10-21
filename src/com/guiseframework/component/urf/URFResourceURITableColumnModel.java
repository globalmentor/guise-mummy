package com.guiseframework.component.urf;

import java.net.URI;

import com.guiseframework.model.DefaultTableColumnModel;

/**A model for a table column representing the reference URI of an URF resource.
This is implemented as a separate class so that other classes such as {@link URFResourceTableModel} can recognize the column's special semantics.
@author Garret Wilson
*/
public class URFResourceURITableColumnModel extends DefaultTableColumnModel<URI>
{

	/**Default constructor.*/
	public URFResourceURITableColumnModel()
	{
		this(null);	//construct the class with no label
	}
	
	/**Label constructor.
	@param labelText The text of the label.
	*/
	public URFResourceURITableColumnModel(final String labelText)
	{
		super(URI.class, labelText);	//construct the parent class
	}
	
}
