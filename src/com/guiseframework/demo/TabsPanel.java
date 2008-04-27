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

package com.guiseframework.demo;

import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;

/**Tabs Guise demonstration panel.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates tabbed panels, tab controls, and card panels. 
@author Garret Wilson
*/
public class TabsPanel extends LayoutPanel
{

	/**Default constructor.*/
	public TabsPanel()
	{
		super(new FlowLayout(Flow.PAGE));	//construct the parent class flowing vertically
		setLabel("Guise\u2122 Demonstration: Tabs");	//set the panel title	

			//TabbedPanel and CardTabControl demo
		final GroupPanel tabbedPanelPanel=new GroupPanel(new FlowLayout(Flow.PAGE));	//create a panel flowing vertically
		tabbedPanelPanel.setLabel("TabbedPanel with associated CardTabControl");
				//TabbedPanel
		final TabbedPanel tabbedPanel=new TabbedPanel();	//create a tabbed panel
					//page 1
		final Panel tabbedPanelPage1=new LayoutPanel();	//create a panel to serve as the page
		final Heading tabbedPanelPage1Heading=new Heading(0);	//create a top-level heading
		tabbedPanelPage1Heading.setLabel("This is page 1.");	//set the text of the heading
		tabbedPanelPage1.add(tabbedPanelPage1Heading);	//add the heading to the page
		tabbedPanel.add(tabbedPanelPage1, new CardConstraints("Page 1"));	//add the panel with a label
					//page 2
		final Panel tabbedPanelPage2=new LayoutPanel();	//create a panel to serve as the page
		final Heading tabbedPanelPage2Heading=new Heading(0);	//create a top-level heading
		tabbedPanelPage2Heading.setLabel("This is page 2.");	//set the text of the heading
		tabbedPanelPage2.add(tabbedPanelPage2Heading);	//add the heading to the page
		tabbedPanel.add(tabbedPanelPage2, new CardConstraints("Page 2"));	//add the panel with a label
		tabbedPanelPanel.add(tabbedPanel);
				//CardTabControl
		final CardTabControl tabbedPanelTabControl=new CardTabControl(tabbedPanel, Flow.LINE);	//create a horizontal card tab control to control the existing tab panel
		tabbedPanelPanel.add(tabbedPanelTabControl);
		add(tabbedPanelPanel);

			//CardPanel and CardTabControl demo
		final GroupPanel cardPanelPanel=new GroupPanel(new FlowLayout(Flow.PAGE));	//create a panel flowing vertically
		cardPanelPanel.setLabel("CardTabControl associated with CardPanel");
				//CardPanel
		final CardPanel cardPanel=new CardPanel();	//create a card panel
					//page 1
		final Panel cardPanelPage1=new LayoutPanel();	//create a panel to serve as the page
		final Heading cardPanelPage1Heading=new Heading(0);	//create a top-level heading
		cardPanelPage1Heading.setLabel("This is page 1.");	//set the text of the heading
		cardPanelPage1.add(cardPanelPage1Heading);	//add the heading to the page
		cardPanel.add(cardPanelPage1, new CardConstraints("Page 1"));	//add the panel with a label
					//page 2
		final Panel cardPanelPage2=new LayoutPanel();	//create a panel to serve as the page
		final Heading cardPanelPage2Heading=new Heading(0);	//create a top-level heading
		cardPanelPage2Heading.setLabel("This is page 2.");	//set the text of the heading
		cardPanelPage2.add(cardPanelPage2Heading);	//add the heading to the page
		cardPanel.add(cardPanelPage2, new CardConstraints("Page 2"));	//add the panel with a label
				//CardTabControl
		final CardTabControl cardPanelTabControl=new CardTabControl(cardPanel, Flow.LINE);	//create a horizontal card tab control to control the existing card panel
		cardPanelPanel.add(cardPanelTabControl);	//place the tab control above the card panel to illustrate common usage
		cardPanelPanel.add(cardPanel);
		add(cardPanelPanel);

			//TabControl demo
		final GroupPanel tabControlPanel=new GroupPanel(new FlowLayout(Flow.PAGE));	//create a panel flowing vertically
		tabControlPanel.setLabel("Standalone TabControl.");
				//TabControl
		final TabControl<Integer> tabControl=new TabControl<Integer>(Integer.class, Flow.LINE);	//create a horizontal
		tabControl.add(new Integer(5));
		tabControl.add(new Integer(10));
		tabControl.add(new Integer(15));
		tabControlPanel.add(tabControl);
		add(tabControlPanel);
	}

}
