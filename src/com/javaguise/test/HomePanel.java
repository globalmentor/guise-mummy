package com.javaguise.test;

import java.net.URI;
import java.util.Calendar;
import java.util.Locale;

import com.javaguise.component.*;
import com.javaguise.component.layout.*;
import com.javaguise.demo.DemoUser;
import com.javaguise.demo.EditUserPanel;
import com.javaguise.event.*;
import com.javaguise.geometry.Extent;
import com.javaguise.model.*;
import com.javaguise.session.GuiseSession;
import com.javaguise.style.RGBColor;
import com.javaguise.validator.IntegerRangeValidator;
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
	final Label testLabel;

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

		final LayoutPanel contentPanel=new LayoutPanel(session, new FlowLayout(session, Flow.PAGE)); 

		final CalendarMonthTableModel calendarMonthTableModel=new CalendarMonthTableModel(session);
		final Table calendarMonthTable=new Table(session, calendarMonthTableModel);
		contentPanel.add(calendarMonthTable);
		
		final CalendarControl calendarControl=new CalendarControl(session);
		contentPanel.add(calendarControl);
		
		
		calendarControl.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGuisePropertyChangeListener<CalendarMonthTableModel, Calendar>()
				{
					public void propertyChange(final GuisePropertyChangeEvent<CalendarMonthTableModel, Calendar> propertyChangeEvent)
					{
						final Calendar newValue=propertyChangeEvent.getNewValue();	//get the new value
						if(newValue!=null)
						{
							new MessageOptionDialogFrame(session,	//create a new message dialog
									"You selected date: "+newValue.getTime().toString(),
									MessageOptionDialogFrame.Option.OK).open(true);
						}
					}
				});
		
		
		//input panel
		final LayoutPanel inputPanel=new LayoutPanel(session, new FlowLayout(session, Flow.PAGE));	//create the input panel flowing vertically
		inputPanel.setBackgroundColor(RGBColor.AQUA_MARINE);
		final TextControl<Float> inputTextControl=new TextControl<Float>(session, Float.class);	//create a text input control to receive a float
		inputTextControl.getModel().setLabel("Input Number");	//add a label to the text input control
		inputTextControl.getModel().setValidator(new ValueRequiredValidator<Float>(session));	//install a validator requiring a value
		inputTextControl.setBackgroundColor(RGBColor.DARK_GOLDEN_ROD);

		inputTextControl.getModel().setDescription("This is a description of the first text control.");
		inputTextControl.setFlyoverEnabled(true);	//turn on flyovers

		inputTextControl.getFlyoverStrategy().setPreferredWidth(new Extent(15, Extent.Unit.EM));
		inputTextControl.getFlyoverStrategy().setPreferredHeight(new Extent(10, Extent.Unit.EM));
		
