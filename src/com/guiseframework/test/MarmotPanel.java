package com.guiseframework.test;

import java.beans.PropertyVetoException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.mail.internet.ContentType;

import com.garretwilson.model.ResourceModel;
import com.globalmentor.marmot.AbstractMarmotSession;
import com.globalmentor.marmot.MarmotSession;
import com.globalmentor.marmot.guise.repository.RepositoryResourceTreeNodeModel;
import com.globalmentor.marmot.guise.repository.RepositoryResourceTreeNodeRepresentationStrategy;
import com.globalmentor.marmot.repository.Repository;
import com.globalmentor.marmot.repository.webdav.WebDAVRepository;
import com.globalmentor.marmot.resource.DefaultResourceKit;
import com.globalmentor.marmot.resource.folder.FolderResourceKit;
import com.globalmentor.marmot.resource.image.ImageResourceKit;
import com.globalmentor.rdf.RDFLiteral;
import com.globalmentor.rdf.RDFResource;
import com.globalmentor.rdf.RDFResources;
import com.globalmentor.rdf.maqro.Activity;
import com.globalmentor.rdf.maqro.ActivityModelIOKit;
import com.globalmentor.rdf.rdfs.RDFS;
import com.globalmentor.util.Debug;
import com.guiseframework.GuiseSession;
import com.guiseframework.component.*;
import com.guiseframework.component.rdf.RDFLiteralTreeNodeRepresentationStrategy;
import com.guiseframework.component.rdf.RDFResourceTreeNodeRepresentationStrategy;
import com.guiseframework.component.transfer.ImportStrategy;
import com.guiseframework.component.transfer.Transferable;
import com.guiseframework.event.*;
import com.guiseframework.model.Notification;
import com.guiseframework.model.rdf.RDFObjectTreeNodeModel;
import com.guiseframework.model.rdf.RDFResourceTreeNodeModel;
import com.guiseframework.validator.ValidationException;

/**Test panel for Marmot
@author Garret Wilson
*/
public class MarmotPanel extends LayoutPanel
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

/*TODO fix		
		final MarmoxMarmotSession marmotSession=new MarmoxMarmotSession(new DefaultResourceKit());
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
							
							
*/							
							
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
		
/*TODO fix
							final TreeControl treeControl=new TreeControl();
							treeControl.setTreeNodeDragEnabled(true);	//allow tree nodes to be dragged
							treeControl.setDropEnabled(true);	//TODO testing
							treeControl.addImportStrategy(new ImportStrategy<TreeControl>()	//add a new import strategy for this component
									{		
										public boolean canImportTransfer(final TreeControl component, final Transferable<?> transferable)
										{
											return true;	//accept all import types
										}
										public boolean importTransfer(final TreeControl component, final Transferable<?> transferable)
										{
Debug.trace("!!got an import from component:", component);											
											return true;
										}
									});
							
							
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
		
		//details text area
		final TextAreaControl detailsTextArea=new TextAreaControl(15, 80);	//create a text area control
		detailsTextArea.setLabel("Drop Here for Drop Details");	//set the label of the text area
		detailsTextArea.setEditable(false);	//don't allow the text area control to be edited
		detailsTextArea.setDropEnabled(true);	//allow dropping on the text area
		detailsTextArea.addImportStrategy(new ImportStrategy<TextAreaControl>()	//add a new import strategy for this component
				{		
					public boolean canImportTransfer(final TextAreaControl component, final Transferable<?> transferable)
					{
						return true;	//accept all import types
					}
					public boolean importTransfer(final TextAreaControl component, final Transferable<?> transferable)
					{
						final String oldContent=component.getValue();	//get the old text area control content
						final StringBuilder newContent=new StringBuilder();	//create a string builder to collect our new information
						if(oldContent!=null)	//if there is content already
						{
							newContent.append(oldContent);	//add the old content
						}
						newContent.append("Drop Source: ").append(transferable.getSource().getClass().getName()).append('\n');
						newContent.append("Content count: ").append(transferable.getContentTypes().length).append('\n');
						for(final ContentType contentType:transferable.getContentTypes())	//for each content type
						{
							newContent.append("* Drop Content Type: ").append(contentType).append('\n');
							newContent.append("  Drop Data: ").append(transferable.transfer(contentType)).append('\n');	//actually transfer the data
						}
						newContent.append('\n');
						try
						{
							component.setValue(newContent.toString());	//update the text area contents
						}
						catch(final PropertyVetoException propertyVetoException)	//if the change was vetoed, ignore the exception
						{
						}
						return true;	//indicate that we imported the information
					}
				});
		add(detailsTextArea);	//add the drop details text area control to the panel
*/
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
