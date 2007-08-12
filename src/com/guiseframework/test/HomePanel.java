package com.guiseframework.test;

import java.beans.PropertyVetoException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.garretwilson.beans.AbstractGenericPropertyChangeListener;
import com.garretwilson.beans.GenericPropertyChangeEvent;
import com.garretwilson.country.us.RTN;
import com.garretwilson.country.us.SSN;
import com.garretwilson.iso.idcard.PAN;
import com.garretwilson.iso.idcard.Product;
import com.garretwilson.itu.TelephoneNumber;
import com.garretwilson.net.URIPath;
import com.garretwilson.text.TextUtilities;
import com.garretwilson.util.Debug;
import com.guiseframework.Bookmark;
import com.guiseframework.GuiseSession;
import com.guiseframework.Resources;
import com.guiseframework.Bookmark.Parameter;
import com.guiseframework.audio.Audio;
import com.guiseframework.component.*;
import com.guiseframework.component.effect.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.converter.ConversionException;
import com.guiseframework.converter.DateStringLiteralConverter;
import com.guiseframework.converter.DateStringLiteralStyle;
import com.guiseframework.demo.DemoUser;
import com.guiseframework.demo.EditUserPanel;
import com.guiseframework.event.*;
import com.guiseframework.geometry.Extent;
import com.guiseframework.model.*;
import com.guiseframework.style.RGBColor;
import com.guiseframework.validator.IntegerRangeValidator;
import com.guiseframework.validator.PANValidator;
import com.guiseframework.validator.RegularExpressionStringValidator;
import com.guiseframework.validator.ValidationException;
import com.guiseframework.validator.ValueRequiredValidator;

/**Test panel for a home page.
@author Garret Wilson
*/
public class HomePanel extends DefaultNavigationPanel
{

	private TestFrame frame=null;
	final Label testLabel;

