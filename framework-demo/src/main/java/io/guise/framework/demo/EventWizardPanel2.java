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

package io.guise.framework.demo;

import com.globalmentor.beans.AbstractGenericPropertyChangeListener;
import com.globalmentor.beans.GenericPropertyChangeEvent;

import io.guise.framework.component.*;

/**
 * Event wizard Guise demonstration panel using URF PLOOP. Copyright © 2006 GlobalMentor, Inc. Demonstrates sequence card panels, task card constraints, task
 * status select links, action card couplers, link select card couplers, and action prototypes.
 * @author Garret Wilson
 */
public class EventWizardPanel2 extends LayoutPanel {

	@Override
	public void initialize() {
		final SequenceCardPanel wizardCardPanel = (SequenceCardPanel)getComponentByName(this, "wizardCardPanel");
		final Panel personalAgePanel = (Panel)getComponentByName(this, "personalAgePanel");
		final CheckControl ageCheckControl = (CheckControl)getComponentByName(this, "ageCheckControl");
		ageCheckControl.addPropertyChangeListener(CheckControl.VALUE_PROPERTY, new AbstractGenericPropertyChangeListener<Boolean>() {

			@Override
			public void propertyChange(GenericPropertyChangeEvent<Boolean> propertyChangeEvent) { //if the age checkbox changes
				wizardCardPanel.setDisplayed(personalAgePanel, propertyChangeEvent.getNewValue()); //show or hide the age panel based upon the state of the age checkbox
			}

		});
		final LayoutPanel wizardButtonPanel = (LayoutPanel)getComponentByName(this, "wizardButtonPanel");
		final Button previousButton = new Button(wizardCardPanel.getPreviousActionPrototype());
		wizardButtonPanel.add(previousButton);
		final Button nextButton = new Button(wizardCardPanel.getNextActionPrototype());
		wizardButtonPanel.add(nextButton);
		final Button finishButton = new Button(wizardCardPanel.getFinishActionPrototype());
		wizardButtonPanel.add(finishButton);
		wizardCardPanel.resetSequence();
	}

}
