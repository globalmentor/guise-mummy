package com.garretwilson.guise.test;

import com.garretwilson.guise.component.*;

/**Test frame for a home page.
@author Garret Wilson
*/
public class HomeFrame extends DefaultFrame
{

	/**ID constructor.
	@param id The component identifier.
	@exception NullPointerException if the given identifier is <code>null</code>.
	*/
	public HomeFrame(final String id)
	{
		super(id);	//construct the parent class
		final Label testLabel=new Label("testLabel");
		testLabel.setStyleID("title");
		add(testLabel);	//add a new label
		final Button testButton=new DefaultButton("testButton");
		add(testButton);	//add a new button
		final Label buttonLabel=new Label("buttonLabel");
		testButton.add(testLabel);	//add a new label
	}

}