	/**Default constructor.*/
	public HomePanel()
	{
		super(new RegionLayout());	//construct the parent using a region layout
		setLabel("Home Panel Test with embedded resource text: "+Resources.createStringResourceReference("menu.language.label"));	//set the panel label

		final LayoutPanel contentPanel=new LayoutPanel(new FlowLayout(Flow.PAGE)); 

		
/*TODO del		

		
Debug.trace("testing date");

final DateFormat dateFormat=DateFormat.getDateInstance(DateFormat.SHORT);	//create a predefined date format instance
Debug.trace("date format class:", dateFormat.getClass());
Debug.trace("instance of simple date format?", dateFormat instanceof SimpleDateFormat);
if(dateFormat instanceof SimpleDateFormat)
{
	final SimpleDateFormat simpleDateFormat=(SimpleDateFormat)dateFormat;
	Debug.trace("pattern:", simpleDateFormat.toPattern());
	Debug.trace("localized pattern:", simpleDateFormat.toLocalizedPattern());
}




		final Date now=new Date();
		final DateStringLiteralConverter shortDateConverter=new DateStringLiteralConverter(DateStringLiteralStyle.SHORT);
		final DateStringLiteralConverter mediumDateConverter=new DateStringLiteralConverter(DateStringLiteralStyle.MEDIUM);
		final DateStringLiteralConverter longDateConverter=new DateStringLiteralConverter(DateStringLiteralStyle.LONG);
		final DateStringLiteralConverter fullDateConverter=new DateStringLiteralConverter(DateStringLiteralStyle.FULL);
		
try
{
Debug.trace("short date:", shortDateConverter.convertValue(now));
Debug.trace("medium date:", mediumDateConverter.convertValue(now));
Debug.trace("long date:", longDateConverter.convertValue(now));
Debug.trace("full date:", fullDateConverter.convertValue(now));
}
catch(final ConversionException conversionException)
{
	Debug.error(conversionException);
}
try
{
Debug.trace("converting 3/24/06");
Debug.trace(shortDateConverter.convertLiteral("3/24/06"));
}
catch(final ConversionException conversionException)
{
	Debug.error(conversionException);
}
try
{
Debug.trace("converting 03/24/06");
Debug.trace(shortDateConverter.convertLiteral("03/24/06"));
}
catch(final ConversionException conversionException)
{
	Debug.error(conversionException);
}
try
{
Debug.trace("converting 3/24/2006");
Debug.trace(shortDateConverter.convertLiteral("3/24/2006"));
}
catch(final ConversionException conversionException)
{
	Debug.error(conversionException);
}
try
{
Debug.trace("converting 3-24-06");
Debug.trace(shortDateConverter.convertLiteral("3-24-06"));
}
catch(final ConversionException conversionException)
{
	Debug.error(conversionException);
}
try
{
Debug.trace("converting 3-24-2006");
Debug.trace(shortDateConverter.convertLiteral("3-24-2006"));
}
catch(final ConversionException conversionException)
{
	Debug.error(conversionException);
}
*/

		final SelectLink selectLink=new SelectLink();
		selectLink.setLabel("This is a select link");
		selectLink.setToggle(true);
		selectLink.setSelectedIcon(URI.create("guise/images/accept.gif"));
		contentPanel.add(selectLink);

		final TreeControl tree=new TreeControl();
		final TreeNodeModel<String> treeNode1=new DefaultTreeNodeModel<String>(String.class, "Node 1");
//TODO del		treeNode1.setExpanded(true);
		final TreeNodeModel<String> treeNode11=new DefaultTreeNodeModel<String>(String.class, "Node 1.1");
//	TODO del		treeNode11.setExpanded(true);
		treeNode1.add(treeNode11);
		final TreeNodeModel<String> treeNode111=new DefaultTreeNodeModel<String>(String.class, "Node 1.1.1");
//	TODO del		treeNode111.setExpanded(true);
		treeNode11.add(treeNode111);
		final TreeNodeModel<String> treeNode112=new DefaultTreeNodeModel<String>(String.class, "Node 1.1.2");
//	TODO del		treeNode112.setExpanded(true);
		treeNode11.add(treeNode112);
		final TreeNodeModel<String> treeNode113=new DefaultTreeNodeModel<String>(String.class, "Node 1.1.3");
//	TODO del		treeNode1.setExpanded(true);
		treeNode113.add(treeNode113);
		final TreeNodeModel<String> treeNode12=new DefaultTreeNodeModel<String>(String.class, "Node 1.2");
//	TODO del		treeNode12.setExpanded(true);
		treeNode1.add(treeNode12);
		final TreeNodeModel<String> treeNode13=new DefaultTreeNodeModel<String>(String.class, "Node 1.3");
//	TODO del		treeNode13.setExpanded(true);
		treeNode1.add(treeNode13);
		final TreeNodeModel<String> treeNode2=new DefaultTreeNodeModel<String>(String.class, "Node 2");
//	TODO del		treeNode12.setExpanded(true);
		final TreeNodeModel<String> treeNode3=new DefaultTreeNodeModel<String>(String.class, "Node 3");
//	TODO del		treeNode3.setExpanded(true);

		tree.getRootNode().add(treeNode1);
		tree.getRootNode().add(treeNode2);
		tree.getRootNode().add(treeNode3);
		contentPanel.add(tree);		
		
/*TODO del		
    final GroupPanel cardPanelPanel=new GroupPanel(new FlowLayout(Flow.PAGE));    //create a panel flowing vertically
    cardPanelPanel.setLabelText("CardTabControl associated with CardPanel");
            //CardPanel
    final CardPanel cardPanel=new CardPanel();   //create a card panel
                //page 1
    final Panel<?> cardPanelPage1=new LayoutPanel(); //create a panel to serve as the page
    final Heading cardPanelPage1Heading=new Heading(0);    //create a top-level heading
    cardPanelPage1Heading.setLabelText("This is page 1.");   //set the text of the heading
    cardPanelPage1.add(cardPanelPage1Heading);  //add the heading to the page
    cardPanel.add(cardPanelPage1, new CardLayout.Constraints(new DefaultLabelModel("Page 1")));    //add the panel with a label
                //page 2
    final Panel<?> cardPanelPage2=new LayoutPanel(); //create a panel to serve as the page
    final Heading cardPanelPage2Heading=new Heading(0);    //create a top-level heading
    cardPanelPage2Heading.setLabelText("This is page 2.");   //set the text of the heading
    cardPanelPage2.add(cardPanelPage2Heading);  //add the heading to the page
    cardPanel.add(cardPanelPage2, new CardLayout.Constraints(new DefaultLabelModel("Page 2")));    //add the panel with a label
            //CardTabControl
    final CardTabControl cardPanelTabControl=new CardTabControl(cardPanel, Flow.LINE); //create a horizontal card tab control to control the existing card panel
    cardPanelPanel.add(cardPanelTabControl);    //place the tab control above the card panel to illustrate common usage
    cardPanelPanel.add(cardPanel);
    cardPanel.getLayout().getConstraints(cardPanelPage2).setEnabled(Boolean.FALSE);
    
    contentPanel.add(cardPanelPanel);
*/
		
/*TODO del		
		final CalendarMonthTableModel calendarMonthTableModel=new CalendarMonthTableModel();
		final Table calendarMonthTable=new Table(calendarMonthTableModel);
		contentPanel.add(calendarMonthTable);
		
		final CalendarControl calendarControl=new CalendarControl();
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
*/
		
		
		//input panel
		final LayoutPanel inputPanel=new LayoutPanel(new FlowLayout(Flow.PAGE));	//create the input panel flowing vertically
		inputPanel.setBackgroundColor(RGBColor.AQUA_MARINE);
		final TextControl<Float> inputTextControl=new TextControl<Float>(Float.class);	//create a text input control to receive a float
		inputTextControl.setMaximumLength(5);
		inputTextControl.setLabel("Input Number");	//add a label to the text input control
		inputTextControl.setValidator(new ValueRequiredValidator<Float>());	//install a validator requiring a value
		inputTextControl.setBackgroundColor(RGBColor.DARK_GOLDEN_ROD);

		inputTextControl.setDescription("This is a description of the first text control.");
		inputTextControl.setFlyoverEnabled(true);	//turn on flyovers

		inputTextControl.getFlyoverStrategy().setLineExtent(new Extent(15, Extent.Unit.EM));
		inputTextControl.getFlyoverStrategy().setPageExtent(new Extent(10, Extent.Unit.EM));
		
//TODO del when works		inputTextControl.addMouseListener(new TextControl.DefaultFlyoverStrategy<TextControl>(inputTextControl));


		final TextControl<Float> outputTextControl=new TextControl<Float>(Float.class);	//create a text input control to display the result
		outputTextControl.setLabel("Double the Number");	//add a label to the text output control
		outputTextControl.setEditable(false);	//set the text output control to read-only so that the user cannot modify it
		inputPanel.add(outputTextControl);	//add the output control to the input panel
		inputTextControl.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGenericPropertyChangeListener<Float>()
				{
					public void propertyChange(final GenericPropertyChangeEvent<Float> propertyChangeEvent)
					{
						final Float newValue=propertyChangeEvent.getNewValue();	//get the new value
						try
						{
							outputTextControl.setValue(newValue*2);	//update the value
						}
						catch(final PropertyVetoException propertyVetoException)	//if the change was vetoed, ignore the exception
						{
						}							
					}
				});
		final CheckControl checkbox=new CheckControl();
		checkbox.setLabel("Enable the button \u278A");
		try
		{
			checkbox.setValue(Boolean.TRUE);
		}
		catch(final PropertyVetoException propertyVetoException)	//if the change was vetoed, ignore the exception
		{
		}										
		inputPanel.add(checkbox);
		
