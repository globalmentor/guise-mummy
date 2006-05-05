package com.guiseframework.test;

import java.beans.PropertyVetoException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import com.garretwilson.model.ResourceModel;
import com.garretwilson.rdf.RDFLiteral;
import com.garretwilson.rdf.RDFResource;
import com.garretwilson.rdf.RDFUtilities;
import com.garretwilson.rdf.maqro.Activity;
import com.garretwilson.rdf.maqro.ActivityModelIOKit;
import com.garretwilson.rdf.rdfs.RDFSUtilities;
import com.garretwilson.util.Debug;
import com.globalmentor.marmot.DefaultMarmotSession;
import com.globalmentor.marmot.MarmotSession;
import com.globalmentor.marmot.guise.repository.RepositoryResourceTreeNodeModel;
import com.globalmentor.marmot.guise.repository.RepositoryResourceTreeNodeRepresentationStrategy;
import com.globalmentor.marmot.repository.Repository;
import com.globalmentor.marmot.repository.webdav.WebDAVRepository;
import com.globalmentor.marmot.resource.DefaultResourceKit;
import com.globalmentor.marmot.resource.folder.FolderResourceKit;
import com.globalmentor.marmot.resource.image.ImageResourceKit;
import com.guiseframework.GuiseSession;
import com.guiseframework.component.*;
import com.guiseframework.component.rdf.RDFLiteralTreeNodeRepresentationStrategy;
import com.guiseframework.component.rdf.RDFResourceTreeNodeRepresentationStrategy;
import com.guiseframework.event.*;
import com.guiseframework.model.Notification;
import com.guiseframework.model.rdf.RDFObjectTreeNodeModel;
import com.guiseframework.model.rdf.RDFResourceTreeNodeModel;
import com.guiseframework.validator.ValidationException;

/**Test panel for Marmot
@author Garret Wilson
*/
public class MarmotPanel extends DefaultNavigationPanel
{

	/**Default constructor.*/
	public MarmotPanel()
	{
		setLabel("Marmot Test");	//set the panel label

		final TextControl<String> textInput=new TextControl<String>(String.class);	//create a text input control
		textInput.setLabel("WebDAV Repository");
		textInput.setColumnCount(40);
		try
		{
			textInput.setValue("https://dav.globalmentor.com/public/");
		}
		catch(final PropertyVetoException propertyVetoException)	//if the change was vetoed, ignore the exception
		{
		}
		add(textInput);

		final MarmotSession marmotSession=new DefaultMarmotSession(new DefaultResourceKit());
		marmotSession.registerResourceKit(new ImageResourceKit());
		marmotSession.registerResourceKit(new FolderResourceKit());
		
		final Button addButton=new Button();
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
							final TreeControl treeControl=new TreeControl();
							treeControl.setTreeNodeRepresentationStrategy(RDFResource.class, new RepositoryResourceTreeNodeRepresentationStrategy(marmotSession));
							treeControl.setTreeNodeRepresentationStrategy(RDFLiteral.class, new RDFLiteralTreeNodeRepresentationStrategy());
							try
							{
									final List<RDFResource> rootResources=repository.getChildResourceDescriptions(repositoryURI);
									for(final RDFResource resource:rootResources)
									{
										treeControl.getRootNode().add(new RepositoryResourceTreeNodeModel(repository, resource));
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
/*TODO del
		try
		{
			final ActivityModelIOKit activityModelIOKit=new ActivityModelIOKit();
			final ResourceModel<Activity> activityResourceModel=activityModelIOKit.load(new FileInputStream("D:\\projects\\marmot\\example\\Activities\\test.maqro"), URI.create("file:/D:/projects/marmot/example/Activities/"));
			final Activity activity=activityResourceModel.getResource();
//TODO del			Debug.trace(RDFUtilities.toString(activity));
			final TreeControl treeControl=new TreeControl();
			treeControl.setTreeNodeRepresentationStrategy(RDFResource.class, new RDFResourceTreeNodeRepresentationStrategy());
			treeControl.setTreeNodeRepresentationStrategy(RDFLiteral.class, new RDFLiteralTreeNodeRepresentationStrategy());
			treeControl.getRootNode().add(new RDFResourceTreeNodeModel(activity));
			add(treeControl);
		}
		catch(final IOException ioException)
		{
			throw new AssertionError(ioException);
		}
*/
	}

}
