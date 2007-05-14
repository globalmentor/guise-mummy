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

		//standard theme labels
	public final static String LABEL_ABOUT=createStringResourceReference("theme.label.about");
	public final static String LABEL_ABOUT_X=createStringResourceReference("theme.label.about.x");
	public final static String LABEL_ACCEPT=createStringResourceReference("theme.label.accept");
	public final static String LABEL_ACCESS=createStringResourceReference("theme.label.access");
	public final static String LABEL_ADD=createStringResourceReference("theme.label.add");
	public final static String LABEL_CANCEL=createStringResourceReference("theme.label.cancel");
	public final static String LABEL_DELETE=createStringResourceReference("theme.label.delete");
	public final static String LABEL_DELETE_X=createStringResourceReference("theme.label.delete.x");
	public final static String LABEL_DOWNLOAD=createStringResourceReference("theme.label.download");
	public final static String LABEL_EDIT=createStringResourceReference("theme.label.edit");
	public final static String LABEL_FINISH=createStringResourceReference("theme.label.finish");
	public final static String LABEL_FIRST=createStringResourceReference("theme.label.first");
	public final static String LABEL_HELP=createStringResourceReference("theme.label.help");
	public final static String LABEL_HOME=createStringResourceReference("theme.label.home");
	public final static String LABEL_JOIN=createStringResourceReference("theme.label.join");
	public final static String LABEL_LAST=createStringResourceReference("theme.label.last");
	public final static String LABEL_LOGIN=createStringResourceReference("theme.label.login");
	public final static String LABEL_LOGOUT=createStringResourceReference("theme.label.logout");
	public final static String LABEL_NEXT=createStringResourceReference("theme.label.next");
	public final static String LABEL_PASSWORD=createStringResourceReference("theme.label.password");
	public final static String LABEL_PASSWORD_VERIFICATION=createStringResourceReference("theme.label.password.verification");
	public final static String LABEL_PREVIOUS=createStringResourceReference("theme.label.previous");
	public final static String LABEL_REJECT=createStringResourceReference("theme.label.reject");
	public final static String LABEL_RESOURCE=createStringResourceReference("theme.label.resource");
	public final static String LABEL_SUBMIT=createStringResourceReference("theme.label.submit");
	public final static String LABEL_SUBTRACT=createStringResourceReference("theme.label.subtract");
	public final static String LABEL_UNKNOWN=createStringResourceReference("theme.label.unknown");
	public final static String LABEL_UPLOAD=createStringResourceReference("theme.label.upload");
	public final static String LABEL_USERNAME=createStringResourceReference("theme.label.username");
	public final static String LABEL_VERSION=createStringResourceReference("theme.label.version");
		//standard theme icons
	public final static URI GLYPH_ABOUT=createURIResourceReference("theme.glyph.about");
	public final static URI GLYPH_ACCEPT=createURIResourceReference("theme.glyph.accept");
	public final static URI GLYPH_ACCEPT_MULTIPLE=createURIResourceReference("theme.glyph.accept.multiple");
	public final static URI GLYPH_ACCESS=createURIResourceReference("theme.glyph.access");
	public final static URI GLYPH_ANIMATION=createURIResourceReference("theme.glyph.animation");
	public final static URI GLYPH_ADD=createURIResourceReference("theme.glyph.add");
	public final static URI GLYPH_BLANK=createURIResourceReference("theme.glyph.blank");
	public final static URI GLYPH_BUSY=createURIResourceReference("theme.glyph.busy");
	public final static URI GLYPH_CANCEL=createURIResourceReference("theme.glyph.cancel");
	public final static URI GLYPH_DELETE=createURIResourceReference("theme.glyph.delete");
	public final static URI GLYPH_DOCUMENT=createURIResourceReference("theme.glyph.document");
	public final static URI GLYPH_DOCUMENT_CONTENT=createURIResourceReference("theme.glyph.document.content");
	public final static URI GLYPH_DOCUMENT_NEW=createURIResourceReference("theme.glyph.document.new");
	public final static URI GLYPH_DOCUMENT_PREVIEW=createURIResourceReference("theme.glyph.document.preview");
	public final static URI GLYPH_DOCUMENT_RICH_CONTENT=createURIResourceReference("theme.glyph.document.rich.content");
	public final static URI GLYPH_DOCUMENT_STACk=createURIResourceReference("theme.glyph.document.stack");
	public final static URI GLYPH_DOWNLOAD=createURIResourceReference("theme.glyph.download");
	public final static URI GLYPH_EDIT=createURIResourceReference("theme.glyph.edit");
	public final static URI GLYPH_ENTER=createURIResourceReference("theme.glyph.enter");
	public final static URI GLYPH_ERROR=createURIResourceReference("theme.glyph.error");
	public final static URI GLYPH_EXIT=createURIResourceReference("theme.glyph.exit");
	public final static URI GLYPH_EXCLAMATION=createURIResourceReference("theme.glyph.exclamation");
	public final static URI GLYPH_EYEGLASSES=createURIResourceReference("theme.glyph.eyeglasses");
	public final static URI GLYPH_FINISH=createURIResourceReference("theme.glyph.finish");
	public final static URI GLYPH_FIRST=createURIResourceReference("theme.glyph.first");
	public final static URI GLYPH_FOLDER=createURIResourceReference("theme.glyph.folder");
	public final static URI GLYPH_FOLDER_CLOSED=createURIResourceReference("theme.glyph.folder.closed");
	public final static URI GLYPH_FOLDER_OPEN=createURIResourceReference("theme.glyph.folder.open");
	public final static URI GLYPH_HELP=createURIResourceReference("theme.glyph.help");
	public final static URI GLYPH_HIDE=createURIResourceReference("theme.glyph.hide");
	public final static URI GLYPH_HIERARCHY=createURIResourceReference("theme.glyph.hierarchy");
	public final static URI GLYPH_HOME=createURIResourceReference("theme.glyph.home");
	public final static URI GLYPH_IMAGE=createURIResourceReference("theme.glyph.image");
	public final static URI GLYPH_INFO=createURIResourceReference("theme.glyph.info");
	public final static URI GLYPH_INSERT=createURIResourceReference("theme.glyph.insert");
	public final static URI GLYPH_JOIN=createURIResourceReference("theme.glyph.join");
	public final static URI GLYPH_KEY=createURIResourceReference("theme.glyph.key");
	public final static URI GLYPH_LAST=createURIResourceReference("theme.glyph.last");
	public final static URI GLYPH_LOCK_CLOSED=createURIResourceReference("theme.glyph.lock.closed");
	public final static URI GLYPH_LOCK_OPEN=createURIResourceReference("theme.glyph.lock.open");
	public final static URI GLYPH_LOGIN=createURIResourceReference("theme.glyph.login");
	public final static URI GLYPH_LOGOUT=createURIResourceReference("theme.glyph.logout");
	public final static URI GLYPH_NEXT=createURIResourceReference("theme.glyph.next");
	public final static URI GLYPH_PASSWORD=createURIResourceReference("theme.glyph.password");
	public final static URI GLYPH_POLYGON_CURVED=createURIResourceReference("theme.glyph.polygon.curved");
	public final static URI GLYPH_POLYGON_POINTS=createURIResourceReference("theme.glyph.polygon.points");
	public final static URI GLYPH_PREVIOUS=createURIResourceReference("theme.glyph.previous");
	public final static URI GLYPH_QUESTION=createURIResourceReference("theme.glyph.question");
	public final static URI GLYPH_REDO=createURIResourceReference("theme.glyph.redo");
	public final static URI GLYPH_REMOVE=createURIResourceReference("theme.glyph.remove");
	public final static URI GLYPH_REJECT=createURIResourceReference("theme.glyph.reject");
	public final static URI GLYPH_REJECT_MULTIPLE=createURIResourceReference("theme.glyph.reject.multiple");
	public final static URI GLYPH_RESOURCE=createURIResourceReference("theme.glyph.resource");
	public final static URI GLYPH_STOP=createURIResourceReference("theme.glyph.stop");
	public final static URI GLYPH_SUBMIT=createURIResourceReference("theme.glyph.submit");
	public final static URI GLYPH_SUBTRACT=createURIResourceReference("theme.glyph.subtract");
	public final static URI GLYPH_UPLOAD=createURIResourceReference("theme.glyph.upload");
	public final static URI GLYPH_USER=createURIResourceReference("theme.glyph.user");
	public final static URI GLYPH_VIEW=createURIResourceReference("theme.glyph.view");
	public final static URI GLYPH_WARN=createURIResourceReference("theme.glyph.warn");
		//standard theme messages
	public final static String MESSAGE_BUSY=createStringResourceReference("theme.message.busy");
	public final static String MESSAGE_PASSWORD_INVALID=createStringResourceReference("theme.message.password.invalid");
	public final static String MESSAGE_PASSWORD_UNVERIFIED=createStringResourceReference("theme.message.password.unverified");
	public final static String MESSAGE_TASK_SUCCESS=createStringResourceReference("theme.message.task.success");
	public final static String MESSAGE_USER_INVALID=createStringResourceReference("theme.message.user.invalid");
	public final static String MESSAGE_USER_EXISTS=createStringResourceReference("theme.message.user.exists");
	
}
