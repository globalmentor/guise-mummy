package com.garretwilson.guise.test;

import java.net.URI;

import com.garretwilson.beans.AbstractPropertyValueChangeListener;
import com.garretwilson.beans.PropertyValueChangeEvent;
import com.garretwilson.guise.component.*;
import com.garretwilson.guise.component.layout.*;
import com.garretwilson.guise.event.ActionEvent;
import com.garretwilson.guise.event.ActionListener;
import com.garretwilson.guise.event.NavigateActionListener;
import com.garretwilson.guise.model.AbstractModelGroup;
import com.garretwilson.guise.model.ActionModel;
import com.garretwilson.guise.model.ModelGroup;
import com.garretwilson.guise.model.MutualExclusionModelGroup;
import com.garretwilson.guise.model.ValueModel;
import com.garretwilson.guise.session.GuiseSession;
import com.garretwilson.guise.validator.RegularExpressionStringValidator;
import com.garretwilson.util.Debug;

/**Test frame for a home page.
@author Garret Wilson
*/
public class HomeFrame extends NavigationFrame
{

	/**Guise session constructor.
	@param session The Guise session that owns this frame.
	*/
	public HomeFrame(final GuiseSession<?> session)
	{
		this(session, null);	//construct the component, indicating that a default ID should be used
	}

	/**ID constructor.
	@param session The Guise session that owns this frame.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	*/
	public HomeFrame(final GuiseSession<?> session, final String id)
	{
		super(session, id, new FlowLayout(Axis.Y));	//construct the parent class, flowing verticallly
		setTitle("Home Frame Test");	//set the frame label
		
		final Label testLabel=new Label(session, "testLabel");
		testLabel.setStyleID("title");
		testLabel.getModel().setText("This is label text from the model.");
		add(testLabel);	//add a new label
		
		final Panel buttonPanel=new Panel(session, "testButtonPanel", new FlowLayout(Axis.X));	//create a panel flowing horizontally

		final ActionControl testButton=new ActionControl(session, "testButton");
		testButton.getModel().setText("Click here to go to the 'Hello World' demo.");
		testButton.getModel().addActionListener(new NavigateActionListener<ActionModel>("helloworld"));
		buttonPanel.add(testButton);	//add a new button
		final ActionControl testButton2=new ActionControl(session, "testButton2");
		testButton2.getModel().setText("Click this button to change the text.");
		testButton2.getModel().addActionListener(new ActionListener<ActionModel>()
				{
					public void onAction(ActionEvent<ActionModel> actionEvent)
					{
						testLabel.getModel().setText("You pressed the button!");
					}
				});
		buttonPanel.add(testButton2);	//add a new button
		add(buttonPanel);	//add the button panel to the frame
		final ValueControl<String> textInput=new ValueControl<String>(session, "textInput", String.class);	//create a text input control
		textInput.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractPropertyValueChangeListener<String>()
				{
					public void propertyValueChange(PropertyValueChangeEvent<String> propertyValueChangeEvent)
					{
						testLabel.getModel().setText(propertyValueChangeEvent.getNewValue());
					}
				});
		textInput.getModel().setValidator(new RegularExpressionStringValidator("[a-z]*"));
		add(textInput);
	
	
	
	
	
		final Panel booleanPanel=new Panel(session, new FlowLayout(Axis.Y));	//create a panel flowing vertically
		final CheckControl check1=new CheckControl(session, "check1");
		check1.setCheckType(CheckControl.CheckType.ELLIPSE);
		booleanPanel.add(check1);	
		final CheckControl check2=new CheckControl(session, "check2");	
		check2.setCheckType(CheckControl.CheckType.ELLIPSE);
		booleanPanel.add(check2);	
		final ModelGroup<ValueModel<Boolean>> booleanGroup=new MutualExclusionModelGroup();
		booleanGroup.add(check1.getModel());
		booleanGroup.add(check2.getModel());
		
		add(booleanPanel);
	}

}