		final ListControl<Float> listControl=new ListControl<Float>(Float.class, new SingleListSelectionPolicy<Float>());	//create a list control allowing only single selections
		listControl.setLabel("Pick a Number");	//set the list control label
		listControl.setRowCount(5);
		listControl.add(new Float(10));
		listControl.add(new Float(20));
		listControl.add(new Float(30));
		listControl.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGenericPropertyChangeListener<Float>()
				{
					public void propertyChange(final GenericPropertyChangeEvent<Float> propertyChangeEvent)
					{
						final Float newValue=propertyChangeEvent.getNewValue();	//get the new value
						try
						{
Debug.trace("list control changed value to", newValue);
							outputTextControl.setValue(newValue!=null ? newValue*2 : null);	//update the value
						}
						catch(final PropertyVetoException propertyVetoException)	//if the change was vetoed, ignore the exception
						{
						}							
					}
				});
		inputPanel.add(listControl);

		final TextControl<PAN> ccTextControl=new TextControl<PAN>(PAN.class);
		ccTextControl.setLabel("Primary Account Number");	//add a label to the text input control
		ccTextControl.setValidator(new PANValidator(Product.VISA, Product.MASTERCARD, Product.AMERICAN_EXPRESS));	//install a ID card product validator		
		inputPanel.add(ccTextControl);

		final TextControl<SSN> ssnTextControl=new TextControl<SSN>(SSN.class);
		ssnTextControl.setLabel("SSN");	//add a label to the text input control
		inputPanel.add(ssnTextControl);		

		final TextControl<TelephoneNumber> telTextControl=new TextControl<TelephoneNumber>(TelephoneNumber.class);
		telTextControl.setLabel("Telephone Number");	//add a label to the text input control
		inputPanel.add(telTextControl);		

		final TextControl<RTN> rtnTextControl=new TextControl<RTN>(RTN.class);
		rtnTextControl.setLabel("Routing Transit Number");	//add a label to the text input control
		inputPanel.add(rtnTextControl);		

		contentPanel.add(inputPanel);	//add the input panel to the temperature panel
		
		
		testLabel=new Label();
		testLabel.setDragEnabled(true);
		testLabel.setStyleID("title");
		testLabel.setLabel("This is label text from the model.");
		
		
		final Object testCookie=getSession().getPlatform().getEnvironment().getProperty("testCookie");
		if(testCookie instanceof String)
		{
			testLabel.setLabel((String)testCookie);
		}
		
		
		contentPanel.add(testLabel);	//add a new label
		
		
		
/*TODO del
		final Flash flash=new Flash();
		flash.setFlashURI(URI.create("test.swf"));
		flash.setPreferredWidth(new Extent(564));
		flash.setPreferredHeight(new Extent(474));
		contentPanel.add(flash);
*/
/*TODO del
    final ImageBooleanSelectActionControl imageAction=new ImageBooleanSelectActionControl();
    imageAction.setImage(URI.create("http://www.garretwilson.com/photos/2000/february/cowcalf.jpg"));
    imageAction.setRolloverImage(URI.create("slider-thumb.gif"));
    contentPanel.add(imageAction);
*/
		
		final Text testText=new Text();
		testText.setTextContentType(XHTML_FRAGMENT_CONTENT_TYPE);
//TODO bring back		testText.getModel().setTextResourceKey("test.html");
		testText.setText("this is <strong>good</strong> stuff");
		
		final Label boundLabel=new Label();
		boundLabel.setLabel("Button");
		boundLabel.setDescription("This is button flyover.");
		boundLabel.setFlyoverEnabled(true);	//turn on flyovers
		testText.add(boundLabel, new ReferenceConstraints("boundComponent"));

		contentPanel.add(testText);
		
		final LayoutPanel buttonPanel=new LayoutPanel(new FlowLayout(Flow.LINE));	//create a panel flowing horizontally

		final Button testButton=new Button();
		testButton.setLabel("Click here to go to the 'Hello World' demo.");
		testButton.setDescription("This is the hello world button.");
