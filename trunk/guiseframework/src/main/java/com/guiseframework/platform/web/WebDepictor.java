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

package com.guiseframework.platform.web;

import com.guiseframework.platform.*;

/**A strategy for depicting objects on the web platform.
@param <O> The type of object being depicted.
@author Garret Wilson
*/
public interface WebDepictor<O extends DepictedObject> extends Depictor<O>
{

	/**@return The web platform on which this depictor is depicting ojects.*/
	public WebPlatform getPlatform();

	/**Retrieves information and functionality related to the current depiction on the web platform.
	This method delegates to {@link WebPlatform#getDepictContext()}.
	@return A context for the current depiction.
	@exception IllegalStateException if no depict context can be returned in the current depiction state.
	*/
	public WebDepictContext getDepictContext();
}
