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

package com.guiseframework.component.layout;

import com.guiseframework.model.*;

/**
 * A layout that manages child components as a series of tabs. Only one child component is visible at a time. The tab layout maintains its own value model that
 * maintains the current selected component. If a tab implements {@link Activeable} the tab is set as active when selected and set as inactive when the tab is
 * unselected.
 * @author Garret Wilson
 */
public class TabLayout extends AbstractValueLayout<ControlConstraints> {

	@Override
	public Class<? extends ControlConstraints> getConstraintsClass() {
		return ControlConstraints.class;
	}

	@Override
	public ControlConstraints createDefaultConstraints() {
		return new ControlConstraints(); //create constraints
	}

}
