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

package com.guiseframework.theme;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.ploop.graph.PLOOPURFProcessor;
import org.urframework.AbstractClassTypedURFResource;
import org.urframework.select.Select;
import org.urframework.select.Selector;

import com.globalmentor.util.DataException;

import static com.globalmentor.java.Objects.*;
import static com.guiseframework.theme.Theme.*;
import static org.urframework.select.Select.*;

/**A rule for specifying part of a theme.
@author Garret Wilson
*/
public class Rule extends AbstractClassTypedURFResource
{

	/**Default constructor.*/
	public Rule()
	{
		this(null);	//construct the class with no reference URI
	}

	/**Reference URI constructor.
	@param referenceURI The reference URI for the new resource.
	*/
	public Rule(final URI referenceURI)
	{
		super(referenceURI, THEME_NAMESPACE_URI);  //construct the parent class
	}

	/**Return this rule's selector, or <code>null</code> if this rule has no selector property or the value is not a {@link Selector}.
	@return This rule's selector, or <code>null</code> if this rule has no selector property or the value is not a {@link Selector}.
	@see Select#SELECTOR_PROPERTY_URI
	*/
	public Selector getSelector()
	{
		return asInstance(getPropertyValue(SELECTOR_PROPERTY_URI), Selector.class);	//return the select.selector value
	}

	/**Sets this rule's selector.
	@param selector This rule's selector, or <code>null</code> if this rule should have no selector property.
	*/
	public void setSelector(final Selector selector)
	{
		setPropertyValue(SELECTOR_PROPERTY_URI, selector);	//set the select.select property
	}

	/**@return This rule's apply declaration, or <code>null</code> if this rule has no <code>theme.apply</code> selector or the value is not a {@link Template}.*/
	public Template getApply()
	{
		return asInstance(getPropertyValue(APPLY_PROPERTY_URI), Template.class);	//return the theme.apply value if it is a Template
	}

	/**Sets this rule's apply declaration.
	@param apply This rule's apply declaration, or <code>null</code> if this rule should have no apply declaration.
	*/
	public void setApply(final Template apply)
	{
		setPropertyValue(APPLY_PROPERTY_URI, apply);	//set the theme.apply property
	}

	/**Applies this rule to the given object.
	Providing a PLOOP processor allows consistency of referenced values across rule applications.
	@param object The object on which this rule should be applied.
	@param ploopProcessor The PLOOP processor for setting object properties.
	@return <code>true</code> if the rule selected the given object.
	@exception NullPointerException if the given object and/or PLOOP processor is <code>null</code>.
 	@exception DataException if a resource is a Java-typed resource the class of which cannot be found.
	@exception DataException if a particular value is not an appropriate argument for the corresponding property.
	@exception DataException If a particular property could not be accessed.
	@exception InvocationTargetException if a resource indicates a Java class the constructor of which throws an exception.
	*/
	public boolean apply(final Object object, final PLOOPURFProcessor ploopProcessor) throws DataException, InvocationTargetException
	{
		final Selector selector=getSelector();	//get the selector, if any
		if(selector!=null && selector.selects(object))	//if this selector selects the object
		{
			final Template template=getApply();	//get the template to apply, if any
			if(template!=null)	//if there is a template
			{
				template.apply(object, ploopProcessor);	//apply the template to the object
			}
			return true;	//indicate that the rule applied to the object
		}
		return false;	//indicate that the rule didn't apply to the object
	}


}