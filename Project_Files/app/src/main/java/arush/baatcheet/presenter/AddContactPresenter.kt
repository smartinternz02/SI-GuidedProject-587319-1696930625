package arush.baatcheet.presenter

import android.content.ContentResolver
import android.content.Context
import arush.baatcheet.model.AddContactModel
import arush.baatcheet.model.ContactItem
import arush.baatcheet.model.DatabaseHandler
import arush.baatcheet.model.FileHandler
import kotlinx.coroutines.flow.Flow

class AddContactPresenter {

    private val connection = DatabaseHandler()
    private val addContactModel = AddContactModel()

    fun getContactList(contentResolver: ContentResolver):List<ContactItem>{
        return addContactModel.getContactList(contentResolver)
    }
    fun getDPLink(username: String): Flow<String> {
        return connection.getDPLink(username)
    }

    fun sendInvite(number:String, context: Context){
        addContactModel.sendInvite(number,context)
    }

    fun createGroup(contactList: Set<String>, name: String, context: Context, myNum: String, newGroup: Boolean){
        val groupName = if(newGroup){
            connection.createGroup(name)
        } else{
            name
        }
        var contactListString = ""
        val fileHandler = FileHandler(context)
        fileHandler.addContact(groupName)
        for (contact in contactList){
            contactListString += "$contact "
        }
        fileHandler.storeGroup(groupName, contactListString)
        if (newGroup){
            contactListString += myNum
            for (contact in contactList) {
                connection.sendGroupInvite(contact, groupName, contactListString)
            }
        }
    }

    fun addContact(username: String, context: Context){
        FileHandler(context).addContact(username)
    }
}