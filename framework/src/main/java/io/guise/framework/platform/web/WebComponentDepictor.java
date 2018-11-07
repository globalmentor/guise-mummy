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

package io.guise.framework.platform.web;

import io.guise.framework.component.Component;
import io.guise.framework.platform.ComponentDepictor;

/**
 * A strategy for depicting components on the web platform. All component depictors on the web platform must implement this interface.
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public interface WebComponentDepictor<C extends Component> extends ComponentDepictor<C>, WebDepictor<C> {

	/**
	 * Determines the identifier to place in the name attribute of the component's XHTML element, if appropriate. This is usually the string version of the ID of
	 * the component, but some grouped components may use a common name.
	 * @return An identifier appropriate for the name attribute of the component's XHTML element.
	 */
	public String getDepictName();
}
