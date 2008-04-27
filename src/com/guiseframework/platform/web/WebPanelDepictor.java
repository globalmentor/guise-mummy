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

import com.guiseframework.component.*;

/**Strategy for rendering a panel as an XHTML <code>&lt;div&gt;</code> element.
Changes to {@link Component#LABEL_PROPERTY} are ignored.
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public class WebPanelDepictor<C extends Panel> extends WebLayoutComponentDepictor<C>
{

	/**Default constructor.*/
	public WebPanelDepictor()
	{
		getIgnoredProperties().add(Panel.LABEL_PROPERTY);	//ignore Panel.label by default, because panels are large objects with many children but most do not show labels
	}

}