/*TODO fix
		testButton.setFlyoverEnabled(true);	//turn on flyovers
		testButton.getFlyoverStrategy().setPreferredWidth(new Extent(15, Extent.Unit.EM));
		testButton.getFlyoverStrategy().setPreferredHeight(new Extent(10, Extent.Unit.EM));
*/
		
		testButton.setCornerArcSize(Corner.LINE_FAR_PAGE_NEAR, Component.ROUNDED_CORNER_ARC_SIZE);
		testButton.setCornerArcSize(Corner.LINE_FAR_PAGE_FAR, Component.ROUNDED_CORNER_ARC_SIZE);
		
		testButton.addActionListener(new NavigateActionListener(new URIPath("helloworld")));
		buttonPanel.add(testButton);	//add a new button
		
		final Button testButton2=new Button();
		testButton2.setLabel("Click this button to change the text.");
		testButton2.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						testLabel.setLabel("You pressed the button!");
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
						final Table multiplicationTable=new Table(Integer.class, multiplicationTableData, columnNames);	//create the table component
						multiplicationTable.setLabelText("Multiplication Table");	//give the table a label
*/

					  final NotificationOptionDialogFrame myDialog=new NotificationOptionDialogFrame(Notification.Option.OK);    //show the OK button
						final Heading heading=new Heading(0);

						heading.setLabel("Delete Dialog");

						myDialog.setOptionContent(heading);

						myDialog.open();
						
						getSession().getPlatform().getEnvironment().setProperty("testCookie", "This is a successful cookie value.");
						
/*TODO bring back
						final Label label=new Label(new DefaultLabelModel("Are you sure?"));
						
						final DefaultOptionDialogFrame confirmDialog=new DefaultOptionDialogFrame(label, DefaultOptionDialogFrame.Option.OK, DefaultOptionDialogFrame.Option.CANCEL);
						confirmDialog.setLabelText("Confirm your choice");
						confirmDialog.setPreferredWidth(new Extent(20, Extent.Unit.EM));
						confirmDialog.setPreferredHeight(new Extent(10, Extent.Unit.EM));
						confirmDialog.addPropertyChangeListener(ModalComponent.MODE_PROPERTY, new AbstractPropertyValueChangeListener<Mode>()
								{
									public void propertyValueChange(final PropertyValueChangeEvent<Mode> propertyValueChangeEvent)
									{
										if(propertyValueChangeEvent.getNewValue()==null)	//if modality is ended
										{
											testLabel.setLabelText("resulting option is "+confirmDialog.getModel().getValue());											
										}
									}
								});
						confirmDialog.open();
*/
					}
				});
		buttonPanel.add(testButton2);	//add a new button
		final Link testLink=new Link();
		testLink.setLabel("This is a link.");
		testLink.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent actionEvent)
					{

						getSession().getPlatform().getEnvironment().removeProperty("testCookie");

						testLabel.setLabel("The link works.");

					
					
					}
				});
		buttonPanel.add(testLink);	//add a new button
		final Link modalLink=new Link();
		modalLink.setLabel("Test modal.");
		modalLink.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						getSession().navigateModal(new URIPath("edituser"), new ModalNavigationAdapter()
								{
									/**Called when an a modal panel ends its modality.
									@param modalEvent The event indicating the panel ending modality and the modal value.
									*/
									public void modalEnded(final ModalEvent modalEvent)
									{
										
									}
								}
						
						);
					}
				});

		final Button audioButton=new Button();
		audioButton.setLabel("Audio");
		audioButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						final Audio audio=new Audio();
						audio.setAudioURI(URI.create("https://dav.globalmentor.com/public/desperado.mp3"));
						audio.play();
					}
				});
		buttonPanel.add(audioButton);	//add a new button
		
		
		
		buttonPanel.add(modalLink);	//add a new button
		
		final Link helloLink=new Link();
		helloLink.setLabel("More Hello World.");
		helloLink.addActionListener(new NavigateActionListener(new URIPath("helloworld")));
		buttonPanel.add(helloLink);	//add the link

		final Link frameLink=new Link();
		frameLink.setLabel("Frame");
		frameLink.setDescription("This is a flyover for the frame link.");
		frameLink.setFlyoverEnabled(true);	//turn on flyovers
		frameLink.getFlyoverStrategy().setLineExtent(new Extent(15, Extent.Unit.EM));
		frameLink.getFlyoverStrategy().setPageExtent(new Extent(10, Extent.Unit.EM));
