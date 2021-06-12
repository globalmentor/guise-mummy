/*
 * Copyright © 2005-2012 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.guise.framework.platform.web;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import com.globalmentor.net.MediaType;

import io.guise.framework.component.*;
import io.guise.framework.converter.*;
import io.guise.framework.model.Notification;
import io.guise.framework.platform.*;

import static com.globalmentor.text.Text.*;
import static com.globalmentor.java.Conditions.*;
import static com.globalmentor.java.Objects.*;
import static com.globalmentor.html.spec.HTML.*;
import static io.guise.framework.platform.web.GuiseCSSStyleConstants.*;
import static io.guise.framework.platform.web.WebPlatform.*;

/**
 * Strategy for rendering a text control as an XHTML <code>&lt;input&gt;</code> element or an XHTML <code>&lt;textarea&gt;</code> element. This view will change
 * the XHTML element rendered based upon the number of rows requested by the text control. This implementation automatically converts between the controls LF
 * end-of-line representation and the CRLF required by HTML &lt;textarea&gt;.
 * @param <V> The type of value represented in the control.
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public class WebTextControlDepictor<V, C extends TextControl<V>> extends AbstractDecoratedWebComponentDepictor<C> {

	/** The media type for XStandard objects. */
	public static final MediaType XSTANDARD_MEDIA_TYPE = MediaType.of(MediaType.APPLICATION_PRIMARY_TYPE, "x-xstandard");

	/** The XStandard class ID. */
	public static final String XSTANDARD_CLASS_ID = "clsid:0EED7206-1661-11D7-84A3-00606744831D";

	/** Default constructor using the XHTML <code>&lt;input&gt;</code> element. */
	public WebTextControlDepictor() {
		super(XHTML_NAMESPACE_URI, ELEMENT_INPUT, true); //represent <xhtml:input>, allowing an empty element if possible; this may be modified at render time based upon whether an <input> or or <textarea> element is being used
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version returns the local name of the XHTML <code>&lt;input&gt;</code> element if exactly one row is specified, otherwise the local name of the XHTML
	 * <code>&lt;textarea&gt;</code>.
	 * </p>
	 */
	@Override
	public String getBodyLocalName() {
		final C component = getDepictedObject(); //get the component being depicted
		/*TODO XStandard
				if(isHTML(component.getValueContentType())) {	//if the content is HTML, use an <object> tag for XStandard
					return ELEMENT_OBJECT;
				}
		*/
		return component.getRowCount() == 1 ? ELEMENT_INPUT : ELEMENT_TEXTAREA; //if something besides one row is requested (such as no rows), use a text area
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version returns <code>true</code> for <code>&lt;input&gt;</code> and <code>false</code> for <code>&lt;textarea&gt;</code>.
	 * </p>
	 * @see #getBodyLocalName()
	 */
	@Override
	public boolean isEmptyBodyElementAllowed() {
		return ELEMENT_INPUT.equals(getBodyLocalName()); //only allow an empty element for <xhtml:input> (IE6 and IE7 breaks on an empty <textarea> element)
	}

	/**
	 * Determines the XHTML input type to use for getting input from the user.
	 * @return One of the XHTML input types.
	 */
	protected String getInputType() {
		return getDepictedObject().isMasked() ? INPUT_TYPE_PASSWORD : INPUT_TYPE_TEXT; //if this is a masked component, use a password input 
	}

	@Override
	public void processEvent(final PlatformEvent event) {
		if(event instanceof WebChangeDepictEvent) { //if a property changed
			final WebChangeDepictEvent webChangeEvent = (WebChangeDepictEvent)event; //get the web change event
			final C component = getDepictedObject(); //get the depicted object
			if(webChangeEvent.getDepictedObject() != component) { //if the event was meant for another depicted object
				throw new IllegalArgumentException("Depict event " + event + " meant for depicted object " + webChangeEvent.getDepictedObject());
			}
			final Map<String, Object> properties = webChangeEvent.getProperties(); //get the new properties
			String valueText = null; //we'll get a new value to use if needed
			String provisionalText = asInstance(properties.get("provisionalValue"), String.class).orElse(null); //get the provisional value, if any; a provisional value will never be null TODO use a constant
			if(provisionalText != null) { //if there is a provisional value
				provisionalText = normalizeEol(provisionalText, LINE_FEED_STRING).toString(); //normalize the provisional text to LF ends of lines
				final Pattern autoCommitPattern = component.getAutoCommitPattern(); //get the auto-commit pattern, if any
				if(autoCommitPattern != null && autoCommitPattern.matcher(provisionalText).matches()) { //if there is an auto-commit pattern and the text patches that pattern
					valueText = provisionalText; //don't make this provisional; go ahead and commit the change
				} else { //if we have a new provisional value
					component.setNotification(null); //clear the component errors; this method may generate new errors if the change is not provisional TODO do we want this here?
					component.setProvisionalText(provisionalText); //update the provisional literal text of the component
				}
			}
			final boolean valueSpecified = properties.containsKey("value"); //see if a value was specified TODO use a constant
			if(valueSpecified || valueText != null) { //if a value was specified, or we have a provisional value to commit
				if(valueSpecified) { //if a value was specified, it will always override any specified provisional value
					valueText = asInstance(properties.get("value"), String.class).orElse(null); //get the new value; this will incorrectly use a new value of null if the given value isn't a string TODO use a constant					
					valueText = normalizeEol(valueText, LINE_FEED_STRING).toString(); //normalize the value text to LF ends of lines
				}
				component.setNotification(null); //clear the component errors; this method may generate new errors if the change is not provisional
				try {
					component.setTextValue(valueText); //update the literal text of the component, which will in turn update the provisional text of the component, and then update the value
				} catch(final ConversionException conversionException) { //if there is a conversion error
					component.setNotification(new Notification(conversionException)); //add this error to the component
				} catch(final PropertyVetoException propertyVetoException) { //if there is a veto
					final Throwable cause = propertyVetoException.getCause(); //get the cause of the veto, if any
					component.setNotification(new Notification(cause != null ? cause : propertyVetoException)); //add notification of the error to the component
				}
			}
		} else if(event instanceof WebFormEvent) { //if this is a form submission
			final WebFormEvent formEvent = (WebFormEvent)event; //get the form submit event
			final C component = getDepictedObject(); //get the component
			final String componentName = getDepictName(); //get the component's name
			if(componentName != null) { //if there is a component name
				asInstance(formEvent.getParameterListMap().getItem(componentName), String.class) //get the form value for this control
						.ifPresent(text -> { //if there was a parameter value for this component
							component.setNotification(null); //clear the component errors; this method may generate new errors if the change is not provisional
							try {
								component.setTextValue(text); //update the literal text of the component, which will in turn update the provisional text of the component, and then update the value
							} catch(final ConversionException conversionException) { //if there is a conversion error
								component.setNotification(new Notification(conversionException)); //add this error to the component
							} catch(final PropertyVetoException propertyVetoException) { //if there is a veto
								final Throwable cause = propertyVetoException.getCause(); //get the cause of the veto, if any
								component.setNotification(new Notification(cause != null ? cause : propertyVetoException)); //add notification of the error to the component
							}
						});
			}
		}
		super.processEvent(event); //do the default event processing
	}

	@Override
	protected void depictBegin() throws IOException {
		super.depictBegin(); //do the default beginning rendering
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final C component = getDepictedObject(); //get the component
		depictContext.writeAttribute(null, ATTRIBUTE_NAME, getPlatform().getDepictIDString(component.getDepictID())); //write the component ID in the XHTML name attribute
		if(!component.isEnabled()) { //if the component's is not enabled
			depictContext.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_DISABLED, INPUT_DISABLED_DISABLED); //disabled="disabled"			
		}
		if(!component.isEditable()) { //if the component's is not editable
			depictContext.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_READONLY, INPUT_READONLY_READONLY); //readonly="readonly"			
		}
		final int columnCount = component.getColumnCount(); //get the column count
		final String bodyLocalName = getBodyLocalName(); //see which type of element we are rendering
		if(ELEMENT_INPUT.equals(bodyLocalName)) { //if we are rendering an <xhtml:input>
			depictContext.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_TYPE, getInputType()); //type="inputType"
			final int maxLength = component.getMaximumLength(); //get the maximum length
			if(maxLength >= 0) { //if a maximum length is given
				depictContext.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_MAXLENGTH, Integer.toString(maxLength)); //maxlength="maxLength"							
			}
			if(columnCount >= 0) { //if a valid column count is given
				depictContext.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_SIZE, Integer.toString(columnCount)); //size="columnCount"							
			}
			final String text = component.getProvisionalText(); //see what string we should use for the XHTML value attribute (the provisional text represents the most recent text we know about)
			if(text != null) { //if there is a value
				depictContext.writeAttribute(null, ATTRIBUTE_VALUE, text); //value="encodedValue"			
			}
			//check to see if this is just a provisional AJAX update, in which case we should tell the browser not to update the the value
			//this is a provisional AJAX update if the provisionalText property is modified but the text property is not, and the general update property is not set
			final Set<String> modifiedProperties = getModifiedProperties(); //get the modified properties
			if(!modifiedProperties.contains(GENERAL_PROPERTY) //if a general modification did not occur
					&& modifiedProperties.contains(TextControl.PROVISIONAL_TEXT_PROPERTY) //and if the text was provisionally modified
					&& !modifiedProperties.contains(TextControl.TEXT_PROPERTY)) { //but the actual text was not modified, this was just a provisional value update; notify the browser not to patch in the new value
				depictContext.writeAttribute(GUISE_ML_NAMESPACE_URI, ATTRIBUTE_PATCH_TYPE, ATTRIBUTE_PATCH_TYPE_NO_VALUE); //guise:patchType="novalue"
			}
		} else if(ELEMENT_TEXTAREA.equals(bodyLocalName)) { //if we are rendering an <xhtml:textarea>
			final int rowCount = component.getRowCount(); //get the row count
			if(rowCount >= 0) { //if a valid row count is given
				depictContext.writeAttribute(null, ELEMENT_TEXTAREA_ATTRIBUTE_ROWS, Integer.toString(rowCount)); //rows="rowCount"							
			}
			if(columnCount >= 0) { //if a valid column count is given
				depictContext.writeAttribute(null, ELEMENT_TEXTAREA_ATTRIBUTE_COLS, Integer.toString(columnCount)); //cols="columnCount"							
			}
			final boolean lineWrap = component.isLineWrap(); //see if we should wrap lines
			depictContext.writeAttribute(null, ELEMENT_TEXTAREA_ATTRIBUTE_WRAP, lineWrap ? TEXTAREA_WRAP_SOFT : TEXTAREA_WRAP_OFF); //wrap="soft|off"
			final boolean multiline = component.isMultiline(); //see if we should allow multiple lines of input
			depictContext.writeAttribute(GUISE_ML_NAMESPACE_URI, ELEMENT_TEXTAREA_ATTRIBUTE_MULTILINE, Boolean.toString(multiline)); //guise:multiline="true|false"
			final MediaType valueContentType = component.getValueContentType(); //get the content type of the value
			depictContext.writeAttribute(GUISE_ML_NAMESPACE_URI, ATTRIBUTE_CONTENT_TYPE, valueContentType.toBaseTypeString()); //guise:contentType="valueContentType"
			if(isHTML(valueContentType)) { //if the content is HTML
				depictContext.writeAttribute(GUISE_ML_NAMESPACE_URI, ATTRIBUTE_PATCH_TYPE, ATTRIBUTE_PATCH_TYPE_NONE); //don't do any patching on the text area itself; rely on events instead
			}
		}
		/*TODO XStandard
				else if(ELEMENT_OBJECT.equals(bodyLocalName)) {	//if we are rendering an <xhtml:object> (XStandard)
						//if IE7 has not had Security Update MS07-057 (KB939653) applied, it will not auto-install from the type and will require a classid (see http://xstandard.com/en/support/ie-7-auto-install-workaround/ )
					if(getPlatform().getClientProduct().isBrandVersionNumber(WebUserAgentProduct.Brand.INTERNET_EXPLORER, 7)) {	//if this is IE7
						depictContext.writeAttribute(null, ELEMENT_OBJECT_ATTRIBUTE_CLASSID, XSTANDARD_CLASS_ID);	//classid="clsid:0EED7206-1661-11D7-84A3-00606744831D"; only write the classid attributes for IE 7
							//depict the codebase URI to the CAB file
						depictContext.writeAttribute(null, ELEMENT_OBJECT_ATTRIBUTE_CODEBASE, GuiseApplication.GUISE_ASSETS_CABS_PATH.resolve("xstandard/XStandard.cab").toString()+"#Version=2,0,0,0");	//codebase="...assets/cabs/xstandard/XStandard.cab#Version=2,0,0,0" TODO use constant; allow version to be specified
					}
					else {	//if the user agent is not IE7, specify the object content type
						depictContext.writeAttribute(null, ELEMENT_OBJECT_ATTRIBUTE_TYPE, XSTANDARD_CONTENT_TYPE.toString());	//type="application/x-xstandard"
					}
					final String text=component.getProvisionalText();	//see what string we should use for the XHTML value attribute (the provisional text represents the most recent text we know about)
					if(text!=null) {	//if there is a value
						depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_PARAM, true);	//<xhtml:param>
						depictContext.writeAttribute(null, ELEMENT_PARAM_ATTRIBUTE_NAME, "Value");	//name="Value" TODO use a constant
						depictContext.writeAttribute(null, ELEMENT_PARAM_ATTRIBUTE_VALUE, text);	//value="text"
						depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_PARAM);	//</xhtml:param>
						depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_OBJECT);	//</xhtml:object>
					}
				}
		*/
		else { //if we don't recognize the type of element we are rendering
			throw new AssertionError("Unrecognized element local name: " + bodyLocalName);
		}
		//TODO del Log.trace("*****getting ready to update text input view; modified properties are:", getModifiedProperties());
	}

	@Override
	protected void depictBody() throws IOException {
		super.depictBody(); //render the default main part of the component
		final String bodyLocalName = getBodyLocalName(); //see which type of element we are rendering
		if(ELEMENT_TEXTAREA.equals(getBodyLocalName())/*TODO del || ELEMENT_DIV.equals(getBodyLocalName())*/) //if we are rendering an <xhtml:textarea> or an <xhtml:div>
		{
			final String text = getDepictedObject().getText(); //see what literal text representation we are using
			if(text != null) { //if there is a value
				getDepictContext().write(normalizeEol(text, CRLF_STRING).toString()); //normalize the text ends of lines to CRLF and write the value			
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version writes a dummy element for rich text editing if needed.
	 * </p>
	 * @see #writeErrorMessage()
	 */
	@Override
	protected void writeDecoratorEnd() throws IOException {
		if(isHTML(getDepictedObject().getValueContentType())) { //if the content is HTML, create the dummy elements needed by the client-side editor
			final WebDepictContext depictContext = getDepictContext(); //get the depict context
			switch(HTML_EDITOR) { //see which editor we're using
				case CKEDITOR:
					depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_SPAN); //<xhtml:span>
					depictContext.writeAttribute(null, ATTRIBUTE_ID,
							"cke_" + decorateID(getPlatform().getDepictIDString(getDepictedObject().getDepictID()), null, COMPONENT_BODY_CLASS_SUFFIX)); //write the ID that will be used by the generated component for the editor
					depictContext.writeAttribute(GUISE_ML_NAMESPACE_URI, ATTRIBUTE_PATCH_TYPE, ATTRIBUTE_PATCH_TYPE_TEMP); //guise:patchType="temp"; this is just a fill-in for the real content that gets placed later
					depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_SPAN); //</xhtml:span>
					break;
				case TINY_MCE:
					depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_SPAN); //<xhtml:span>
					depictContext.writeAttribute(null, ATTRIBUTE_ID,
							decorateID(getPlatform().getDepictIDString(getDepictedObject().getDepictID()), null, COMPONENT_BODY_CLASS_SUFFIX + "_parent")); //write the ID that will be used by the generated component for the editor
					depictContext.writeAttribute(GUISE_ML_NAMESPACE_URI, ATTRIBUTE_PATCH_TYPE, ATTRIBUTE_PATCH_TYPE_TEMP); //guise:patchType="temp"; this is just a fill-in for the real content that gets placed later
					depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_SPAN); //</xhtml:span>
					break;
				default:
					throw impossible();
			}
		}
		super.writeDecoratorEnd(); //write the ending components
	}

}
