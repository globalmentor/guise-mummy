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

package io.guise.framework.theme;

import static io.guise.framework.theme.Theme.*;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.ploop.graph.PLOOPURFProcessor;
import org.urframework.AbstractClassTypedURFResource;

import com.globalmentor.util.DataException;

/**
 * An application template of a rule.
 * @author Garret Wilson
 */
public class Template extends AbstractClassTypedURFResource {

	/** Default constructor. */
	public Template() {
		this(null); //construct the class with no reference URI
	}

	/**
	 * Reference URI constructor.
	 * @param referenceURI The reference URI for the new resource.
	 */
	public Template(final URI referenceURI) {
		super(referenceURI, THEME_NAMESPACE_URI); //construct the parent class
	}

	/**
	 * Applies this template to a given object. Providing a PLOOP processor allows consistency of referenced values across template applications.
	 * @param object The object to which this template will be applied.
	 * @param ploopProcessor The PLOOP processor for setting object properties.
	 * @throws NullPointerException if the given object and/or PLOOP processor is <code>null</code>.
	 * @throws DataException if a resource is a Java-typed resource the class of which cannot be found.
	 * @throws DataException if a particular value is not an appropriate argument for the corresponding property.
	 * @throws DataException If a particular property could not be accessed.
	 * @throws InvocationTargetException if a resource indicates a Java class the constructor of which throws an exception.
	 */
	public void apply(final Object object, final PLOOPURFProcessor ploopProcessor) throws DataException, InvocationTargetException {
		ploopProcessor.setObjectProperties(object, this); //initialize the object from the template
	}

}