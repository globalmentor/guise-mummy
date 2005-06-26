package com.garretwilson.guise.demo;

import com.garretwilson.guise.component.*;
import com.garretwilson.guise.component.layout.*;
import com.garretwilson.guise.session.GuiseSession;

/**Hello World Guise demonstration frame.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates frames and labels.
@author Garret Wilson
*/
public class HelloWorldFrame extends NavigationFrame
{

	/**Guise session constructor.
	@param session The Guise session that owns this frame.
	*/
	public HelloWorldFrame(final GuiseSession<?> session)
	{
		super(session, new FlowLayout(Axis.Y));	//construct the parent class, flowing vertically
		setTitle("Hello World Guise Demonstration");	//set the frame label
		
		final Label helloWorldLabel=new Label(session);	//create a label
		helloWorldLabel.getModel().setText("Hello World!");	//set the text of the label, using its model
		add(helloWorldLabel);	//add the label to the frame
	}

}