//TODO del		frameLink.getFlyoverStrategy().setOpenEffect(new OpacityFadeEffect(1500));	//TODO testing openEffect
		frameLink.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						if(frame==null)
						{
							frame=new TestFrame();
							frame.setLabel("Test Frame");
						}
	Debug.trace("ready to set frame visible");
						frame.open();
					}
				});
		buttonPanel.add(frameLink);

		final Link modalFrameLink=new Link();
		modalFrameLink.setLabel("Modal Frame");
		modalFrameLink.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						final DefaultDialogFrame<Boolean> dialog=new DefaultDialogFrame<Boolean>(Boolean.class);
						dialog.setLabel("Test Dialog");
						
						final TextControl<Float> inputTextControl=new TextControl<Float>(Float.class);	//create a text input control to receive a float
						inputTextControl.setLabel("Input Number");	//add a label to the text input control
						inputTextControl.setValidator(new ValueRequiredValidator<Float>());	//install a validator requiring a value
						((Container)dialog.getContent()).add(inputTextControl);	//add the input control to the input panel
						final TextControl<Float> outputTextControl=new TextControl<Float>(Float.class);	//create a text input control to display the result
						outputTextControl.setLabel("Double the Number");	//add a label to the text output control
						((Container)dialog.getContent()).add(outputTextControl);	//add the output control to the input panel
						
						dialog.open(true);
					}
				});
		buttonPanel.add(modalFrameLink);

		contentPanel.add(buttonPanel);	//add the button panel to the panel

		

		
		final Integer[][] testTableData;
		final int rowCount=19;
		final int columnCount=3;
		testTableData=new Integer[rowCount][columnCount];
		for(int rowIndex=0; rowIndex<rowCount; ++rowIndex)
		{
			for(int columnIndex=0; columnIndex<columnCount; ++columnIndex)
			{
				testTableData[rowIndex][columnIndex]=new Integer(rowIndex);
			}
		}
		final Table pagedTable=new Table(Integer.class, testTableData, "Column1", "Column2", "Column3");
		pagedTable.setDisplayRowCount(5);
		contentPanel.add(pagedTable);
		final Button tableFirstButton=new Button(pagedTable.getFirstActionPrototype());
		tableFirstButton.setLabelDisplayed(false);
		final Button tablePreviousButton=new Button(pagedTable.getPreviousActionPrototype());
		tablePreviousButton.setLabelDisplayed(false);
		final Button tableNextButton=new Button(pagedTable.getNextActionPrototype());
		tableNextButton.setLabelDisplayed(false);
		final Button tableLastButton=new Button(pagedTable.getLastActionPrototype());
		tableLastButton.setLabelDisplayed(false);
		
		contentPanel.add(tableFirstButton);
		contentPanel.add(tablePreviousButton);
		contentPanel.add(tableNextButton);
		contentPanel.add(tableLastButton);

		
		final LayoutPanel linkPanel=new LayoutPanel(new FlowLayout(Flow.LINE));	//create a panel flowing horizontally
		
		final Link nearbyLink=new Link();
		nearbyLink.setLabel("Inside");
		nearbyLink.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						getSession().navigate(URI.create("http://www.cnn.com"));
					}
				});
		linkPanel.add(nearbyLink);
		
		final Link popupLink=new Link();
		popupLink.setLabel("Popup");
		popupLink.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						getSession().navigate(URI.create("http://www.cnn.com"), "another");
					}
				});
		linkPanel.add(popupLink);
	
		contentPanel.add(linkPanel);
		
		final Link listenerPopupLink=new Link();
		listenerPopupLink.setLabel("Popup from NavigateActionListener");
		listenerPopupLink.addActionListener(new NavigateActionListener(URI.create("http://www.about.com"), "another"));
		linkPanel.add(listenerPopupLink);
		
		
		final CheckControl check3=new CheckControl();	
//TODO del		check3.setCheckType(CheckControl.CheckType.ELLIPSE);
		check3.setLabel("Third, disconnected check");
		contentPanel.add(check3);
		
		
		
		
		final LayoutPanel bookmarkPanel=new LayoutPanel(new FlowLayout(Flow.LINE));	//create a panel flowing horizontally

		final Link bookmark1Link=new Link();
		bookmark1Link.setLabel("Bookmark1");
		bookmark1Link.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						getSession().setBookmark(new Bookmark(new Bookmark.Parameter("bookmark", "1")));
					}
				});
		bookmarkPanel.add(bookmark1Link);

		final Link bookmark2Link=new Link();
		bookmark2Link.setLabel("Bookmark2");
		bookmark2Link.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						getSession().setBookmark(new Bookmark(new Bookmark.Parameter("bookmark", "2")));
					}
				});
		bookmarkPanel.add(bookmark2Link);

		final Link bookmark3Link=new Link();
		bookmark3Link.setLabel("Go Bookmark3");
		bookmark3Link.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						getSession().navigate(getSession().getNavigationPath(), new Bookmark(new Bookmark.Parameter("bookmark", "3")));
					}
				});
		bookmarkPanel.add(bookmark3Link);

		contentPanel.add(bookmarkPanel);	//add the bookmark panel to the panel
		
		
		final LayoutPanel sliderPanel=new LayoutPanel(new FlowLayout(Flow.LINE));

		
		final ValueModel<Integer> sliderModel=new DefaultValueModel<Integer>(Integer.class, 100);	//default to 100
		sliderModel.setValidator(new IntegerRangeValidator(0, 100));	//set a range validator for the model
		
		final SliderControl<Integer> horizontalSlider=new SliderControl<Integer>(sliderModel, Flow.LINE);
		horizontalSlider.setLabel("Slider Value");
		horizontalSlider.setThumbXImage(URI.create("slider-thumb.gif"));
		horizontalSlider.setTrackXImage(URI.create("slider-track.gif"));
		sliderPanel.add(horizontalSlider);

		final SliderControl<Integer> verticalSlider=new SliderControl<Integer>(sliderModel, Flow.PAGE);
		verticalSlider.setLabel("Slider Value");
		sliderPanel.add(verticalSlider);
		
		final TextControl<Integer> sliderInput=new TextControl<Integer>(sliderModel);	//create a text input control
		sliderInput.setLabel("Slider Value");
		sliderPanel.add(sliderInput);

		contentPanel.add(sliderPanel);	//add the slider panel to the panel
		
		final TextControl<String> textInput=new TextControl<String>(String.class);	//create a text input control
		textInput.setLabel("This is the text input label.");
		textInput.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGenericPropertyChangeListener<String>()
				{
					public void propertyChange(GenericPropertyChangeEvent<String> propertyChangeEvent)
					{
						testLabel.setLabel(propertyChangeEvent.getNewValue());
						if(frame!=null)
						{
							frame.label.setLabel(propertyChangeEvent.getNewValue());
							frame.setLabel("Updated frame.");
						}
					}
				});
