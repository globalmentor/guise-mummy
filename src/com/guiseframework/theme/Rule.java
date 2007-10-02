package com.guiseframework.theme;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.garretwilson.rdf.*;
import com.garretwilson.urf.ploop.PLOOPProcessor;

import static com.guiseframework.theme.Theme.*;

/**A rule for specifying part of a theme.
@author Garret Wilson
*/
public class Rule extends ClassTypedRDFResource
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

	/**@return This rule's select declaration, or <code>null</code> if this rule has no <code>theme:select</code> property.
	@exception ClassCastException if the value of the <code>theme:select</code> property is not a {@link Selector}.
	*/
	public Selector getSelect() throws ClassCastException
	{
		return (Selector)getPropertyValue(THEME_NAMESPACE_URI, SELECT_PROPERTY_NAME);	//return the theme:select value
	}

	/**@return This rule's apply declaration, or <code>null</code> if this rule has no <code>theme:apply</code> selector or the value is not of the correct type.*/
	public Template getApply()
	{
		return asInstance(getPropertyValue(THEME_NAMESPACE_URI, APPLY_PROPERTY_NAME), Template.class);	//return the theme:apply value if it is a Template
	}

	/**Applies this rule to the given object.
	Providing a PLOOP processor allows consistency of referenced values across rule applications.
	@param object The object on which this rule should be applied.
	@param ploopProcessor The PLOOP processor for setting object properties.
	@return <code>true</code> if the rule selected the given object.
	@exception ClassNotFoundException if a class was specified and the indicated class cannot be found.
	@exception InvocationTargetException if the given RDF object indicates a Java class the constructor of which throws an exception.
	*/
	public boolean apply(final Object object, final PLOOPProcessor ploopProcessor) throws ClassNotFoundException, InvocationTargetException
	{
		final Selector selector=getSelect();	//get the selector, if any
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