package com.javaguise.test;

import java.net.URI;
import java.util.Locale;

import com.garretwilson.beans.AbstractPropertyValueChangeListener;
import com.garretwilson.beans.PropertyValueChangeEvent;
import com.javaguise.component.*;
import com.javaguise.component.layout.*;
import com.javaguise.demo.DemoUser;
import com.javaguise.event.*;
import com.javaguise.model.*;
import com.javaguise.session.GuiseSession;
import com.javaguise.validator.RegularExpressionStringValidator;
import com.javaguise.validator.ValidationException;
import com.javaguise.validator.ValueRequiredValidator;
import com.garretwilson.util.Debug;

/**Test panel for a home page.
@author Garret Wilson
*/
public class HomePanel extends DefaultNavigationPanel
{

	private TestFrame frame=null;

	/**Guise session constructor.
	@param session The Guise session that owns this panel.
	*/
	public HomePanel(final GuiseSession session)
	{
		this(session, null);	//construct the component, indicating that a default ID should be used
	}

	/**ID constructor.
	@param session The Guise session that owns this panel.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	*/
	public HomePanel(final GuiseSession session, final String id)
	{
		super(session, id, new RegionLayout(session));	//construct the parent using a region layout
		getModel().setLabel("Home Panel Test");	//set the panel label

		final LayoutPanel contentPanel=new LayoutPanel(session, new FlowLayout(session, Orientation.Flow.PAGE)); 

		//input panel
		final LayoutPanel inputPanel=new LayoutPanel(session, new FlowLayout(session, Orientation.Flow.PAGE));	//create the input panel flowing vertically
		final TextControl<Float> inputTextControl=new TextControl<Float>(session, Float.class);	//create a text input control to receive a float
		inputTextControl.getModel().setLabel("Input Number");	//add a label to the text input control
		inputTextControl.getModel().setValidator(new ValueRequiredValidator<Float>(session));	//install a validator requiring a value
		inputPanel.add(inputTextControl);	//add the input control to the input panel
		final TextControl<Float> outputTextControl=new TextControl<Float>(session, Float.class);	//create a text input control to display the result
		outputTextControl.getModel().setLabel("Double the Number");	//add a label to the text output control
		outputTextControl.getModel().setEditable(false);	//set the text output control to read-only so that the user cannot modify it
		inputPanel.add(outputTextControl);	//add the output control to the input panel
		inputTextControl.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractPropertyValueChangeListener<Float>()
				{
					public void propertyValueChange(final PropertyValueChangeEvent<Float> propertyValueChangeEvent)
					{
						final Float newValue=propertyValueChangeEvent.getNewValue();	//get the new value
						try
						{
							outputTextControl.getModel().setValue(newValue*2);	//update the value
						}
						catch(final ValidationException validationException)	//we have no validator installed in the check control model, so we don't expect changing its value ever to cause any problems
						{
							throw new AssertionError(validationException);
						}							
					}
				});
		final CheckControl checkbox=new CheckControl(session, "checkbox");
		checkbox.getModel().setLabel("Enable the button");
		try
		{
			checkbox.getModel().setValue(Boolean.TRUE);
		}
		catch(final ValidationException validationException)	//we have no validator installed in the check control model, so we don't expect changing its value ever to cause any problems
		{
			throw new AssertionError(validationException);
		}										
		inputPanel.add(checkbox);
		
