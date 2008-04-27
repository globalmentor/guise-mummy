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

package com.guiseframework;

import java.util.*;
import static java.util.Collections.*;

import com.globalmentor.util.DefaultComparableIDNamed;

/**A general category used in Guise.
@author Garret Wilson
*/
public class Category extends DefaultComparableIDNamed<String, String>
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
