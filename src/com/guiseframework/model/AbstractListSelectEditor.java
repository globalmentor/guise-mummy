package com.guiseframework.model;

import java.beans.PropertyVetoException;

import com.garretwilson.beans.*;
import static com.garretwilson.lang.ObjectUtilities.*;

import com.guiseframework.component.*;
import com.guiseframework.event.*;
import com.guiseframework.prototype.ActionPrototype;
import static com.guiseframework.theme.Theme.*;

/**An abstract class for editing values in a list model.
Prototypes are provided for common edit functionality.
This class registers itself with the given list, which will cause memory leaks if an instance of this class is discarded without also discarding the list.
@param <V> The type of values contained in the model.
@author Garret Wilson
*/
public abstract class AbstractListSelectEditor<V> implements ListSelectEditor<V>
{

	/**The list select model being edited.*/
	private final ListSelectModel<V> listSelectModel;

		/**@return The list select model being edited.*/
		public ListSelectModel<V> getListSelectModel() {return listSelectModel;}

	/**The prototype for inserting a value into the list.*/
	private final ActionPrototype insertActionPrototype;
		
		/**@return The prototype for inserting a value into the list.*/
		public ActionPrototype getInsertActionPrototype() {return insertActionPrototype;}

	/**The prototype for editing a value in the list.*/
	private final ActionPrototype editActionPrototype;
		
		/**@return The prototype for editing a value in the list.*/
		public ActionPrototype getEditActionPrototype() {return editActionPrototype;}

	/**The prototype for removing a value from the list.*/
	private final ActionPrototype removeActionPrototype;
		
		/**@return The prototype for removing a value from the list.*/
		public ActionPrototype getRemoveActionPrototype() {return removeActionPrototype;}

	/**List select model constructor.
	@param listSelectModel The list select model this prototype manipulates.
	@exception NullPointerException if the given list select model is <code>null</code>.
	*/
	public AbstractListSelectEditor(final ListSelectModel<V> listSelectModel)
	{
		this.listSelectModel=checkInstance(listSelectModel, "List select model cannot be null.");
		listSelectModel.addListSelectionListener(new ListSelectionListener<V>()	//listen for the list selection changing
				{
					public void listSelectionChanged(final ListSelectionEvent<V> selectionEvent)	//if the list selection changes
					{
						updateProperties();	//keep the properties up-to-date
					}
				});
			//insert
		insertActionPrototype=new ActionPrototype(LABEL_INSERT, GLYPH_INSERT);
		insertActionPrototype.addActionListener(new ActionListener()	//listen for the insert action being performed
				{
					public void actionPerformed(final ActionEvent actionEvent)	//if the action was performed
					{
						final ListSelectModel<V> listSelectModel=getListSelectModel();	//get the list select model
						//TODO lock the list
						final int selectedIndex=listSelectModel.getSelectedIndex();	//get the selected index
						final int insertIndex=selectedIndex>=0 ? selectedIndex : listSelectModel.size();	//if no index is select, insert at the end of the list
						final V value=createValue();	//create a new value
						editValue(value, insertIndex, false);	//edit and insert the new value
					}
				});
			//edit
		editActionPrototype=new ActionPrototype(LABEL_EDIT, GLYPH_EDIT);		
		editActionPrototype.addActionListener(new ActionListener()	//listen for the edit action being performed
				{
					public void actionPerformed(final ActionEvent actionEvent)	//if the action was performed
					{
						final ListSelectModel<V> listSelectModel=getListSelectModel();	//get the list select model
						//TODO lock the list
						final int selectedIndex=listSelectModel.getSelectedIndex();	//get the selected index
						if(selectedIndex>=0)	//if there is a selected index
						{
							final V value=listSelectModel.get(selectedIndex);	//get the selected value
							editValue(value, selectedIndex, true);	//edit and replace the current selected value
						}
					}
				});
			//remove
		removeActionPrototype=new ActionPrototype(LABEL_REMOVE, GLYPH_REMOVE);
		removeActionPrototype.addActionListener(new ActionListener()	//listen for the remove action being performed
				{
					public void actionPerformed(final ActionEvent actionEvent)	//if the action was performed
					{
						final ListSelectModel<V> listSelectModel=getListSelectModel();	//get the list select model
						//TODO lock the list
						final int selectedIndex=listSelectModel.getSelectedIndex();	//get the selected index
						if(selectedIndex>=0)	//if there is a selected index
						{
							listSelectModel.remove(selectedIndex);	//remove the selected index
						}
					}
				});
		updateProperties();	//initialize the properties
	}

