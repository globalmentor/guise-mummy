package com.guiseframework.theme;

import java.net.URI;
import java.util.Locale;

import com.garretwilson.rdf.*;
import com.guiseframework.style.*;

import static com.garretwilson.rdf.RDFUtilities.*;
import static com.garretwilson.rdf.xpackage.XMLOntologyConstants.*;
import static com.guiseframework.Guise.*;
import static com.guiseframework.Resources.*;

/**Guise theme specification.
@author Garret Wilson
*/
public class Theme extends ClassTypedRDFResource
{

	/**The resources property name; the local name of <code>http://guiseframework.com/namespaces/guise#</code>.*/
	public final static String RESOURCES_PROPERTY_NAME="resources";

	/**The theme parent, or <code>null</code> if there is no resolving parent.*/
	private Theme parent=null;

		/**@return The theme parent, or <code>null</code> if there is no resolving parent.*/
		public Theme getParent() {return parent;}

		/**Sets the theme parent.
		@param newParent The new theme parent, or <code>null</code> if there should be no resolving parent.
		*/
		public void setParent(final Theme newParent) {parent=newParent;}	//TODO maybe remove and create custom ThemeIO

	/**Default constructor.*/
	public Theme()
	{
		this(null);	//construct the class with no reference URI
	}

	/**Reference URI constructor.
	@param referenceURI The reference URI for the new resource.
	*/
	public Theme(final URI referenceURI)
	{
		super(referenceURI, GUISE_NAMESPACE_URI);  //construct the parent class
	}

	/**Retrieves the URI of resources for the given locale.
	@return The URI to resources for the given locale, or <code>null</code> if no appropriate resources property could be found.
	*/
	public URI getResourcesURI(final Locale locale)
	{
		final RDFResource rdfResource=asResource(getPropertyValue(GUISE_NAMESPACE_URI, RESOURCES_PROPERTY_NAME));	//get the resources resource, if any
		return rdfResource!=null ? rdfResource.getReferenceURI() : null;	//return the resources URI TODO search based upon locale
	}

	/**Retrieves an iterable to the XML style resources, represented by <code>x:style</code> properties.
	@return An iterable to the styles, if any.
	*/
	public Iterable<RDFResource> getStyles()
	{
		return getPropertyValues(XML_ONTOLOGY_NAMESPACE_URI, STYLE_PROPERTY_NAME, RDFResource.class); //return an iterable to style properties
	}

		//standard colors
	public final static Color<?> COLOR_SELECTED_BACKGROUND=new ResourceColor("theme.color.selected.background");

