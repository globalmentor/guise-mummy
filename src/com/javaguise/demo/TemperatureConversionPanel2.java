package com.javaguise.demo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import static com.garretwilson.lang.JavaUtilities.*;
import com.garretwilson.rdf.*;
import static com.garretwilson.rdf.RDFUtilities.*;
import com.garretwilson.text.xml.XMLUtilities;
import com.garretwilson.util.Debug;
import com.javaguise.GuiseSession;
import com.javaguise.component.*;
import com.javaguise.component.layout.*;
import com.javaguise.event.*;

import com.javaguise.model.*;
import com.javaguise.validator.ValidationException;
import com.javaguise.validator.ValueRequiredValidator;

/**Temperature Conversion Guise demonstration panel.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates layout panels, group panels, float input controls, float input validation,
	radio button controls, dynamic updates (e.g. AJAX on the web platform),
	required value validation, and read-only controls.
@author Garret Wilson
*/
public class TemperatureConversionPanel2 extends DefaultNavigationPanel
{
/*TODO fix
	private final TextControl<Float> temperatureInput;
	private final TextControl<Float> temperatureOutput;
	private final CheckControl celsiusCheckControl;
	private final CheckControl fahrenheitCheckControl;
*/

	public final static URI GUISE_COMPONENT_NAMESPACE_URI=URI.create("java:com.javaguise.component.");
	public final static URI GUISE_PROPERTY_NAMESPACE_URI=URI.create("http://guiseframework.com/id/property#");

