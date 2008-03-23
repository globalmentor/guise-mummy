package com.guiseframework.component;

import java.net.URI;

import com.guiseframework.model.*;
import com.guiseframework.prototype.LabelPrototype;

/**A label component.
This component installs a default export strategy supporting export of the following content types:
<ul>
	<li>The label content type.</li>
</ul>
@author Garret Wilson
*/
public class Label extends AbstractLabel
{

	/**Default constructor with a default label model.*/
	public Label()
	{
		this(new DefaultLabelModel());	//construct the class with a default label model
	}

	/**Label constructor.
	@param label The text of the label, or <code>null</code> if there should be no label.
	*/
	public Label(final String label)
	{
		this(label, null);	//construct the class with no icon
	}

	/**Label and icon constructor.
	@param label The text of the label, or <code>null</code> if there should be no label.
	@param icon The icon URI, which may be a resource URI, or <code>null</code> if there is no icon URI.
	*/
	public Label(final String label, final URI icon)
	{
		this(new DefaultLabelModel(label, icon));	//construct the class  with a default label model and the given label text
	}

	/**Label model constructor.
	@param labelModel The component label model.
	@exception NullPointerException if the given label model is <code>null</code>.
	*/
	public Label(final LabelModel labelModel)
	{
		super(labelModel);	//construct the parent class
	}

	/**Prototype constructor.
	@param labelPrototype The prototype on which this component should be based.
	@exception NullPointerException if the given prototype is <code>null</code>.
	*/
	public Label(final LabelPrototype labelPrototype)
	{
		this((LabelModel)labelPrototype);	//use the label prototype as the needed model
	}

}