//TODO del		textInput.getModel().setValidator(new RegularExpressionStringValidator("[a-z]*"));
		contentPanel.add(textInput);
	
	
	
		final LayoutPanel horizontalPanel=new LayoutPanel(new FlowLayout(Flow.LINE));	//create a panel flowing horizontally
	
	
		final GroupPanel booleanPanel=new GroupPanel(new FlowLayout(Flow.PAGE));	//create a panel flowing vertically
//		booleanPanel.setDragEnabled(true);
		booleanPanel.setLabel("Check one of these");
		final CheckControl check1=new CheckControl();
//TODO del		check1.setCheckType(CheckControl.CheckType.ELLIPSE);
		check1.setLabel("First check");
		booleanPanel.add(check1);	
		final CheckControl check2=new CheckControl();	
//TODO del		check2.setCheckType(CheckControl.CheckType.ELLIPSE);
		check2.setLabel("Second check");
//		check2.getModel().setEnabled(false);	//TODO fix
		booleanPanel.add(check2);
		final ModelGroup<ValueModel<Boolean>> booleanGroup=new MutualExclusionPolicyModelGroup();
		booleanGroup.add(check1);
		booleanGroup.add(check2);
		booleanGroup.add(check3);
	
		horizontalPanel.add(booleanPanel);

		final Button testButtona=new Button();
		testButtona.setLabel("Nuther button.");
//		testButtona.setDragEnabled(true);
		horizontalPanel.add(testButtona);	//add a new button
/*TODO fix		
		final Panel booleanPanela=new Panel(new FlowLayout(Axis.Y));	//create a panel flowing vertically
		booleanPanela.setLabelText("Check one of these");
		final CheckControl check1a=new CheckControl("check1");
		check1a.setCheckType(CheckControl.CheckType.ELLIPSE);
		check1a.setLabelText("First check");
		booleanPanela.add(check1a);	
		final CheckControl check2a=new CheckControl("check2");	
		check2a.setCheckType(CheckControl.CheckType.ELLIPSE);
		check2a.setLabelText("Second check");
		booleanPanela.add(check2a);	
		final ModelGroup<ValueModel<Boolean>> booleanGroupa=new MutualExclusionModelGroup();
		booleanGroupa.add(check1a.getModel());
		booleanGroupa.add(check2a.getModel());

		horizontalPanel.add(booleanPanela);
*/
		
		final Picture image=new Picture();
		image.setImage(URI.create("http://www.garretwilson.com/photos/2000/february/cowcalf.jpg"));
/*TODO fix
		image.setLabelText("Cow and Calf");
		image.getModel().setMessage("A cow and her minutes-old calf.");
*/
		image.setLabel("\u0622\u067E");
		image.setDescription("\u0628\u0627\u062A");
		image.setDragEnabled(true);
		horizontalPanel.add(image);

		sliderModel.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGenericPropertyChangeListener<Integer>()
				{
					public void propertyChange(GenericPropertyChangeEvent<Integer> propertyChangeEvent)
					{
						final Integer newValue=propertyChangeEvent.getNewValue();	//get the new value
						if(newValue!=null)	//if there is a new value
						{
							testLabel.setOpacity(newValue.floatValue()/100);	//update the label opacity
							image.setOpacity(newValue.floatValue()/100);	//update the image opacity
						}
					}
				});

		
		contentPanel.add(horizontalPanel);
		
/*TODO del		
		final Heading resourceHeading=new Heading(2);
		resourceHeading.getModel().setLabelResourceKey("test.resource");
		add(resourceHeading);
*/

		final Label afterImageLabel=new Label();
		afterImageLabel.setLabel("This is a lot of text. ;alsjfd ;lkjas ;ljag ;lkjas g;lkajg; laksgj akjlshf lkjashd flkjsdhlksahlsadkhj asldkhjf ;sgdh a;lgkh a;glkha s;dglh asgd;");
		contentPanel.add(afterImageLabel);

		final ListControl<String> listSelectControl=new ListControl<String>(String.class, new SingleListSelectionPolicy<String>());
		listSelectControl.setLabel("Choose an option.");
		listSelectControl.add("The first option");
//TODO fix		listSelectControl.getModel().add(null);
		listSelectControl.add("The second option");
		listSelectControl.add("The third option");
		listSelectControl.add("The fourth option");