	/**Guise session constructor.
	@param session The Guise session that owns this panel.
	*/
	public TemperatureConversionPanel2(final GuiseSession session)
	{

		/*TODO del		
		super(session, new FlowLayout(session, Flow.LINE));	//construct the parent class flowing horizontally
		getModel().setLabel("Guise\u2122 Demonstration: Temperature Conversion");	//set the panel title	

			//input panel
		final LayoutPanel inputPanel=new LayoutPanel(session, new FlowLayout(session, Flow.PAGE));	//create the input panel flowing vertically
		temperatureInput=new TextControl<Float>(session, Float.class);	//create a text input control to receive a float
		temperatureInput.getModel().setLabel("Input Temperature");	//add a label to the text input control
		temperatureInput.getModel().setValidator(new ValueRequiredValidator<Float>(session));	//install a validator requiring a value
		temperatureInput.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGuisePropertyChangeListener<ValueModel<Float>, Float>()	//listen for temperature changes
				{
					public void propertyChange(final GuisePropertyChangeEvent<ValueModel<Float>, Float> propertyChangeEvent)	//if the input temperature changes
					{
						convertTemperature();	//convert the temperature						
					}
				});		
		inputPanel.add(temperatureInput);	//add the input control to the input panel
		temperatureOutput=new TextControl<Float>(session, Float.class);	//create a text input control to display the result
		temperatureOutput.getModel().setLabel("Output Temperature");	//add a label to the text output control
		temperatureOutput.getModel().setEditable(false);	//set the text output control to read-only so that the user cannot modify it
		inputPanel.add(temperatureOutput);	//add the output control to the input panel

		add(inputPanel);	//add the input panel to the temperature panel

		final LayoutPanel conversionPanel=new LayoutPanel(session, new FlowLayout(session, Flow.PAGE));	//create the right-hand panel flowing vertically
			
			//scale panel
		final GroupPanel scalePanel=new GroupPanel(session, new FlowLayout(session, Flow.PAGE));	//create the scale panel flowing vertically
		scalePanel.getModel().setLabel("Input Scale");	//set the group panel label
		celsiusCheckControl=new CheckControl(session, CheckControl.CheckType.ELLIPSE);	//create a check control for the Celsius scale, using an ellipse check are
		celsiusCheckControl.getModel().setLabel("Celsius");	//set the label of the check to indicate the scale
		try
		{
			celsiusCheckControl.getModel().setValue(Boolean.TRUE);	//default to converting from Celsius to Fahrenheit
		}
		catch(final ValidationException validationException)	//we have no validator installed in the check control model, so we don't expect changing its value ever to cause any problems
		{
			throw new AssertionError(validationException);
		}
		scalePanel.add(celsiusCheckControl);	//add the Celsius check control to the panel	
		fahrenheitCheckControl=new CheckControl(session, CheckControl.CheckType.ELLIPSE);	//create a check control for the Fahrenheit scale, using an ellipse check are
		fahrenheitCheckControl.getModel().setLabel("Fahrenheit");	//set the label of the check to indicate the scale
		scalePanel.add(fahrenheitCheckControl);	//add the Fahrenheit check control to the panel	
			//create a mutual exclusion policy group and add the Celsius and Fahrenheit check box boolean value models to get radio button functionality
		final ModelGroup<ValueModel<Boolean>> radioButtonModelGroup=new MutualExclusionPolicyModelGroup(celsiusCheckControl.getModel(), fahrenheitCheckControl.getModel());
		conversionPanel.add(scalePanel);	//add the scale panel to the conversion panel

			//create a listener to listen for check control changes and update the temperature immediately (e.g. with AJAX on the web platform)
		final GuisePropertyChangeListener<ValueModel<Boolean>, Boolean> checkControlListener=new AbstractGuisePropertyChangeListener<ValueModel<Boolean>, Boolean>()
				{
					public void propertyChange(final GuisePropertyChangeEvent<ValueModel<Boolean>, Boolean> propertyChangeEvent)
					{
						if(propertyChangeEvent.getNewValue())	//if this check control was selected
						{
							convertTemperature();	//convert the temperature							
						}
					}
				};

		celsiusCheckControl.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, checkControlListener);	//listen for the Celsius control changing
		fahrenheitCheckControl.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, checkControlListener);	//listen for the Fahrenheit control changing
		
			//conversion button
		final Button convertButton=new Button(session);	//create a button for initiating the conversion
		convertButton.getModel().setLabel("Convert");	//set the button label
		convertButton.getModel().addActionListener(new ActionListener<ActionModel>()	//when the convert button is pressed
				{
					public void actionPerformed(ActionEvent<ActionModel> actionEvent)	//convert the temperature in the input field and place the result in the output field
					{
						convertTemperature();	//convert the temperature
					}
				});
		conversionPanel.add(convertButton);	//add the button to the conversion panel
		
		add(conversionPanel);	//add the conversion panel to the panel
*/
		
		
		
		super(session);	//construct the parent class

		try
		{
			
			final URI BASE_URI=URI.create("guise:/");	//TODO fix
			
			final String xmlString=session.getStringResource("TemperatureConversionPanel2a.xml");	//TODO maybe have a getXMLResource method
//TODO del Debug.trace("just read XML:", xmlString);
			final DocumentBuilderFactory documentBuilderFactory=DocumentBuilderFactory.newInstance();	//create a document builder factory TODO create a shared document builder factory, maybe---but make sure it is used by only one thread
			documentBuilderFactory.setNamespaceAware(true);	//we must be aware of namespaces to work with RDF
			final DocumentBuilder documentBuilder=documentBuilderFactory.newDocumentBuilder();	//create a new document builder
			final byte[] bytes=xmlString.getBytes("UTF-8");	//TODO fix to check encoding
			final InputStream inputStream=new ByteArrayInputStream(bytes);
			final Document document=documentBuilder.parse(inputStream);
//TODO del Debug.trace("just parsed XML:", XMLUtilities.toString(document));
			final RDFXMLProcessor rdfProcessor=new RDFXMLProcessor();
			final RDF rdf=rdfProcessor.processRDF(document, BASE_URI);
				//integrate the structure into the RDF
			
			
			
			Debug.trace("just read RDF", RDFUtilities.toString(rdf));
			
			
			
			
			final RDFResource navigationPanelResource=RDFUtilities.getResourceByType(rdf, GUISE_COMPONENT_NAMESPACE_URI, "DefaultNavigationPanel");
Debug.trace("ready to construct panel with resource", navigationPanelResource);
			constructComponent(this, navigationPanelResource);
			
			
			
			
			
		}
		catch(final ParserConfigurationException parserConfigurationException)	//we don't expect parser configuration errors
		{
			throw new AssertionError(parserConfigurationException);
		}
		catch(final SAXException saxException)	//we don't expect parsing errors
		{
			throw new AssertionError(saxException);	//TODO maybe change to throwing an IOException
		}
		catch(final URISyntaxException uriSyntaxException)
		{
			throw new AssertionError(uriSyntaxException);	//TODO fix better
		}
		catch(final IOException ioException)	//if there is an I/O exception
		{
			throw new AssertionError(ioException);	//TODO fix better
		}		
	}

	/**Integrates component structure in an XML element hierarchy into the RDF data model.
	@param rdf The RDF data model.
	@param element
	*/
	protected void integrateStructure(final RDF rdf, final Element element)
	{
		
	}