		//standard labels
	public final static String LABEL_ACCEPT=createStringResourceReference("theme.label.accept");
	public final static String LABEL_ACCESS=createStringResourceReference("theme.label.access");
	public final static String LABEL_ADD=createStringResourceReference("theme.label.add");
	public final static String LABEL_DELETE=createStringResourceReference("theme.label.delete");
	public final static String LABEL_EDIT=createStringResourceReference("theme.label.edit");
	public final static String LABEL_FINISH=createStringResourceReference("theme.label.finish");
	public final static String LABEL_FIRST=createStringResourceReference("theme.label.first");
	public final static String LABEL_HOME=createStringResourceReference("theme.label.home");
	public final static String LABEL_LAST=createStringResourceReference("theme.label.last");
	public final static String LABEL_LOGIN=createStringResourceReference("theme.label.login");
	public final static String LABEL_LOGOUT=createStringResourceReference("theme.label.logout");
	public final static String LABEL_NEXT=createStringResourceReference("theme.label.next");
	public final static String LABEL_PREVIOUS=createStringResourceReference("theme.label.previous");
	public final static String LABEL_REJECT=createStringResourceReference("theme.label.reject");
	public final static String LABEL_SUBMIT=createStringResourceReference("theme.label.submit");
	public final static String LABEL_SUBTRACT=createStringResourceReference("theme.label.subtract");
		//standard icons
	public final static URI ICON_ACCEPT=createURIResourceReference("theme.icon.accept");
	public final static URI ICON_ACCEPT_MULTIPLE=createURIResourceReference("theme.icon.accept.multiple");
	public final static URI ICON_ACCESS=createURIResourceReference("theme.icon.access");
	public final static URI ICON_ADD=createURIResourceReference("theme.icon.add");
	public final static URI ICON_BUSY=createURIResourceReference("theme.icon.busy");
	public final static URI ICON_DELETE=createURIResourceReference("theme.icon.delete");
	public final static URI ICON_DOCUMENT=createURIResourceReference("theme.icon.document");
	public final static URI ICON_DOCUMENT_CONTENT=createURIResourceReference("theme.icon.document.content");
	public final static URI ICON_DOCUMENT_NEW=createURIResourceReference("theme.icon.document.new");
	public final static URI ICON_DOCUMENT_PREVIEW=createURIResourceReference("theme.icon.document.preview");
	public final static URI ICON_DOCUMENT_RICH_CONTENT=createURIResourceReference("theme.icon.document.rich.content");
	public final static URI ICON_DOCUMENT_STACk=createURIResourceReference("theme.icon.document.stack");
	public final static URI ICON_EDIT=createURIResourceReference("theme.icon.edit");
	public final static URI ICON_ENTER=createURIResourceReference("theme.icon.enter");
	public final static URI ICON_EXIT=createURIResourceReference("theme.icon.exit");
	public final static URI ICON_EXCLAMATION=createURIResourceReference("theme.icon.exclamation");
	public final static URI ICON_EYEGLASSES=createURIResourceReference("theme.icon.eyeglasses");
	public final static URI ICON_FINISH=createURIResourceReference("theme.icon.finish");
	public final static URI ICON_FIRST=createURIResourceReference("theme.icon.first");
	public final static URI ICON_FOLDER=createURIResourceReference("theme.icon.folder");
	public final static URI ICON_FOLDER_CLOSED=createURIResourceReference("theme.icon.folder.closed");
	public final static URI ICON_FOLDER_OPEN=createURIResourceReference("theme.icon.folder.open");
	public final static URI ICON_HIDE=createURIResourceReference("theme.icon.hide");
	public final static URI ICON_HIERARCHY=createURIResourceReference("theme.icon.hierarchy");
	public final static URI ICON_HOME=createURIResourceReference("theme.icon.home");
	public final static URI ICON_IMAGE=createURIResourceReference("theme.icon.image");
	public final static URI ICON_INSERT=createURIResourceReference("theme.icon.insert");
	public final static URI ICON_KEY=createURIResourceReference("theme.icon.key");
	public final static URI ICON_LAST=createURIResourceReference("theme.icon.last");
	public final static URI ICON_LOCK_CLOSED=createURIResourceReference("theme.icon.lock.closed");
	public final static URI ICON_LOCK_OPEN=createURIResourceReference("theme.icon.lock.open");
	public final static URI ICON_LOGIN=createURIResourceReference("theme.icon.login");
	public final static URI ICON_LOGOUT=createURIResourceReference("theme.icon.logout");
	public final static URI ICON_NEXT=createURIResourceReference("theme.icon.next");
	public final static URI ICON_PREVIOUS=createURIResourceReference("theme.icon.previous");
	public final static URI ICON_QUESTION=createURIResourceReference("theme.icon.question");
	public final static URI ICON_REDO=createURIResourceReference("theme.icon.redo");
	public final static URI ICON_REMOVE=createURIResourceReference("theme.icon.remove");
	public final static URI ICON_REJECT=createURIResourceReference("theme.icon.reject");
	public final static URI ICON_REJECT_MULTIPLE=createURIResourceReference("theme.icon.reject.multiple");
	public final static URI ICON_RESOURCE=createURIResourceReference("theme.icon.resource");
	public final static URI ICON_STOP=createURIResourceReference("theme.icon.stop");
	public final static URI ICON_SUBMIT=createURIResourceReference("theme.icon.submit");
	public final static URI ICON_SUBTRACT=createURIResourceReference("theme.icon.subtract");
	public final static URI ICON_VIEW=createURIResourceReference("theme.icon.view");
		//standard messages
	public final static String MESSAGE_BUSY=createStringResourceReference("theme.message.busy");
}
