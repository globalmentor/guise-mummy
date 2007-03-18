package com.guiseframework.component;

import com.guiseframework.model.*;

/**A label component.
This component installs a default export strategy supporting export of the following content types:
<ul>
	<li>The label content type.</li>
</ul>
@author Garret Wilson
*/
public class Label extends AbstractLabel<Label>
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
		this(new DefaultLabelModel(label));	//construct the label with a default label model and the given label text
	}

	/**Label model constructor.
	@param labelModel The component label model.
	@exception NullPointerException if the given label model is <code>null</code>.
	*/
	public Label(final LabelModel labelModel)
	{
		super(labelModel);	//construct the parent class
	}

}