//TODO bring back		listSelectControl.setValidator(new ValueRequiredValidator<String>());
		
/*TODO fix
		listSelectControl.setValueRepresentationStrategy(new ListControl.DefaultValueRepresentationStrategy<String>()
				{
					public Label createComponent(final ListSelectModel<String> model, final String value, final int index, final boolean selected, final boolean focused)
					{
						return value!=null	//if there is a value
								? super.createComponent(model, value, index, selected, focused)	//return the default component
								: new Label(new DefaultLabelModel("-"));	//return a component with the custom representation
					}
				});
*/
		
		contentPanel.add(listSelectControl);

		final TextControl<String> textAreaControl=new TextControl<String>(String.class, 25, 100, true);
		textAreaControl.setLabel("Type some text.");
/*TODO bring back
		try
		{
			textAreaControl.getModel().setValue("This is some text\nand some more on another line.\n\nSkipping two lines down, we find a line that is really long, is really, really, ;lkjas;lfk alkg; ;alkghj;alg lkjahq glkjh flkjhasdflkjhasdfl kjhasdf lkjh lkadhf lkshd flksadhf lksadhlskdqah slhjfg sd long.");
		}
		catch (ValidationException e)
		{
			throw new AssertionError(e);
		}
*/
		textAreaControl.setValidator(new RegularExpressionStringValidator(".{0,10}", true));
		contentPanel.add(textAreaControl);
/*TODO del
		final Text text=new Text();
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
		final Table multiplicationTable=new Table(Integer.class, multiplicationTableData, "0", "1");
		multiplicationTable.setLabel("Multiplication Table");
		for(final TableColumnModel<?> column:multiplicationTable.getColumns())
		{
			column.setEditable(true);
		}
		contentPanel.add(multiplicationTable);

		final TreeControl treeControl=new TreeControl();
		final TreeNodeModel<String> firstItem=new DefaultTreeNodeModel<String>(String.class, "First Item");
		firstItem.add(new DefaultTreeNodeModel<String>(String.class, "Sub Item A"));
		firstItem.add(new DefaultTreeNodeModel<String>(String.class, "Sub Item B"));
		treeControl.getRootNode().add(firstItem);
		treeControl.getRootNode().add(new DefaultTreeNodeModel<String>(String.class, "Second Item"));
		treeControl.getRootNode().add(new DefaultTreeNodeModel<String>(String.class, "Third Item"));

		contentPanel.add(treeControl);

		final TabbedPanel tabbedPanel=new TabbedPanel();
		//input panel
		final LayoutPanel temperaturePanel=new LayoutPanel(new FlowLayout(Flow.PAGE));	//create the input panel flowing vertically
		final TextControl<Float> temperatureInput=new TextControl<Float>(Float.class);	//create a text input control to receive a float
		temperatureInput.setLabel("Input Temperature");	//add a label to the text input control
		temperatureInput.setValidator(new ValueRequiredValidator<Float>());	//install a validator requiring a value
		temperaturePanel.add(temperatureInput);	//add the input control to the input panel
		final TextControl<Float> temperatureOutput=new TextControl<Float>(Float.class);	//create a text input control to display the result
		temperatureOutput.setLabel("Output Temperature");	//add a label to the text output control
		temperatureOutput.setEditable(false);	//set the text output control to read-only so that the user cannot modify it
		temperaturePanel.add(temperatureOutput);	//add the output control to the input panel
		tabbedPanel.add(temperaturePanel, new CardConstraints("Temperature"));
	
		final LayoutPanel helloPanel=new LayoutPanel();
		final Heading helloWorldHeading=new Heading(0);	//create a top-level heading
		helloWorldHeading.setLabel("Hello World!");	//set the text of the heading, using its model
		helloPanel.add(helloWorldHeading);
		tabbedPanel.add(helloPanel, new CardConstraints("Hello"));
		
		contentPanel.add(tabbedPanel);

		final TabControl<String> stringTabControl=new TabControl<String>(String.class, Flow.LINE);
		stringTabControl.add("First tab");
		stringTabControl.add("Second tab");
		stringTabControl.add("Third tab");
		contentPanel.add(stringTabControl);
		try
		{
			stringTabControl.setSelectedValues("First tab");
		}
		catch(final PropertyVetoException propertyVetoException)	//if the change was vetoed, ignore the exception
		{
		}

		
		
		final CardTabControl remoteTabControl=new CardTabControl(tabbedPanel, Flow.LINE);
		contentPanel.add(remoteTabControl);
/*TODO del
Debug.trace("tabbed panel", tabbedPanel, "has view", tabbedPanel.getViewer());
Debug.trace("card tab control", remoteTabControl, "has view", remoteTabControl.getViewer());
*/
		checkbox.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGenericPropertyChangeListener<Boolean>()
				{
					public void propertyChange(final GenericPropertyChangeEvent<Boolean> propertyChangeEvent)
					{
						final Boolean newValue=propertyChangeEvent.getNewValue();	//get the new value
//TODO del						testButton.setDisplayed(newValue);	//update the button enabled state
						testButton.setVisible(newValue);	//update the button enabled state
						
						
						ssnTextControl.setEnabled(newValue);
						
//TODO del						testButton.setVisible(newValue);	//update the button enabled state
//TODO bring back						testButton.getModel().setEnabled(newValue);	//update the button enabled state
Debug.trace("ready to set tabbed panel enabled to", newValue);
//TODO del						tabbedPanel.getLayout().getConstraints(helloPanel).setEnabled(newValue);	//TODO testing
/*TODO del
						remoteTabControl.setValueEnabled(helloPanel, newValue);	//TODO testing
Debug.trace("now tab enabled is", remoteTabControl.isValueEnabled(helloPanel));
*/
					}
				});


		add(contentPanel, new RegionConstraints(Region.CENTER));	//add the content panel in the center

		add(createMenu(Flow.LINE), new RegionConstraints(Region.PAGE_START));	//add the pulldown menu at the top

