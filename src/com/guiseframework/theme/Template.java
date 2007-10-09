package com.guiseframework.theme;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import com.garretwilson.urf.AbstractClassTypedURFResource;
import com.garretwilson.urf.ploop.PLOOPURFProcessor;

import static com.guiseframework.theme.Theme.*;

/**An application template of a rule.
@author Garret Wilson
*/
public class Template extends AbstractClassTypedURFResource
{

	/**Default constructor.*/
	public Template()
	{
		this(null);	//construct the class with no reference URI
	}

	/**Reference URI constructor.
	@param referenceURI The reference URI for the new resource.
	*/
	public Template(final URI referenceURI)
	{
		super(referenceURI, THEME_NAMESPACE_URI);  //construct the parent class
	}

	/**Applies this template to a given object.
	Providing a PLOOP processor allows consistency of referenced values across template applications.
	@param object The object to which this template will be applied.
	@param ploopProcessor The PLOOP processor for setting object properties.
	@exception ClassNotFoundException if a class was specified and the indicated class cannot be found.
	@exception InvocationTargetException if the given RDF object indicates a Java class the constructor of which throws an exception.
	*/
	public void apply(final Object object, final PLOOPURFProcessor ploopProcessor) throws ClassNotFoundException, InvocationTargetException
	{
		ploopProcessor.setObjectProperties(object, this, THEME_NAMESPACE_URI);	//initialize the object from the template, using the theme namespace to find the properties
	}

}