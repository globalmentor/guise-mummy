package com.guiseframework.model;

/**A dummy tree node that holds no value and, if placed as the root of a tree, will not be displayed.
@author Garret Wilson
*/
public class DummyTreeNodeModel extends DefaultTreeNodeModel<String>	//TODO later make this just store a null Object rather than a String, once we have default editors for object types
{

	/**Constructs a dummy tree node model.*/
	public DummyTreeNodeModel()
	{
			//TODO decide what type to use for the default root node, and what type of editor to use in the default editor (Object won't work with the default text input component)
		super(String.class, null);	//construct the class with a null initial value
	}
}
