package com.guiseframework.demo;

import java.beans.PropertyVetoException;

import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.converter.*;
import com.guiseframework.converter.NumberStringLiteralConverter.Style;
import com.guiseframework.event.*;

/**Diagnostic Guise demonstration panel.
Copyright © 2006 GlobalMentor, Inc.
Demonstrates
@author Garret Wilson
*/
public class DiagnosticPanel extends DefaultNavigationPanel
{

	final TextControl<Long> maxMemoryTextControl;
	final TextControl<Long> totalMemoryTextControl;
	final TextControl<Double> totalPercentMemoryTextControl;
	final TextControl<Long> usedMemoryTextControl;
	final TextControl<Long> freeMemoryTextControl;
	final TextControl<Double> percentMemoryTextControl;

	/**Default constructor.*/
	public DiagnosticPanel()
	{
		setLabel("Guise\u2122 Demonstration: Diagnostic");	//set the panel title

		final Panel memoryPanel=new GroupPanel(new FlowLayout(Flow.LINE));
		memoryPanel.setLabel("Memory");

		maxMemoryTextControl=new TextControl<Long>(Long.class);
		maxMemoryTextControl.setLabel("Maximum");
		maxMemoryTextControl.setEditable(false);
		memoryPanel.add(maxMemoryTextControl);

		totalMemoryTextControl=new TextControl<Long>(Long.class);
		totalMemoryTextControl.setLabel("Total");
		totalMemoryTextControl.setEditable(false);
		memoryPanel.add(totalMemoryTextControl);

		totalPercentMemoryTextControl=new TextControl<Double>(Double.class);
		totalPercentMemoryTextControl.setLabel("Total Percent");
		totalPercentMemoryTextControl.setConverter(new DoubleStringLiteralConverter(Style.PERCENT));
		totalPercentMemoryTextControl.setEditable(false);
		memoryPanel.add(totalPercentMemoryTextControl);

		usedMemoryTextControl=new TextControl<Long>(Long.class);
		usedMemoryTextControl.setLabel("Used");
		usedMemoryTextControl.setEditable(false);
		memoryPanel.add(usedMemoryTextControl);

		freeMemoryTextControl=new TextControl<Long>(Long.class);
		freeMemoryTextControl.setLabel("Free");
		freeMemoryTextControl.setEditable(false);
		memoryPanel.add(freeMemoryTextControl);

		percentMemoryTextControl=new TextControl<Double>(Double.class);
		percentMemoryTextControl.setLabel("Percent Used");
		percentMemoryTextControl.setConverter(new DoubleStringLiteralConverter(Style.PERCENT));
		percentMemoryTextControl.setEditable(false);
		memoryPanel.add(percentMemoryTextControl);

		add(memoryPanel);

		final Button diagnoseButton=new Button();
		diagnoseButton.setLabel("Update");
		diagnoseButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(final ActionEvent actionEvent)
					{
						diagnose();	//update the diagnostics						
					}
				});
		add(diagnoseButton);
		
		diagnose();	//update the diagnostics
	}

	/**Updates diagnostic labels.*/
	protected void diagnose()
	{
		try
		{
			final Runtime runtime=Runtime.getRuntime();	//get the runtime instance
			final long maxMemory=runtime.maxMemory();
			final long totalMemory=runtime.totalMemory();
			final long freeMemory=runtime.freeMemory();
			final long usedMemory=totalMemory-freeMemory;
			maxMemoryTextControl.setValue(maxMemory);
			totalMemoryTextControl.setValue(totalMemory);
			totalPercentMemoryTextControl.setValue((double)totalMemory/maxMemory);
			freeMemoryTextControl.setValue(freeMemory);
			usedMemoryTextControl.setValue(usedMemory);
			percentMemoryTextControl.setValue((double)usedMemory/totalMemory);
		}
		catch(final PropertyVetoException propertyVetoException)
		{
			throw new AssertionError(propertyVetoException);
		}
	}
}
