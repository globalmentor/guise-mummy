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

package io.guise.framework.component.urf;

import java.beans.*;

import org.urframework.*;

import io.guise.framework.component.*;

/**
 * A component for specifying alteration to be performed on a resource. Panels containing resource property information typically implement this interface, for
 * example.
 * @author Garret Wilson
 */
public interface URFResourceAlterationComponent extends Component {

	/**
	 * Retrieves the specification for modifying a resource based upon the contents of the component.
	 * @return The specification of alterations to be performed on a resource according to the properties edited in the component.
	 */
	public URFResourceAlteration getResourceAlteration();

	/**
	 * Sets the resource information displayed in the component.
	 * @param resource The resource containing the URI and properties to set in the component.
	 * @throws NullPointerException if the given resource is <code>null</code>.
	 * @throws PropertyVetoException if there was an error setting the information in the component.
	 */
	public void setResource(final URFResource resource) throws PropertyVetoException;
}