		final ListControl<Float> listControl=new ListControl<Float>(session, Float.class, new SingleListSelectionStrategy<Float>());	//create a list control allowing only single selections
		listControl.getModel().setLabel("Pick a Number");	//set the list control label
		listControl.setRowCount(5);
		listControl.getModel().add(new Float(10));
		listControl.getModel().add(new Float(20));
		listControl.getModel().add(new Float(30));
		listControl.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractPropertyValueChangeListener<Float>()
				{
					public void propertyValueChange(final PropertyValueChangeEvent<Float> propertyValueChangeEvent)
					{
						final Float newValue=propertyValueChangeEvent.getNewValue();	//get the new value
						try
						{
Debug.trace("list control changed value to", newValue);
							outputTextControl.getModel().setValue(newValue!=null ? newValue*2 : null);	//update the value
						}
						catch(final ValidationException validationException)	//we have no validator installed in the check control model, so we don't expect changing its value ever to cause any problems
						{
							throw new AssertionError(validationException);
						}							
					}
				});
		inputPanel.add(listControl);


		contentPanel.add(inputPanel);	//add the input panel to the temperature panel
		
		
		final Label testLabel=new Label(session, "testLabel");
		testLabel.setDragEnabled(true);
		testLabel.setStyleID("title");
		testLabel.getModel().setLabel("This is label text from the model.");
		contentPanel.add(testLabel);	//add a new label
		
		final LayoutPanel buttonPanel=new LayoutPanel(session, "testButtonPanel", new FlowLayout(session, Orientation.Flow.LINE));	//create a panel flowing horizontally

		final Button testButton=new Button(session, "testButton");
		testButton.getModel().setLabel("Click here to go to the 'Hello World' demo.");
		testButton.getModel().addActionListener(new NavigateActionListener<ActionModel>("helloworld"));
		buttonPanel.add(testButton);	//add a new button
		
		
		checkbox.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractPropertyValueChangeListener<Boolean>()
				{
					public void propertyValueChange(final PropertyValueChangeEvent<Boolean> propertyValueChangeEvent)
					{
						final Boolean newValue=propertyValueChangeEvent.getNewValue();	//get the new value
						testButton.getModel().setEnabled(newValue);	//update the button enabled state
					}
				});
		
		
		
		final Button testButton2=new Button(session, "testButton2");
		testButton2.getModel().setLabel("Click this button to change the text.");
		testButton2.getModel().addActionListener(new ActionListener<ActionModel>()
				{
					public void actionPerformed(ActionEvent<ActionModel> actionEvent)
					{
						testLabel.getModel().setLabel("You pressed the button!");
					}
				});
		buttonPanel.add(testButton2);	//add a new button
		final Link testLink=new Link(session);
		testLink.getModel().setLabel("This is a link.");
		testLink.getModel().addActionListener(new ActionListener<ActionModel>()
				{
					public void actionPerformed(ActionEvent<ActionModel> actionEvent)
					{
						testLabel.getModel().setLabel("The link works.");
					}
				});
		buttonPanel.add(testLink);	//add a new button
		final Link modalLink=new Link(session);
		modalLink.getModel().setLabel("Test modal.");
		modalLink.getModel().addActionListener(new ActionListener<ActionModel>()
				{
					public void actionPerformed(ActionEvent<ActionModel> actionEvent)
					{
						getSession().navigateModal("edituser", new ModalAdapter<Object>()
								{
									/**Called when an a modal panel ends its modality.
									@param modalEvent The event indicating the panel ending modality and the modal value.
									*/
									public void modalEnded(final ModalEvent<Object> modalEvent)
									{
										
									}
								}
						
						);
					}
				});
		buttonPanel.add(modalLink);	//add a new button
		
		final Link helloLink=new Link(session);
		helloLink.getModel().setLabel("More Hello World.");
		helloLink.getModel().addActionListener(new NavigateActionListener<ActionModel>("helloworld"));
		buttonPanel.add(helloLink);	//add the link

		final Link frameLink=new Link(session);
		frameLink.getModel().setLabel("Frame");
		frameLink.getModel().addActionListener(new ActionListener<ActionModel>()
				{
					public void actionPerformed(ActionEvent<ActionModel> actionEvent)
					{
						if(frame==null)
						{
							frame=new TestFrame(session);
							frame.getModel().setLabel("Test Frame");
						}
	Debug.trace("ready to set frame visible");
						frame.setVisible(true);
					}
				});
		buttonPanel.add(frameLink);

		contentPanel.add(buttonPanel);	//add the button panel to the panel
		final TextControl<String> textInput=new TextControl<String>(session, "textInput", String.class);	//create a text input control
		textInput.getModel().setLabel("This is the text input label.");
		textInput.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractPropertyValueChangeListener<String>()
				{
					public void propertyValueChange(PropertyValueChangeEvent<String> propertyValueChangeEvent)
					{
						testLabel.getModel().setLabel(propertyValueChangeEvent.getNewValue());
						if(frame!=null)
						{
							frame.label.getModel().setLabel(propertyValueChangeEvent.getNewValue());
							frame.getModel().setLabel("Updated frame.");
						}
					}
				});