	/**Updates the action properties based upon the current state of the list select model.*/
	protected void updateProperties()
	{
		editActionPrototype.setEnabled(getListSelectModel().getSelectedIndex()>=0);	//only enable the edit prototype if there is a list item selected
		removeActionPrototype.setEnabled(getListSelectModel().getSelectedIndex()>=0);	//only enable the remove prototype if there is a list item selected
	}

	/**Commences editing a value.
	This method returns immediately before editing is finished.
	Once the value has been edited and accepted, it will be added to the list and the selected value updated to the edited value.
	@param value The value to edit.
	@param index The index at which the edited value will be placed.
	@param replace <code>true</code> if the value should replace the value at the given index, or <code>false</code> if the value should be inserted at the given index.
	*/
	protected void editValue(final V value, final int index, final boolean replace)
	{
		final ListSelectModel<V> listSelectModel=getListSelectModel();	//get the list select model
		final ValuedComponent<V> valuedComponent=createValuedComponent();	//create a component for editing the value
		try
		{
			valuedComponent.setValue(value);	//show the value in the component
		}
		catch(final PropertyVetoException propertyVetoException)	//if the value can't be set, consider it an implementation error
		{
			throw new AssertionError(propertyVetoException);
		}
	  final NotificationOptionDialogFrame editDialog=new NotificationOptionDialogFrame(valuedComponent, Notification.Option.OK, Notification.Option.CANCEL);    //show the compoent in a dialog with two buttons
		final String title=LABEL_EDIT;	//determine the title TODO add the lexical form of the value 				
		editDialog.setLabel(title);	//set the title of the dialog
	  editDialog.open(new AbstractGenericPropertyChangeListener<Frame.Mode>()	//open the dialog modally
	  		{
			  	public void propertyChange(final GenericPropertyChangeEvent<Frame.Mode> genericPropertyChangeEvent)
			  	{
			  		if(genericPropertyChangeEvent.getNewValue()==null && editDialog.getValue()==Notification.Option.OK)	//if modality is ending and the user selected OK
			  		{
			  			final V newValue=valuedComponent.getValue();	//get the new value
			  			if(replace)	//if we should replace the current value
			  			{
		  					listSelectModel.set(index, value);	//change the value in the list
			  			}
			  			else	//if we shouldn't replace the current value
			  			{
			  				listSelectModel.add(index, value);	//insert the value into the list
			  			}
			  			try
			  			{
			  				listSelectModel.setSelectedIndexes(index);	//make sure the modifed index is selected
			  			}
			  			catch(final PropertyVetoException propertyVetoException)	//if we can't select the modified item, there's a problem somewhere; in itself this does not hurt the edit functionality, but it's a problem that shouldn't be occurring 
			  			{
			  				throw new AssertionError(propertyVetoException);
			  			}
			  		}
			  	}
	  		});
	}

	/**Creates and allows the user to edit a new value.
	If the user accepts the changes, the value is inserted before the currently selected value in the list,
	or at the end of the list if no value is selected.
	*/
	public void insertValue()
	{
		final ListSelectModel<V> listSelectModel=getListSelectModel();	//get the list select model
		//TODO lock the list
		final int selectedIndex=listSelectModel.getSelectedIndex();	//get the selected index
		final int insertIndex=selectedIndex>=0 ? selectedIndex : listSelectModel.size();	//if no index is select, insert at the end of the list
		final V value=createValue();	//create a new value
		editValue(value, insertIndex, false);	//edit and insert the new value		
	}

	/**Edits the currently selected value in the list.
	If no value is selected in the list, no action occurs.
	*/
	public void editValue()
	{
		final ListSelectModel<V> listSelectModel=getListSelectModel();	//get the list select model
		//TODO lock the list
		final int selectedIndex=listSelectModel.getSelectedIndex();	//get the selected index
		if(selectedIndex>=0)	//if there is a selected index
		{
			final V value=listSelectModel.get(selectedIndex);	//get the selected value
			editValue(value, selectedIndex, true);	//edit and replace the current selected value
		}		
	}

	/**Removes the currently selected value in the list.
	If no value is selected in the list, no action occurs.
	*/
	public void removeValue()
	{
		final ListSelectModel<V> listSelectModel=getListSelectModel();	//get the list select model
		//TODO lock the list
		final int selectedIndex=listSelectModel.getSelectedIndex();	//get the selected index
		if(selectedIndex>=0)	//if there is a selected index
		{
			listSelectModel.remove(selectedIndex);	//remove the selected index
		}		
	}

	/**Creates a new value to add to the list.
	@return A new default value to add.
	*/
	protected abstract V createValue();

	/**Creates a component for editing a value.
	@param value The current value.
	@return A component for editing the value.
	*/
	protected abstract ValuedComponent<V> createValuedComponent();

}
