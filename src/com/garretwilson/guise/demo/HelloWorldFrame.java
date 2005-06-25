package com.garretwilson.guise.demo;

import com.garretwilson.guise.component.*;
import com.garretwilson.guise.component.layout.*;

/**Hello World Guise demonstration frame.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates frames and labels.
@author Garret Wilson
*/
public class HelloWorldFrame extends DefaultFrame
{

	/**Default constructor.*/
	public HelloWorldFrame()
	{
		super(new FlowLayout(Axis.Y));	//construct the parent class, flowing vertically
		setTitle("Hello World Guise Demonstration");	//set the frame label
		
		final Label helloWorldLabel=new Label();	//create a label
		helloWorldLabel.getModel().setText("Hello World!");	//set the text of the label, using its model
		add(helloWorldLabel);	//add the label to the frame
	}

}
