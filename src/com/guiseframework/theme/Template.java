package com.guiseframework.theme;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import com.globalmentor.urf.AbstractClassTypedURFResource;
import com.globalmentor.urf.ploop.PLOOPURFProcessor;
import com.globalmentor.util.DataException;

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
	@exception NullPointerException if the given object and/or PLOOP processor is <code>null</code>.
 	@exception DataException if a resource is a Java-typed resource the class of which cannot be found.
	@exception DataException if a particular value is not an appropriate argument for the corresponding property.
	@exception DataException If a particular property could not be accessed.
	@exception InvocationTargetException if a resource indicates a Java class the constructor of which throws an exception.
	*/
	public void apply(final Object object, final PLOOPURFProcessor ploopProcessor) throws DataException, InvocationTargetException
	{
		ploopProcessor.setObjectProperties(object, this, TEMPLATE_CLASS_URI);	//initialize the object from the template, using the URI of the template class as the namespace URI for finding properties
	}

}