//TODO del		textInput.getModel().setValidator(new RegularExpressionStringValidator("[a-z]*"));
		contentPanel.add(textInput);
	
	
	
		final LayoutPanel horizontalPanel=new LayoutPanel(session, new FlowLayout(session, Orientation.Flow.LINE));	//create a panel flowing horizontally
	
	
		final GroupPanel booleanPanel=new GroupPanel(session, new FlowLayout(session, Orientation.Flow.PAGE));	//create a panel flowing vertically
		booleanPanel.setDragEnabled(true);
		booleanPanel.getModel().setLabel("Check one of these");
		final CheckControl check1=new CheckControl(session, "check1");
		check1.setCheckType(CheckControl.CheckType.ELLIPSE);
		check1.getModel().setLabel("First check");
		booleanPanel.add(check1);	
		final CheckControl check2=new CheckControl(session, "check2");	
		check2.setCheckType(CheckControl.CheckType.ELLIPSE);
		check2.getModel().setLabel("Second check");
		check2.getModel().setEnabled(false);	//TODO fix
		booleanPanel.add(check2);	
		final ModelGroup<ValueModel<Boolean>> booleanGroup=new MutualExclusionPolicyModelGroup();
		booleanGroup.add(check1.getModel());
		booleanGroup.add(check2.getModel());
	
		horizontalPanel.add(booleanPanel);

		final Button testButtona=new Button(session, "testButton");
		testButtona.getModel().setLabel("Nuther button.");
		testButtona.setDragEnabled(true);
		horizontalPanel.add(testButtona);	//add a new button
/*TODO fix		
		final Panel booleanPanela=new Panel(session, new FlowLayout(Axis.Y));	//create a panel flowing vertically
		booleanPanela.getModel().setLabel("Check one of these");
		final CheckControl check1a=new CheckControl(session, "check1");
		check1a.setCheckType(CheckControl.CheckType.ELLIPSE);
		check1a.getModel().setLabel("First check");
		booleanPanela.add(check1a);	
		final CheckControl check2a=new CheckControl(session, "check2");	
		check2a.setCheckType(CheckControl.CheckType.ELLIPSE);
		check2a.getModel().setLabel("Second check");
		booleanPanela.add(check2a);	
		final ModelGroup<ValueModel<Boolean>> booleanGroupa=new MutualExclusionModelGroup();
		booleanGroupa.add(check1a.getModel());
		booleanGroupa.add(check2a.getModel());

		horizontalPanel.add(booleanPanela);
*/
		
		final Image image=new Image(session);
		image.getModel().setImage(URI.create("http://www.garretwilson.com/photos/2000/february/cowcalf.jpg"));
/*TODO fix
		image.getModel().setLabel("Cow and Calf");
		image.getModel().setMessage("A cow and her minutes-old calf.");
*/
		image.getModel().setLabel("\u0622\u067E");
		image.getModel().setMessage("\u0628\u0627\u062A");
		image.setDragEnabled(true);
		horizontalPanel.add(image);
		
		
		contentPanel.add(horizontalPanel);
		
