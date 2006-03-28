package com.guiseframework.test;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import com.garretwilson.rdf.RDFLiteral;
import com.garretwilson.rdf.RDFResource;
import com.garretwilson.rdf.rdfs.RDFSUtilities;
import com.garretwilson.util.Debug;
import com.globalmentor.marmot.repository.Repository;
import com.globalmentor.marmot.repository.webdav.WebDAVRepository;
import com.guiseframework.GuiseSession;
import com.guiseframework.component.*;
import com.guiseframework.event.*;
import com.guiseframework.model.Notification;
import com.guiseframework.model.RDFObjectTreeNodeModel;
import com.guiseframework.model.RDFResourceTreeNodeModel;
import com.guiseframework.validator.ValidationException;

/**Test panel for Marmot
@author Garret Wilson
*/
public class MarmotPanel extends DefaultNavigationPanel
{

	/**Guise session constructor.
	@param session The Guise session that owns this panel.
	*/
	public MarmotPanel(final GuiseSession session)
	{
		this(session, null);	//construct the component, indicating that a default ID should be used
	}

	/**ID constructor.
	@param session The Guise session that owns this panel.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	*/
	public MarmotPanel(final GuiseSession session, final String id)
	{
		super(session, id);	//construct the parent
		setLabel("Marmot Test");	//set the panel label

		final TextControl<String> textInput=new TextControl<String>(session, String.class);	//create a text input control
		textInput.setLabel("WebDAV Repository");
		textInput.setColumnCount(40);
		try
		{
			textInput.setValue("https://dav.globalmentor.com/public/");
		}
		catch(final ValidationException validationException)
		{
			throw new AssertionError(validationException);
		}
		add(textInput);
		
		final Button addButton=new Button(session);
		addButton.setLabel("Add");
		addButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(final ActionEvent actionEvent)
					{						
						final String repositoryURIString=textInput.getValue();
						if(repositoryURIString!=null)
						{
							final URI repositoryURI=URI.create(repositoryURIString);
							final Repository repository=new WebDAVRepository(repositoryURI);
							
//TODO fix							final Frame<?> frame=new DefaultFrame(session);
							
							
//TODO fix							final TreeControl treeControl=new TreeControl(session);
/*TODO fix
							final TextAreaControl textArea=new TextAreaControl(session, 10, 40);
							
final StringBuilder stringBuilder=new StringBuilder();
try
{
							final List<RDFResource> rootResources=repository.getChildResourceDescriptions(repositoryURI);
							for(final RDFResource resource:rootResources)
							{
								final RDFLiteral label=RDFSUtilities.getLabel(resource);
								if(label!=null)
								{
									stringBuilder.append(label).append("\n");
								}
							}
							textArea.setValue(stringBuilder.toString());
}
catch(final IOException ioException)
{
	Debug.error(ioException);
//TODO del	getSession().notify(new Notification(ioException));	//TODO add component-level notify, maybe
}
catch(final ValidationException validationException)
{
	Debug.error(validationException);
//TODO del	getSession().notify(new Notification(validationException));	//TODO add component-level notify, maybe
}
							frame.setContent(textArea);
*/
							final TreeControl treeControl=new TreeControl(session);
							treeControl.setTreeNodeRepresentationStrategy(RDFResource.class, new RDFResourceTreeNodeRepresentationStrategy(session));
							treeControl.setTreeNodeRepresentationStrategy(RDFLiteral.class, new RDFLiteralTreeNodeRepresentationStrategy(session));
							try
							{
									final List<RDFResource> rootResources=repository.getChildResourceDescriptions(repositoryURI);
									for(final RDFResource resource:rootResources)
									{
										treeControl.getRootNode().add(new RDFResourceTreeNodeModel(session, null, resource));
									}
							}
							catch(final IOException ioException)
							{
								Debug.error(ioException);
//							TODO del	getSession().notify(new Notification(ioException));	//TODO add component-level notify, maybe
							}
							add(treeControl);
							
//TODO fix							frame.setContent(treeControl);
							
//TODO fix							frame.open();
						}
					}
				});
		add(addButton);
	}
}
