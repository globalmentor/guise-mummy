package com.javaguise.demo;

import java.io.*;
import java.lang.reflect.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import static com.garretwilson.lang.JavaUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.checkNull;

import static com.garretwilson.net.URIConstants.*;

import static com.garretwilson.lang.ClassUtilities.*;

import com.garretwilson.rdf.*;

import static com.garretwilson.rdf.RDFUtilities.*;
import com.garretwilson.text.xml.XMLUtilities;
import com.garretwilson.util.ArrayUtilities;
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
			initializeObject(this, navigationPanelResource);
			
			
			
			
			
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
		catch(final ClassNotFoundException classNotFoundTargetException)
		{
			throw new AssertionError(classNotFoundTargetException);	//TODO fix better
		}		
		catch(final InvocationTargetException invocationTargetException)
		{
			throw new AssertionError(invocationTargetException);	//TODO fix better
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

	/**Creates and initializes an object to represent the given RDF object.
	The RDF object must be one of the following:
	<dl>
		<dt>RDFLiteral</dt> <dd>Currently returns the string version of the literal's lexical form, regardless of the literal type.</dd>
		<dt>RDFListResource</dt> <dd>Returns a {@link List} recursively containing objects defined by the list's contents.</dd>
		<dt>RDFResource</dt> <dd>Returns an object based upon the <code>java:</code> type information.
	</dl>
	@param rdfObject The RDF object describing the Java object to be created.
	@return A created and initialized object according to the given RDF description. 
 	@exception IllegalArgumentException if the given RDF object is a resource that does not specify Java type information.
 	@exception IllegalArgumentException if the given RDF object is a Java-typed resource the class of which cannot be found.
 	@exception IllegalArgumentException if the given RDF object indicates a Java class that has no appropriate constructor.
 	@exception IllegalArgumentException if the given RDF object indicates a Java class that is an interface or an abstract class.
 	@exception IllegalArgumentException if the given RDF object indicates a Java class the constructor of which is not accessible.
	@exception InvocationTargetException if the given RDF object indicates a Java class the constructor of which throws an exception.
	*/
	protected Object createObject(final RDFObject rdfObject) throws InvocationTargetException
	{
		if(rdfObject instanceof RDFLiteral)	//if the object is a literal
		{
			return ((RDFLiteral)rdfObject).getLexicalForm();	//TODO see if this is a typed literal, and construct other object types accordingly
		}
		else if(rdfObject instanceof RDFListResource)	//if the object is a list
		{
			final RDFListResource rdfListResource=(RDFListResource)rdfObject;	//cast the object to a list
			final List<Object> list=new ArrayList<Object>(rdfListResource.size());	//create a new list of the correct size
			for(final RDFResource rdfListItem:rdfListResource)	//for each RDF resource in the list
			{
				list.add(createObject(rdfListItem));	//create an object from this RDF list item and add it to our list
			}
			return list;	//return the list of objects we created
		}
		else if(rdfObject instanceof RDFResource)	//if the object is a resource
		{
			final RDFResource resource=(RDFResource)rdfObject;	//cast the object to a resource
			String valueClassName=null;	//we'll try to find a Java class name part of the type
//			RDFResource typeResource=null;	//we'll try to find a java: type
			final Iterator<RDFObject> typeIterator=getTypeIterator(resource);	//get an iterator to all the resource types
			while(valueClassName==null && typeIterator.hasNext())	//while we haven't found a type URI and there are other types left
			{
				final RDFObject typeResource=typeIterator.next();	//get the next type
				if(typeResource instanceof RDFResource)	//if the type is an RDF resource (it always should be)
				{
					final URI typeURI=((RDFResource)typeResource).getReferenceURI();	//get the type URI
					if(JAVA_SCHEME.equals(typeURI.getScheme()))	//if the type is a Java type
					{
						valueClassName=typeURI.getSchemeSpecificPart();	//get the class name part of the type
					}
				}
			}
			if(valueClassName==null)	//if we don't know the type of the resource
			{
				throw new IllegalArgumentException("Value resource "+resource+" missing type information.");
			}
Debug.trace("Loading class", valueClassName);
//TODO del			final Object value;	//we'll determine the value by invoking the constructor
			try
			{
				final Class<?> valueClass=Class.forName(valueClassName);	//load the class
				final Map<URI, PropertyDescription> propertyDescriptionMap=getPropertyDescriptionMap(valueClass, resource);	//get the property descriptions from the resource description
				final List<PropertyDescription> readOnlyProperties=new ArrayList<PropertyDescription>(propertyDescriptionMap.size());	//the set of read-only properties, which we may use in the constructor
				for(final PropertyDescription propertyDescription:propertyDescriptionMap.values())	//for each property description
				{
					if(propertyDescription.getSetter()==null)	//if there is no setter for this property, it is a read-only property; save it in case we can use it for the constructor
					{
						readOnlyProperties.add(propertyDescription);	//add this property ot the list of read-only properties
					}
				}
				readOnlyProperties.add(new PropertyDescription(RDFUtilities.createReferenceURI(GUISE_PROPERTY_NAMESPACE_URI, "session"), GuiseSession.class, getSession()));	//artificially popuplate the read-only property with a session TODO refactor this to allow such variables to be specfified in a general way
				final Constructor[] constructors=valueClass.getConstructors();	//get all available constructors
				int maxParameterCount=0;	//we'll determine the maximum number of parameters available
				for(final Constructor constructor:constructors)	//look at each constuctor to find one with the correct number of parameters
				{
					final Class<?>[] parameterTypes=constructor.getParameterTypes();	//get the parameter types for this constructor
					final int parameterCount=parameterTypes.length;	//see how how many parameters this constructor has
					if(parameterCount>maxParameterCount)	//if this parameter count is more than we know about
					{
						maxParameterCount=parameterCount;	//update the maximum parameter count
					}
				}
Debug.trace("ready to create object of type", valueClassName, "with constructors with max parameters", maxParameterCount);
				for(int parameterCount=0; parameterCount<=maxParameterCount; ++parameterCount)	//find a constructor with the least number of parameters, starting with the default constructor, until we exhaust the available constructors
				{
					for(final Constructor constructor:constructors)	//look at each constuctor to find one with the correct number of parameters
					{
						final Class<?>[] parameterTypes=constructor.getParameterTypes();	//get the parameter types for this constructor
						if(parameterTypes.length==parameterCount)	//if this constructor has the correct number of parameters
						{
Debug.trace("Looking at constructor with parameter count:", parameterCount);
							boolean foundArguments=true;	//start out by assuming the parameters match
							final Object[] arguments=new Object[parameterCount];	//create an array sufficient for the arguments
							for(int parameterIndex=0; parameterIndex<parameterCount && foundArguments; ++parameterIndex)	//for each parameter, as long we we have matching parameters
							{
								final Class<?> parameterType=parameterTypes[parameterIndex];	//get this parameter type
Debug.trace("Parameter", parameterIndex, "type: ", parameterType);
								boolean foundArgument=false;	//we'll try to find an argument
								for(final PropertyDescription propertyDescription:readOnlyProperties)	//look at all the properties to find one for this parameter
								{
Debug.trace("checking read-only property:", propertyDescription.getPropertyClass());
									if(parameterType.isAssignableFrom(propertyDescription.getPropertyClass()))	//if this read-only property will work for this constructor
									{
Debug.trace("matches!");
										arguments[parameterIndex]=propertyDescription.getValue();	//use this read-only property in the constructor
										foundArgument=true;	//show that we found an argument
										break;	//stop looking for the argument
									}
								}
								if(!foundArgument)	//if we couldn't find an argument for this parameter
								{
									foundArguments=false;	//indicate that parameters don't match for this constructor
								}
							}
							if(foundArguments)	//if we found a constructor for which we have arguments
							{
								try
								{
Debug.trace("found constructor with the following arguments:", ArrayUtilities.toString(arguments));
									final Object object=constructor.newInstance(arguments);	//invoke the constructor with the arguments
									initializeObject(object, propertyDescriptionMap);	//initialize the object with the properties
									return object;	//return the constructed and initialized object
								}
/*TODO del; we're now allowing more arguments than just the session
								catch(final IllegalArgumentException illegalArgumentException)	//our Guise session should always work OK---and we shouldn't get this exception for the default constructor
								{
									throw new AssertionError(illegalArgumentException);
								}
*/
								catch(final InstantiationException instantiationException)
								{
									throw new IllegalArgumentException(instantiationException);
								}
								catch(final IllegalAccessException illegalAccessException)
								{
									throw new IllegalArgumentException(illegalAccessException);
								}
							}
						}
					}
				}
				throw new IllegalArgumentException("Value class "+valueClassName+" does not have a constructor appropriate for the available read-only properties.");
			}
			catch(final ClassNotFoundException classNotFoundException)	//if we couldn't find the class
			{
				throw new IllegalArgumentException(classNotFoundException);
			}
		}
		else	//if the object is neither a literal nor a resource
		{
			throw new AssertionError("Unknown RDF object type: "+rdfObject.getClass());
		}
	}

	/**Initializes an object based upon the given description.
	@param object The object to initialize.
	@param resource The description for the object.
	@exception ClassNotFoundException if a class was specified and the indicated class cannot be found.
	@exception InvocationTargetException if the given RDF object indicates a Java class the constructor of which throws an exception.
	@see #initializeObject(Object, Map)
	*/
	protected void initializeObject(final Object object, final RDFResource resource) throws ClassNotFoundException, InvocationTargetException
	{
		final Map<URI, PropertyDescription> propertyDescriptionMap=getPropertyDescriptionMap(object.getClass(), resource);	//get property descriptions from the resource description
		initializeObject(object, propertyDescriptionMap);	//initialize the object from the property descriptions
	}

	/**Initializes an object based upon the given property descriptions.
	@param object The object to initialize.
	@param propertyDescriptionMap The property descriptions fo initializing the object.
	@exception InvocationTargetException if the given RDF object indicates a Java class the constructor of which throws an exception.
	*/
	protected void initializeObject(final Object object, final Map<URI, PropertyDescription> propertyDescriptionMap) throws InvocationTargetException
	{
		for(final PropertyDescription propertyDescription:propertyDescriptionMap.values())	//for each property description
		{
			final Method setter=propertyDescription.getSetter();	//get the setter method for this property
			if(setter!=null)	//if there is a setter for this property
			{
				try
				{
					setter.invoke(object, propertyDescription.getValue());	//invoke the setter
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

	/**Sets a property of the object based upon the given RDF property/value pair.
	If the property isn't recognized as relevant to Guise, or the object cannot set the property, no action is taken.
	@param object The component being constructed.
	@param property The RDF property/value pair potentially representing a Guise object property.
	@exception InvocationTargetException if the given RDF object indicates a Java class the constructor of which throws an exception.
	@exception IllegalArgumentException if a string given for an enum property value does not match any of the enum's values.
	*/
/*TODO del when works
	protected void setObjectProperty(final Object object, final RDFPropertyValuePair property) throws InvocationTargetException
	{
//TODO del		final RDFObject propertyValue=property.getValue();	//get the property value
		final URI propertyURI=property.getName().getReferenceURI();	//get the URI of the property
//TODO del Debug.trace("looking at property:", propertyURI);
		if(GUISE_PROPERTY_NAMESPACE_URI.equals(getNamespaceURI(propertyURI)))	//if this is a property for Guise
		{
			final RDFObject propertyValueRDFObject=property.getValue();	//get the property value
			final Object propertyValue=createValue(propertyValueRDFObject);	//get the appropriate value for the property
			final Class<?> propertyValueType=propertyValue.getClass();	//get the type of the value
			final String variableName=getLocalName(propertyURI);	//get the local name of the property
	Debug.trace("looking at property name:", variableName);
			final String setterMethodName="set"+getProperName(variableName);	//get the setter method
Debug.trace("setter: ", setterMethodName);
			final Class<?> objectClass=object.getClass();	//get the object class TODO check generic class type
			final Method[] methods=objectClass.getMethods();	//get all the class methods
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
						final Object parameter=convertObject(propertyValue, parameterType);	//convert the object to the correct type
						if(parameter!=null)	//if we found a parameter to use for this method
						{
							Debug.trace("param has correct type:", parameterType, "ready to invoke with value:", parameter);
							try
							{
								method.invoke(object, parameter);
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
		}
	}
*/
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**Constructs a map of property descriptions for a class.
	If there are duplicate properties, only one will be stored.
	@param objectClass The class of the object to be constructed.
	@param resource The description fo the object.
	@return A map of property descriptions keyed to property URIs.
	@exception ClassNotFoundException if a class was specified and the indicated class cannot be found.
	@exception InvocationTargetException if the given RDF object indicates a Java class the constructor of which throws an exception.
	*/
	protected Map<URI, PropertyDescription> getPropertyDescriptionMap(final Class<?> objectClass, final RDFResource resource) throws ClassNotFoundException, InvocationTargetException
	{
		final Map<URI, PropertyDescription> propertyDescriptionMap=new HashMap<URI, PropertyDescription>(resource.getPropertyCount());	//create a map to hold property descriptions, with a least enough capacity to hold descriptions for all properties
		final Iterator<RDFPropertyValuePair> propertyIterator=resource.getPropertyIterator();	//get an iterator to the resource properties
		while(propertyIterator.hasNext())	//while there are more properties
		{
			final RDFPropertyValuePair property=propertyIterator.next();	//get the next property
			final PropertyDescription propertyDescription=getPropertyDescription(objectClass, property);	//get a description for this property
			if(propertyDescription!=null)	//if this was a recognized property
			{
				propertyDescriptionMap.put(propertyDescription.getPropertyURI(), propertyDescription);	//store this property description in the map
			}
		}
		return propertyDescriptionMap;	//return the property description map
	}
	
	/**Gets a description of a property of the object based upon the given RDF property/value pair.
	The returned property description will indicate a method if the property is settable.
	@param objectClass The class of the object to be constructed.
	@param property The RDF property/value pair potentially representing a Guise object property.
	@return A description of the property, or <code>null</code> if the property is not recognized.
	@exception ClassNotFoundException if a class was specified and the indicated class cannot be found.
	*/
	protected PropertyDescription getPropertyDescription(final Class<?> objectClass, final RDFPropertyValuePair property) throws ClassNotFoundException, InvocationTargetException
	{
//TODO del		final RDFObject propertyValue=property.getValue();	//get the property value
		final URI propertyURI=property.getName().getReferenceURI();	//get the URI of the property
//TODO del Debug.trace("looking at property:", propertyURI);
		if(GUISE_PROPERTY_NAMESPACE_URI.equals(getNamespaceURI(propertyURI)))	//if this is a property for Guise
		{
			final RDFObject propertyValueRDFObject=property.getValue();	//get the property value
			final Object propertyValue=createObject(propertyValueRDFObject);	//get the appropriate value for the property TODO get the type and save it somewhere, because this may return null
			final Class<?> propertyValueType=propertyValue.getClass();	//get the type of the value
			final String variableName=getLocalName(propertyURI);	//get the local name of the property
	Debug.trace("looking at property name:", variableName);
			final String properName=getProperName(variableName);
			final String setterMethodName="set"+properName;	//get the setter method TODO use a constant
Debug.trace("setter: ", setterMethodName);
				//try to find a compatible setter method
			final Method[] methods=objectClass.getMethods();	//get all the class methods
			for(final Method method:methods)	//for each method
			{
				if(method.getName().equals(setterMethodName))	//if this has the setter name
				{
Debug.trace("found setter", setterMethodName);
					final Class<?>[] parameterTypes=method.getParameterTypes();	//get the parameter types for this method
					if(parameterTypes.length==1)	//if this setter has one parameter
					{
	Debug.trace("this setter has one param");
						final Class<?> parameterType=parameterTypes[0];	//get the single parameter type
						final Object value=convertObject(propertyValue, parameterType);	//convert the object to the correct type
						if(value!=null)	//if we found a parameter to use for this method
						{
							Debug.trace("property value has correct type for setter:", parameterType, "property value:", value);
							return new PropertyDescription(propertyURI, parameterType, value, method);	//return a description of this property with the method and parameter
						}
					}
				}
			}
				//if no setter could be found, try to find a getter method to verify this is a property that can be set
			final String getterMethodName="get"+properName;	//get the getter method TODO use a constant
Debug.trace("getter: ", getterMethodName);
			for(final Method method:methods)	//for each method
			{
				if(method.getName().equals(getterMethodName) && method.getParameterTypes().length==0)	//if this has the getter name and no parameters
				{
					final Class<?> returnType=method.getReturnType();	//get the return type of the getter
Debug.trace("found getter", getterMethodName, "for class", objectClass, "with return type", returnType);
					final Object value=convertObject(propertyValue, returnType);	//convert the object to the getter return type, if we can
					if(value!=null)	//if we can convert the property value to the getter return type
					{
						Debug.trace("property value has correct type for getter:", returnType, "property value:", value);
						return new PropertyDescription(propertyURI, value!=null ? value.getClass() : returnType, value);	//return a description of this property with just the value TODO see why covariant return types aren't working correctly; for now, we'll just get the value type directly
					}
				}
			}
		}
		return null;	//indicate that we don't recognize this property
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**Converts an object to the correct type.
	If the object is already of the correct type, no action occurs.
	Strings can be converted to the following types of objects:
	<ul>
		<li>{@link Enum}</li>
		<li>{@link Class}</li>
	</ul>
	@param object The object to convert
	@param requiredType The required type of the object.
	@return The object as the required type, or <code>null</code> if the object cannot be converted to the required type.
	@exception ClassNotFoundException if the required type is {@link Class} and the indicated class cannot be found.
	*/
	protected static Object convertObject(final Object object, final Class<?> requiredType) throws ClassNotFoundException
	{
		final Class<?> objectType=object.getClass();	//get the type of the object
		if(requiredType.isAssignableFrom(objectType))	//if we expect this type (this algorithm could be improved to first try to find an exact match and then find a convertible match)
		{
			Debug.trace("object has correct type:", requiredType);
			return object;	//use the object as-is
		}
		else	//if we expect for another object type
		{
			if(object instanceof String)	//if the object is a string, see if we can convert it to the correct type
			{
				final String stringObject=(String)object;	//cast the value to a string
				if(Enum.class.isAssignableFrom(requiredType))	//if the required type is an enumeration
				{
Debug.trace("Creating enum of type", requiredType);
					return Enum.valueOf((Class<? extends Enum>)requiredType, stringObject);	//TODO check for an IllegalArgumentException here
				}
				else if(Class.class.isAssignableFrom(requiredType))	//if the required type is a class
				{
					return Class.forName(stringObject);	//load the given class
				}
			}
		}
		return null;	//indicate we couldn't get an object of the correct type
	}

	/**Property information for an object's property.
	The information indicates the value of a property, and may also indicate a method for setting the given property.
	@author Garret Wilson
	 */
	protected static class PropertyDescription
	{

		/**The URI identifying the property.*/
		private final URI propertyURI;	//TODO probably remove this

			/**@return The URI identifying the property.*/
			public URI getPropertyURI() {return propertyURI;}

		/**The class representing the property type.*/
		private final Class<?> propertyClass;

			/**@return The class representing the property type.*/
			public Class<?> getPropertyClass() {return propertyClass;}

		/**The property value.*/
		private final Object value;

			/**@return The property value.*/
			public Object getValue() {return value;}

		/**The setter method to be invoked, or <code>null</code> if no setting method is known.*/
		private final Method setter;

			/**@return The setter method to be invoked, or <code>null</code> if no setting method is known.*/
			public Method getSetter() {return setter;}

		/**Value constructor.
		@param propertyURI The URI identifying the property.
		@param propertyClass The class representing the property type.
		@param value The property value.
		@exception NullPointerException if the given property URI and/or property class is <code>null</code>.
		*/
		public PropertyDescription(final URI propertyURI, final Class<?> propertyClass, final Object value)
		{
			this(propertyURI, propertyClass, value, null);	//construct the class with no setter
		}

		/**Setter and value constructor.
		@param propertyURI The URI identifying the property.
		@param setter The setter method to be invoked, or <code>null</code> if no setting method is known.
		@param value The property value.
		@exception NullPointerException if the given property URI and/or property class is <code>null</code>.
		*/
		public PropertyDescription(final URI propertyURI, final Class<?> propertyClass, final Object value, final Method setter)
		{
			this.propertyURI=checkNull(propertyURI, "Property URI cannot be null.");
			this.propertyClass=checkNull(propertyClass, "Property class cannot be null.");
			this.setter=setter;
			this.value=value;
		}
		
	}
	
}