/*TODO del		
		final Heading resourceHeading=new Heading(session, 2);
		resourceHeading.getModel().setLabelResourceKey("test.resource");
		add(resourceHeading);
*/

		final Label afterImageLabel=new Label(session);
		afterImageLabel.getModel().setLabel("This is a lot of text. ;alsjfd ;lkjas ;ljag ;lkjas g;lkajg; laksgj akjlshf lkjashd flkjsdhlksahlsadkhj asldkhjf ;sgdh a;lgkh a;glkha s;dglh asgd;");
		contentPanel.add(afterImageLabel);

		final ListControl<String> listSelectControl=new ListControl<String>(session, String.class, new SingleListSelectionStrategy<String>());
		listSelectControl.getModel().setLabel("Choose an option.");
		listSelectControl.getModel().add("The first option");
		listSelectControl.getModel().add(null);
		listSelectControl.getModel().add("The second option");
		listSelectControl.getModel().add("The third option");
		listSelectControl.getModel().add("The fourth option");
		contentPanel.add(listSelectControl);

		final TextAreaControl textAreaControl=new TextAreaControl(session, 25, 100, true);
		textAreaControl.getModel().setLabel("Type some text.");
		try
		{
			textAreaControl.getModel().setValue("This is some text\nand some more on another line.\n\nSkipping two lines down, we find a line that is really long, is really, really, ;lkjas;lfk alkg; ;alkghj;alg lkjahq glkjh flkjhasdflkjhasdfl kjhasdf lkjh lkadhf lkshd flksadhf lksadhlskdqah slhjfg sd long.");
		}
		catch (ValidationException e)
		{
			throw new AssertionError(e);
		}
		contentPanel.add(textAreaControl);
/*TODO del
		final Text text=new Text(session);
		//TODO del text.getModel().setText("This is some cool text! This is some text\nand some more on another line.\n\nSkipping two lines down, we find a line that is really long, is really, really, ;lkjas;lfk alkg; ;alkghj;alg lkjahq glkjh flkjhasdflkjhasdfl kjhasdf lkjh lkadhf lkshd flksadhf lksadhlskdqah slhjfg sd long. This is some text\nand some more on another line.\n\nSkipping two lines down, we find a line that is really long, is really, really, ;lkjas;lfk alkg; ;alkghj;alg lkjahq glkjh flkjhasdflkjhasdfl kjhasdf lkjh lkadhf lkshd flksadhf lksadhlskdqah slhjfg sd long.");
		text.getModel().setTextResourceKey("sample.html");
		text.getModel().setTextContentType(TextModel.XHTML_CONTENT_TYPE);
		contentPanel.add(text);
*/

		getSession().setLocale(Locale.FRANCE);