//TODO fix		add(createMenu(Orientation.Flow.PAGE), RegionLayout.LINE_START_CONSTRAINTS);	//add the menu at the left

		add(createAccordionMenu(Flow.PAGE), new RegionConstraints(Region.LINE_START));	//add the menu at the left
	}

	protected DropMenu createMenu(final Flow flow)
	{
		final DropMenu menu=new DropMenu(flow);

		final DropMenu fileMenu=new DropMenu(Flow.PAGE);
		fileMenu.setLabel("File");
		final Link openMenuLink=new Link();
		openMenuLink.setLabel("Open");
		fileMenu.add(openMenuLink);
		final Link closeMenuLink=new Link();
		closeMenuLink.setLabel("Close");
		fileMenu.add(closeMenuLink);
		menu.add(fileMenu);

		final DropMenu editMenu=new DropMenu(Flow.PAGE);
		editMenu.setLabel("Edit");
		final Link copyMenuLink=new Link();
		copyMenuLink.setLabel("Copy");
		editMenu.add(copyMenuLink);
		final Link cutMenuLink=new Link();
		cutMenuLink.setLabel("Cut");
		editMenu.add(cutMenuLink);
		final Link pasteMenuLink=new Link();
		pasteMenuLink.setLabel("Paste");
		editMenu.add(pasteMenuLink);
		menu.add(editMenu);

		final DropMenu windowMenu=new DropMenu(Flow.PAGE);
		windowMenu.setLabel("Window");

		final DropMenu arrangeMenu=new DropMenu(Flow.PAGE);
		arrangeMenu.setLabel("Arrange");
		
		final Link tileMenuLink=new Link();
		tileMenuLink.setLabel("Tile");
		arrangeMenu.add(tileMenuLink);
		final Link cascadeMenuLink=new Link();
		cascadeMenuLink.setLabel("Cascade");
		arrangeMenu.add(cascadeMenuLink);
		windowMenu.add(arrangeMenu);
		menu.add(windowMenu);

			//GlobalMentor
		final Link globalmentorLink=new Link();
		globalmentorLink.setLabel("GlobalMentor");
		globalmentorLink.addActionListener(new NavigateActionListener(URI.create("http://www.globalmentor.com/")));
		menu.add(globalmentorLink);
		
		return menu;
	}


	protected AccordionMenu createAccordionMenu(final Flow flow)
	{
		final AccordionMenu menu=new AccordionMenu(flow);

		final AccordionMenu fileMenu=new AccordionMenu(Flow.PAGE);
		fileMenu.setLabel("File");
		final Link openMenuLink=new Link();
		openMenuLink.setLabel("Open");
		fileMenu.add(openMenuLink);
		final Link closeMenuLink=new Link();
		closeMenuLink.setLabel("Close");
		fileMenu.add(closeMenuLink);
		menu.add(fileMenu);

		final AccordionMenu editMenu=new AccordionMenu(Flow.PAGE);
		editMenu.setLabel("Edit");
		final Message message1=new Message();
		message1.setMessage("This is a message to show.");
		editMenu.add(message1);
		menu.add(editMenu);
		
		editMenu.addActionListener(new ActionListener()	//testing accordion menu action
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						testLabel.setLabel("You pressed the accordion edit menu!");
					}
				});

		final AccordionMenu stuffMenu=new AccordionMenu(Flow.PAGE);
		stuffMenu.setLabel("Stuff");
		final Message message2=new Message();
		message2.setMessage("This is a message to show.");
		stuffMenu.add(message2);
		menu.add(stuffMenu);

		return menu;
	}

	protected static class TestFrame extends DefaultFrame
	{
		protected final Label label;
		
		public TestFrame()
		{
//TODO del			final LayoutPanel contentPanel=new LayoutPanel(new FlowLayout(Flow.PAGE)); 
			label=new Label();
			label.setLabel("This is frame content");
			setContent(label);
/*TODO del; testing scrolled flyovers			
			contentPanel.add(label);
			
			final Text text=new Text();
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

			final Link frameLink=new Link();
			frameLink.setLabelText("Frame");
			frameLink.getModel().setDescription("This is a flyover for the frame link.");
			frameLink.setFlyoverEnabled(true);	//turn on flyovers
			frameLink.getFlyoverStrategy().setPreferredWidth(new Extent(15, Extent.Unit.EM));
			frameLink.getFlyoverStrategy().setPreferredHeight(new Extent(10, Extent.Unit.EM));
			contentPanel.add(frameLink);

			final Text otherText=new Text();
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