//TODO del when works		inputTextControl.addMouseListener(new TextControl.DefaultFlyoverStrategy<TextControl>(inputTextControl));
		
		inputPanel.add(inputTextControl);	//add the input control to the input panel
		final TextControl<Float> outputTextControl=new TextControl<Float>(session, Float.class);	//create a text input control to display the result
		outputTextControl.getModel().setLabel("Double the Number");	//add a label to the text output control
		outputTextControl.getModel().setEditable(false);	//set the text output control to read-only so that the user cannot modify it
		inputPanel.add(outputTextControl);	//add the output control to the input panel
		inputTextControl.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGuisePropertyChangeListener<ValueModel<Float>, Float>()
				{
					public void propertyChange(final GuisePropertyChangeEvent<ValueModel<Float>, Float> propertyChangeEvent)
					{
						final Float newValue=propertyChangeEvent.getNewValue();	//get the new value
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
		checkbox.getModel().setLabel("Enable the button \u278A");
		try
		{
			checkbox.getModel().setValue(Boolean.TRUE);
		}
		catch(final ValidationException validationException)	//we have no validator installed in the check control model, so we don't expect changing its value ever to cause any problems
		{
			throw new AssertionError(validationException);
		}										
		inputPanel.add(checkbox);
		
		final ListControl<Float> listControl=new ListControl<Float>(session, Float.class, new SingleListSelectionPolicy<Float>());	//create a list control allowing only single selections
		listControl.getModel().setLabel("Pick a Number");	//set the list control label
		listControl.setRowCount(5);
		listControl.getModel().add(new Float(10));
		listControl.getModel().add(new Float(20));
		listControl.getModel().add(new Float(30));
		listControl.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGuisePropertyChangeListener<ListSelectModel<Float>, Float>()
				{
					public void propertyChange(final GuisePropertyChangeEvent<ListSelectModel<Float>, Float> propertyChangeEvent)
					{
						final Float newValue=propertyChangeEvent.getNewValue();	//get the new value
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
		
		
		testLabel=new Label(session, "testLabel");
		testLabel.setDragEnabled(true);
		testLabel.setStyleID("title");
		testLabel.getModel().setLabel("This is label text from the model.");
		
		
		final Object testCookie=session.getEnvironment().getProperty("testCookie");
		if(testCookie instanceof String)
		{
			testLabel.getModel().setLabel((String)testCookie);
		}
		
		
		contentPanel.add(testLabel);	//add a new label
		
		final LayoutPanel buttonPanel=new LayoutPanel(session, "testButtonPanel", new FlowLayout(session, Flow.LINE));	//create a panel flowing horizontally

		final Button testButton=new Button(session, "testButton");
		testButton.getModel().setLabel("Click here to go to the 'Hello World' demo.");
		testButton.getModel().setDescription("This is the hello world button.");
/*TODO fix
		testButton.setFlyoverEnabled(true);	//turn on flyovers
		testButton.getFlyoverStrategy().setPreferredWidth(new Extent(15, Extent.Unit.EM));
		testButton.getFlyoverStrategy().setPreferredHeight(new Extent(10, Extent.Unit.EM));
*/
		
		testButton.setCornerArcSize(Corner.LINE_FAR_PAGE_NEAR, Component.ROUNDED_CORNER_ARC_SIZE);
		testButton.setCornerArcSize(Corner.LINE_FAR_PAGE_FAR, Component.ROUNDED_CORNER_ARC_SIZE);
		
		testButton.getModel().addActionListener(new NavigateActionListener<ActionModel>("helloworld"));
		buttonPanel.add(testButton);	//add a new button
		
		final Button testButton2=new Button(session, "testButton2");
		testButton2.getModel().setLabel("Click this button to change the text.");
		testButton2.getModel().addActionListener(new ActionListener<ActionModel>()
				{
					public void actionPerformed(ActionEvent<ActionModel> actionEvent)
					{
						testLabel.getModel().setLabel("You pressed the button!");
/*TODO del test						
						final int MAX_FACTOR=5;
						final Integer[][] multiplicationTableData=new Integer[MAX_FACTOR+1][MAX_FACTOR+1];	//create the table data array
						for(int rowIndex=MAX_FACTOR; rowIndex>=0; --rowIndex)	//for each row
						{
							for(int columnIndex=MAX_FACTOR; columnIndex>=0; --columnIndex)	//for each column
							{
								multiplicationTableData[rowIndex][columnIndex]=new Integer(rowIndex*columnIndex);	//fill this cell with data
							}
						}
						final String[] columnNames=new String[MAX_FACTOR+1];	//create the array of column names
						for(int columnIndex=MAX_FACTOR; columnIndex>=0; --columnIndex)	//for each column
						{
							columnNames[columnIndex]=Integer.toString(columnIndex);	//generate the column name
						}
						final Table multiplicationTable=new Table(session, Integer.class, multiplicationTableData, columnNames);	//create the table component
						multiplicationTable.getModel().setLabel("Multiplication Table");	//give the table a label
*/

					  final DefaultOptionDialogFrame myDialog=new DefaultOptionDialogFrame(session, DefaultOptionDialogFrame.Option.OK);    //show the OK button
						final Heading heading=new Heading(session, 0);

						heading.getModel().setLabel("Delete Dialog");

						myDialog.setOptionContent(heading);

						myDialog.open();
						
						session.getEnvironment().setProperty("testCookie", "This is a successful cookie value.");
						
/*TODO bring back
						final Label label=new Label(session, new DefaultLabelModel(session, "Are you sure?"));
						
						final DefaultOptionDialogFrame confirmDialog=new DefaultOptionDialogFrame(session, label, DefaultOptionDialogFrame.Option.OK, DefaultOptionDialogFrame.Option.CANCEL);
						confirmDialog.getModel().setLabel("Confirm your choice");
						confirmDialog.setPreferredWidth(new Extent(20, Extent.Unit.EM));
						confirmDialog.setPreferredHeight(new Extent(10, Extent.Unit.EM));
						confirmDialog.addPropertyChangeListener(ModalComponent.MODE_PROPERTY, new AbstractPropertyValueChangeListener<Mode>()
								{
									public void propertyValueChange(final PropertyValueChangeEvent<Mode> propertyValueChangeEvent)
									{
										if(propertyValueChangeEvent.getNewValue()==null)	//if modality is ended
										{
											testLabel.getModel().setLabel("resulting option is "+confirmDialog.getModel().getValue());											
										}
									}
								});
						confirmDialog.open();
*/
					}
				});
		buttonPanel.add(testButton2);	//add a new button
		final Link testLink=new Link(session);
		testLink.getModel().setLabel("This is a link.");
		testLink.getModel().addActionListener(new ActionListener<ActionModel>()
				{
					public void actionPerformed(ActionEvent<ActionModel> actionEvent)
					{

						session.getEnvironment().removeProperty("testCookie");

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
						getSession().navigateModal("edituser", new ModalNavigationAdapter<EditUserPanel>()
								{
									/**Called when an a modal panel ends its modality.
									@param modalEvent The event indicating the panel ending modality and the modal value.
									*/
									public void modalEnded(final ModalEvent<EditUserPanel> modalEvent)
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
		frameLink.getModel().setDescription("This is a flyover for the frame link.");
		frameLink.setFlyoverEnabled(true);	//turn on flyovers
		frameLink.getFlyoverStrategy().setPreferredWidth(new Extent(15, Extent.Unit.EM));
		frameLink.getFlyoverStrategy().setPreferredHeight(new Extent(10, Extent.Unit.EM));
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
						frame.open();
					}
				});
		buttonPanel.add(frameLink);

		final Link modalFrameLink=new Link(session);
		modalFrameLink.getModel().setLabel("Modal Frame");
		modalFrameLink.getModel().addActionListener(new ActionListener<ActionModel>()
				{
					public void actionPerformed(ActionEvent<ActionModel> actionEvent)
					{
						final DefaultDialogFrame<Boolean> dialog=new DefaultDialogFrame<Boolean>(session, Boolean.class);
						dialog.getModel().setLabel("Test Dialog");
						
						final TextControl<Float> inputTextControl=new TextControl<Float>(session, Float.class);	//create a text input control to receive a float
						inputTextControl.getModel().setLabel("Input Number");	//add a label to the text input control
						inputTextControl.getModel().setValidator(new ValueRequiredValidator<Float>(session));	//install a validator requiring a value
						((Container<?>)dialog.getContent()).add(inputTextControl);	//add the input control to the input panel
						final TextControl<Float> outputTextControl=new TextControl<Float>(session, Float.class);	//create a text input control to display the result
						outputTextControl.getModel().setLabel("Double the Number");	//add a label to the text output control
						((Container<?>)dialog.getContent()).add(outputTextControl);	//add the output control to the input panel
						
						dialog.open(true);
					}
				});
		buttonPanel.add(modalFrameLink);

		contentPanel.add(buttonPanel);	//add the button panel to the panel
		
		
		
		final LayoutPanel sliderPanel=new LayoutPanel(session, new FlowLayout(session, Flow.LINE));

		
		final ValueModel<Integer> sliderModel=new DefaultValueModel<Integer>(session, Integer.class, 100);	//default to 100
		sliderModel.setValidator(new IntegerRangeValidator(session, 0, 100));	//set a range validator for the model
		sliderModel.setLabel("Slider Value");
		
		final SliderControl<Integer> horizontalSlider=new SliderControl<Integer>(session, sliderModel, Flow.LINE);
		horizontalSlider.setThumbImage(URI.create("slider-thumb.gif"));
		horizontalSlider.setTrackImage(URI.create("slider-track.gif"));
		sliderPanel.add(horizontalSlider);

		final SliderControl<Integer> verticalSlider=new SliderControl<Integer>(session, sliderModel, Flow.PAGE);
		sliderPanel.add(verticalSlider);
		
		final TextControl<Integer> sliderInput=new TextControl<Integer>(session, sliderModel);	//create a text input control
		sliderPanel.add(sliderInput);

		contentPanel.add(sliderPanel);	//add the slider panel to the panel
		
		final TextControl<String> textInput=new TextControl<String>(session, "textInput", String.class);	//create a text input control
		textInput.getModel().setLabel("This is the text input label.");
		textInput.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGuisePropertyChangeListener<ValueModel<String>, String>()
				{
					public void propertyChange(GuisePropertyChangeEvent<ValueModel<String>, String> propertyChangeEvent)
					{
						testLabel.getModel().setLabel(propertyChangeEvent.getNewValue());
						if(frame!=null)
						{
							frame.label.getModel().setLabel(propertyChangeEvent.getNewValue());
							frame.getModel().setLabel("Updated frame.");
						}
					}
				});
//TODO del		textInput.getModel().setValidator(new RegularExpressionStringValidator("[a-z]*"));
		contentPanel.add(textInput);
	
	
	
		final LayoutPanel horizontalPanel=new LayoutPanel(session, new FlowLayout(session, Flow.LINE));	//create a panel flowing horizontally
	
	
		final GroupPanel booleanPanel=new GroupPanel(session, new FlowLayout(session, Flow.PAGE));	//create a panel flowing vertically
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

		sliderModel.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGuisePropertyChangeListener<ValueModel<Integer>, Integer>()
				{
					public void propertyChange(GuisePropertyChangeEvent<ValueModel<Integer>, Integer> propertyChangeEvent)
					{
						final Integer newValue=propertyChangeEvent.getNewValue();	//get the new value
						if(newValue!=null)	//if there is a new value
						{
							testLabel.setOpacity(newValue.floatValue()/100);	//update the label opacity
							image.getModel().setOpacity(newValue.floatValue()/100);	//update the image opacity
						}
					}
				});

		
		contentPanel.add(horizontalPanel);
		
/*TODO del		
		final Heading resourceHeading=new Heading(session, 2);
		resourceHeading.getModel().setLabelResourceKey("test.resource");
		add(resourceHeading);
*/

		final Label afterImageLabel=new Label(session);
		afterImageLabel.getModel().setLabel("This is a lot of text. ;alsjfd ;lkjas ;ljag ;lkjas g;lkajg; laksgj akjlshf lkjashd flkjsdhlksahlsadkhj asldkhjf ;sgdh a;lgkh a;glkha s;dglh asgd;");
		contentPanel.add(afterImageLabel);

		final ListControl<String> listSelectControl=new ListControl<String>(session, String.class, new SingleListSelectionPolicy<String>());
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

//TODO del		getSession().setLocale(Locale.FRANCE);
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
		final LayoutPanel temperaturePanel=new LayoutPanel(session, new FlowLayout(session, Flow.PAGE));	//create the input panel flowing vertically
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

		final TabControl<String> stringTabControl=new TabControl<String>(session, String.class, Flow.LINE);
		stringTabControl.getModel().add("First tab");
		stringTabControl.getModel().add("Second tab");
		stringTabControl.getModel().add("Third tab");
		contentPanel.add(stringTabControl);
		try
		{
			stringTabControl.getModel().setSelectedValues("First tab");
		}
		catch (ValidationException e)
		{
			throw new AssertionError(e);
		}

		
		
		final CardTabControl remoteTabControl=new CardTabControl(session, tabbedPanel, Flow.LINE);
		contentPanel.add(remoteTabControl);
		

		checkbox.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGuisePropertyChangeListener<ValueModel<Boolean>, Boolean>()
				{
					public void propertyChange(final GuisePropertyChangeEvent<ValueModel<Boolean>, Boolean> propertyChangeEvent)
					{
						final Boolean newValue=propertyChangeEvent.getNewValue();	//get the new value
//TODO del						testButton.setDisplayed(newValue);	//update the button enabled state
						testButton.setVisible(newValue);	//update the button enabled state
//TODO del						testButton.setVisible(newValue);	//update the button enabled state
//TODO bring back						testButton.getModel().setEnabled(newValue);	//update the button enabled state
//TODO del Debug.trace("ready to set tabbed panel enabled to ", newValue);
//TODO del						tabbedPanel.getLayout().getConstraints(helloPanel).setEnabled(newValue);	//TODO testing
						remoteTabControl.getModel().setValueEnabled(helloPanel, newValue);	//TODO testing
					}
				});


		add(contentPanel, RegionLayout.CENTER_CONSTRAINTS);	//add the content panel in the center

		add(createMenu(session, Flow.LINE), RegionLayout.PAGE_START_CONSTRAINTS);	//add the pulldown menu at the top

//TODO fix		add(createMenu(session, Orientation.Flow.PAGE), RegionLayout.LINE_START_CONSTRAINTS);	//add the menu at the left

		add(createAccordionMenu(session, Flow.PAGE), RegionLayout.LINE_START_CONSTRAINTS);	//add the menu at the left
	}

	protected DropMenu createMenu(final GuiseSession session, final Flow flow)
	{
		final DropMenu menu=new DropMenu(session, flow);

		final DropMenu fileMenu=new DropMenu(session, "fileMenu", Flow.PAGE);
		fileMenu.getModel().setLabel("File");
		final Link openMenuLink=new Link(session, "openMenuItem");
		openMenuLink.getModel().setLabel("Open");
		fileMenu.add(openMenuLink);
		final Link closeMenuLink=new Link(session, "closeMenuItem");
		closeMenuLink.getModel().setLabel("Close");
		fileMenu.add(closeMenuLink);
		menu.add(fileMenu);

		final DropMenu editMenu=new DropMenu(session, "editMenu", Flow.PAGE);
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

		final DropMenu windowMenu=new DropMenu(session, "windowMenu", Flow.PAGE);
		windowMenu.getModel().setLabel("Window");

		final DropMenu arrangeMenu=new DropMenu(session, "arrangeMenu", Flow.PAGE);
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


	protected AccordionMenu createAccordionMenu(final GuiseSession session, final Flow flow)
	{
		final AccordionMenu menu=new AccordionMenu(session, flow);

		final AccordionMenu fileMenu=new AccordionMenu(session, Flow.PAGE);
		fileMenu.getModel().setLabel("File");
		final Link openMenuLink=new Link(session);
		openMenuLink.getModel().setLabel("Open");
		fileMenu.add(openMenuLink);
		final Link closeMenuLink=new Link(session);
		closeMenuLink.getModel().setLabel("Close");
		fileMenu.add(closeMenuLink);
		menu.add(fileMenu);

		final AccordionMenu editMenu=new AccordionMenu(session, Flow.PAGE);
		editMenu.getModel().setLabel("Edit");
		final Message message1=new Message(session, new DefaultMessageModel(session, "This is a message to show."));
		editMenu.add(message1);
		menu.add(editMenu);
		
		editMenu.getModel().addActionListener(new ActionListener<ActionModel>()	//testing accordion menu action
				{
					public void actionPerformed(ActionEvent<ActionModel> actionEvent)
					{
						testLabel.getModel().setLabel("You pressed the accordion edit menu!");
					}
				});

		final AccordionMenu stuffMenu=new AccordionMenu(session, Flow.PAGE);
		stuffMenu.getModel().setLabel("Stuff");
		final Message message2=new Message(session, new DefaultMessageModel(session, "This is a message to show."));
		stuffMenu.add(message2);
		menu.add(stuffMenu);

		return menu;
	}

	protected static class TestFrame extends DefaultFrame
	{
		protected final Label label;
		
		public TestFrame(final GuiseSession session)
		{
			super(session);
//TODO del			final LayoutPanel contentPanel=new LayoutPanel(session, new FlowLayout(session, Flow.PAGE)); 
			label=new Label(session);
			label.getModel().setLabel("This is frame content");
			setContent(label);
/*TODO del; testing scrolled flyovers			
			contentPanel.add(label);
			
			final Text text=new Text(session);
			text.getModel().setText("This is some text. It is added so that it will make the frame wrap and keep going."
					+" This is some text. It is added so that it will make the frame wrap and keep going."
					+" This is some text. It is added so that it will make the frame wrap and keep going."
					+" This is some text. It is added so that it will make the frame wrap and keep going."
					+" This is some text. It is added so that it will make the frame wrap and keep going."
					+" This is some text. It is added so that it will make the frame wrap and keep going."
					+" This is some text. It is added so that it will make the frame wrap and keep going."
					+" This is some text. It is added so that it will make the frame wrap and keep going."
					+" This is some text. It is added so that it will make the frame wrap and keep going."
					+" This is some text. It is added so that it will make the frame wrap and keep going."
			);
			contentPanel.add(text);

			final Link frameLink=new Link(session);
			frameLink.getModel().setLabel("Frame");
			frameLink.getModel().setDescription("This is a flyover for the frame link.");
			frameLink.setFlyoverEnabled(true);	//turn on flyovers
			frameLink.getFlyoverStrategy().setPreferredWidth(new Extent(15, Extent.Unit.EM));
			frameLink.getFlyoverStrategy().setPreferredHeight(new Extent(10, Extent.Unit.EM));
			contentPanel.add(frameLink);

			final Text otherText=new Text(session);
			otherText.getModel().setText("This is some text. It is added so that it will make the frame wrap and keep going."
					+" This is some text. It is added so that it will make the frame wrap and keep going."
			);
			contentPanel.add(otherText);

			setContent(contentPanel);
		
			setPreferredWidth(new Extent(15, Extent.Unit.EM));
			setPreferredHeight(new Extent(10, Extent.Unit.EM));
*/
		}
	}
}
