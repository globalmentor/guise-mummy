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

package com.guiseframework.component;

import static com.globalmentor.java.Classes.*;

/**A component that allows its label to be displayed.
@author Garret Wilson
*/
public interface LabelDisplayableComponent extends Component
{
	/**The bound property of whether the icon is displayed or has no representation, taking up no space.*/
	public final static String ICON_DISPLAYED_PROPERTY=getPropertyName(LabelDisplayableComponent.class, "iconDisplayed");
	/**The bound property of whether the label is displayed or has no representation, taking up no space.*/
	public final static String LABEL_DISPLAYED_PROPERTY=getPropertyName(LabelDisplayableComponent.class, "labelDisplayed");

	/**@return Whether the icon is displayed.*/
	public boolean isIconDisplayed();

	/**Sets whether the icon is displayed.
	This is a bound property of type <code>Boolean</code>.
	@param newIconDisplayed <code>true</code> if the icon should be displayed, else <code>false</code> if the icon should not be displayed and take up no space.
	@see #ICON_DISPLAYED_PROPERTY
	*/
	public void setIconDisplayed(final boolean newIconDisplayed);

	/**@return Whether the label is displayed.*/
	public boolean isLabelDisplayed();

	/**Sets whether the label is displayed.
	This is a bound property of type <code>Boolean</code>.
	@param newLabelDisplayed <code>true</code> if the label should be displayed, else <code>false</code> if the label should not be displayed and take up no space.
	@see #LABEL_DISPLAYED_PROPERTY
	*/
	public void setLabelDisplayed(final boolean newLabelDisplayed);
}
