package com.guiseframework;

import java.util.*;
import static java.util.Collections.*;

import com.garretwilson.util.IDNameObject;

/**A general category used in Guise.
@author Garret Wilson
*/
public class Category extends IDNameObject<String, String>
{

	/**The map of sub-categories; it is not thread-safe, but any changes will simply create a new list.*/
	private List<Category> categories=unmodifiableList(new ArrayList<Category>());

		/**The read-only iterable of categories.*/
		public Iterable<Category> getCategories() {return categories;}

		/**Sets the categories.
		@param categories The list of new categories.
		*/
		public void setCategories(final List<Category> categories)
		{
			this.categories=unmodifiableList(new ArrayList<Category>(categories));	//create a copy of the list and save the list
		}

	/**Constructor for a category with the same ID and name.
	@param id The ID of the category, which will also be used as the category's name.
	*/
	public Category(final String id)
	{
		this(id, id);	//construct the category, using the ID as both the ID and the name
	}

	/**Constructor specifying the ID and name.
	@param id The ID of the category.
	@param name The name of the category.
	*/
	public Category(final String id, final String name)
	{
		super(id, name);	//construct the parent class
	}
}
