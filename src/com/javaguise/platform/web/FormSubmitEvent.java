package com.javaguise.platform.web;

import com.garretwilson.util.*;

import com.javaguise.controller.*;

/**A control event indicating that a full or partial form submission occurred.
@author Garret Wilson
*/
public class FormSubmitEvent implements ControlEvent
{

	/**The map of parameter lists.*/
	private final ListMap<String, Object> parameterListMap=new ArrayListHashMap<String, Object>();

		/**@return The map of parameter lists.*/
		public ListMap<String, Object> getParameterListMap() {return parameterListMap;}

}