//TODO del		getSession().setLocale(new Locale("ar"));

		final Integer[][] multiplicationTableData=new Integer[2][2];
		for(int row=0; row<2; ++row)
		{
			for(int column=0; column<2; ++column)
			{
				multiplicationTableData[row][column]=new Integer(row*column);
			}
		}
		final Table multiplicationTable=new Table(session, Integer.class, multiplicationTableData, "0", "1");
		multiplicationTable.getModel().setLabel("Multiplication Table");
		for(final TableColumnModel<?> column:multiplicationTable.getModel().getColumns())
		{
			column.setEditable(true);
		}
		contentPanel.add(multiplicationTable);

		final TreeControl treeControl=new TreeControl(session);
		final TreeNodeModel<String> firstItem=new DefaultTreeNodeModel<String>(session, String.class, "First Item");
		firstItem.add(new DefaultTreeNodeModel<String>(session, String.class, "Sub Item A"));
		firstItem.add(new DefaultTreeNodeModel<String>(session, String.class, "Sub Item B"));
		treeControl.getModel().getRootNode().add(firstItem);
		treeControl.getModel().getRootNode().add(new DefaultTreeNodeModel<String>(session, String.class, "Second Item"));
		treeControl.getModel().getRootNode().add(new DefaultTreeNodeModel<String>(session, String.class, "Third Item"));

		contentPanel.add(treeControl);

		final TabbedPanel tabbedPanel=new TabbedPanel(session);
		//input panel
		final LayoutPanel temperaturePanel=new LayoutPanel(session, new FlowLayout(session, Orientation.Flow.PAGE));	//create the input panel flowing vertically
		final TextControl<Float> temperatureInput=new TextControl<Float>(session, Float.class);	//create a text input control to receive a float
		temperatureInput.getModel().setLabel("Input Temperature");	//add a label to the text input control
		temperatureInput.getModel().setValidator(new ValueRequiredValidator<Float>(session));	//install a validator requiring a value
		temperaturePanel.add(temperatureInput);	//add the input control to the input panel
		final TextControl<Float> temperatureOutput=new TextControl<Float>(session, Float.class);	//create a text input control to display the result
		temperatureOutput.getModel().setLabel("Output Temperature");	//add a label to the text output control
		temperatureOutput.getModel().setEditable(false);	//set the text output control to read-only so that the user cannot modify it
		temperaturePanel.add(temperatureOutput);	//add the output control to the input panel
		tabbedPanel.add(temperaturePanel, new CardLayout.Constraints(new DefaultLabelModel(session, "Temperature")));
	
		final LayoutPanel helloPanel=new LayoutPanel(session);
		final Heading helloWorldHeading=new Heading(session, 0);	//create a top-level heading
		helloWorldHeading.getModel().setLabel("Hello World!");	//set the text of the heading, using its model
		helloPanel.add(helloWorldHeading);
		tabbedPanel.add(helloPanel, new CardLayout.Constraints(new DefaultLabelModel(session, "Hello")));
		
		contentPanel.add(tabbedPanel);


		add(contentPanel, RegionLayout.CENTER_CONSTRAINTS);	//add the content panel in the center

		add(createMenu(session, Orientation.Flow.LINE), RegionLayout.PAGE_START_CONSTRAINTS);	//add the pulldown menu at the top

		add(createMenu(session, Orientation.Flow.PAGE), RegionLayout.LINE_START_CONSTRAINTS);	//add the menu at the left

	}

	protected Menu createMenu(final GuiseSession session, final Orientation.Flow flow)
	{
		final Menu menu=new Menu(session, flow);

		final Menu fileMenu=new Menu(session, "fileMenu", Orientation.Flow.PAGE);
		fileMenu.getModel().setLabel("File");
		final Link openMenuLink=new Link(session, "openMenuItem");
		openMenuLink.getModel().setLabel("Open");
		fileMenu.add(openMenuLink);
		final Link closeMenuLink=new Link(session, "closeMenuItem");
		closeMenuLink.getModel().setLabel("Close");
		fileMenu.add(closeMenuLink);
		menu.add(fileMenu);

		final Menu editMenu=new Menu(session, "editMenu", Orientation.Flow.PAGE);
		editMenu.getModel().setLabel("Edit");
		final Link copyMenuLink=new Link(session, "copyMenuItem");
		copyMenuLink.getModel().setLabel("Copy");
		editMenu.add(copyMenuLink);
		final Link cutMenuLink=new Link(session, "cutMenuItem");
		cutMenuLink.getModel().setLabel("Cut");
		editMenu.add(cutMenuLink);
		final Link pasteMenuLink=new Link(session, "pasteMenuItem");
		pasteMenuLink.getModel().setLabel("Paste");
		editMenu.add(pasteMenuLink);
		menu.add(editMenu);

		final Menu windowMenu=new Menu(session, "windowMenu", Orientation.Flow.PAGE);
		windowMenu.getModel().setLabel("Window");

		final Menu arrangeMenu=new Menu(session, "arrangeMenu", Orientation.Flow.PAGE);
		arrangeMenu.getModel().setLabel("Arrange");
		
		final Link tileMenuLink=new Link(session, "tileMenuItem");
		tileMenuLink.getModel().setLabel("Tile");
		arrangeMenu.add(tileMenuLink);
		final Link cascadeMenuLink=new Link(session, "cascadeMenuItem");
		cascadeMenuLink.getModel().setLabel("Cascade");
		arrangeMenu.add(cascadeMenuLink);
		windowMenu.add(arrangeMenu);
		menu.add(windowMenu);

			//GlobalMentor
		final Link globalmentorLink=new Link(session);
		globalmentorLink.getModel().setLabel("GlobalMentor");
		globalmentorLink.getModel().addActionListener(new NavigateActionListener<ActionModel>(URI.create("http://www.globalmentor.com/")));
		menu.add(globalmentorLink);
		
		return menu;
	}

	
	protected static class TestFrame extends DefaultFrame
	{
		protected final Label label;
		
		public TestFrame(final GuiseSession session)
		{
			super(session);
			label=new Label(session);
			label.getModel().setLabel("This is frame content");
			setComponent(label);
			
		}
	}
}