	/**Converts the temperature based upon the current UI values.*/
/*TODO bring back
	protected void convertTemperature()
	{
		if(isValid())	//if this panel and all of its components have valid model values
		{
			final float inputValue=temperatureInput.getModel().getValue().floatValue();	//get the input value from the control
			final float outputValue;	//we'll convert the value and store it here
			if(celsiusCheckControl.getModel().getValue())	//if the Celsius radio button is selected
			{
				outputValue=(inputValue*9)/5+32;	//convert: (9c/5)+32
			}
			else if(fahrenheitCheckControl.getModel().getValue())	//if the Fahrenheit radio button is selected
			{
				outputValue=((inputValue-32)*5)/9;	//convert: 5(f-32)/9							
			}
			else	//if neither check control is selected (which should never happen, because we set one to begin with and they are both using a mutual exclusion model group)
			{
				throw new AssertionError("Expected one of the scale radio buttons to be selected.");
			}
			try
			{
				temperatureOutput.getModel().setValue(new Float(outputValue));	//store the conversion result in the temperature output control
			}
			catch(final ValidationException validationException)	//we have no validator installed in the temperature output text control, so we don't expect changing its value ever to cause any problems
			{
				throw new AssertionError(validationException);
			}
		}
	}
*/

	protected Object createValue(final RDFObject rdfObject)
	{
		if(rdfObject instanceof RDFLiteral)
		{
			return ((RDFLiteral)rdfObject).getLexicalForm();	//TODO see if this is a typed literal, and construct other object types accordingly
		}
		throw new IllegalArgumentException("Support for component type "+rdfObject+" not yet supported.");
		
	}


	protected void constructComponent(final Component<?> component, final RDFResource resource)
	{
		final Iterator<RDFPropertyValuePair> propertyIterator=resource.getPropertyIterator();	//get an iterator to the resource properties
		while(propertyIterator.hasNext())	//while there are more properties
		{
			final RDFPropertyValuePair property=propertyIterator.next();	//get the next property
			setComponentProperty(component, property);	//set this component property
			
			
		}
	}

	protected void setComponentProperty(final Component<?> component, final RDFPropertyValuePair property)
	{
//TODO del		final RDFObject propertyValue=property.getValue();	//get the property value
		final URI propertyURI=property.getName().getReferenceURI();	//get the URI of the property
//TODO del Debug.trace("looking at property:", propertyURI);
		if(GUISE_PROPERTY_NAMESPACE_URI.equals(getNamespaceURI(propertyURI)))	//if this is a property for Guise
		{
			final Object propertyValue=createValue(property.getValue());	//get the appropriate value for the property
			final Class<?> propertyValueType=propertyValue.getClass();	//get the type of the value
			final String variableName=getLocalName(propertyURI);	//get the local name of the property
	Debug.trace("looking at property name:", variableName);
			final String setterMethodName="set"+getProperName(variableName);	//get the setter method
Debug.trace("setter: ", setterMethodName);
			final Class<?> componentClass=component.getClass();	//get the component class TODO check generic class type
			final Method[] methods=componentClass.getMethods();	//get all the class methods
			for(final Method method:methods)	//for each method
			{
				if(method.getName().equals(setterMethodName))	//if this has the setter name
				{
Debug.trace("found setter", setterMethodName);
					final Class<?>[] parameterTypes=method.getParameterTypes();	//get the parameter types for this method
					if(parameterTypes.length==1)	//if this setter has one parameter
					{
	Debug.trace("this setter has one param");
						final Class<?> parameterType=parameterTypes[0];	//get the type of the method parameter
						if(parameterType.isAssignableFrom(propertyValueType))	//if this setter is made for the property value type
						{
	Debug.trace("param has correct type:", parameterType, "ready to invoke with value:", propertyValue);
							try
							{
								method.invoke(component, propertyValue);
							} catch (IllegalArgumentException e)
							{
								Debug.error(e);
							} catch (IllegalAccessException e)
							{
								Debug.error(e);
							} catch (InvocationTargetException e)
							{
								Debug.error(e);
							}
						}
					}
				}
			}
/*TODO fix
		final RDFObject propertyValue=property.getValue();	//get the property value
		
		final Class<? extends Component<?>> componentClass=component.getClass();	//get the component class
		componentClass.getMethod(name, parameterTypes)
*/
		}
		
	}
	
}
