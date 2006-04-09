package com.guiseframework.component;

import com.guiseframework.model.*;

/**Control accepting a resource to be imported, such as a web file upload. 
@author Garret Wilson
*/
public class ResourceImportControl extends AbstractValueControl<ResourceImport, ResourceImportControl>
{

	/**Default constructor with a default value model.*/
	public ResourceImportControl()
	{
		this(new DefaultValueModel<ResourceImport>(ResourceImport.class));	//construct the class with a default model
	}

	/**Value model constructor.
	@param valueModel The component value model.
	@exception NullPointerException if the given value model is <code>null</code>.
	*/
	public ResourceImportControl(final ValueModel<ResourceImport> valueModel)
	{
		super(valueModel);	//construct the parent class
	}

}
