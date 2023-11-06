package arush.baatcheet.presenter

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Base64
import arush.baatcheet.model.AddContactModel
import arush.baatcheet.model.Cryptography
import arush.baatcheet.model.DatabaseHandler
import arush.baatcheet.model.FileHandler
import arush.baatcheet.model.GroupDetailsModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.security.PublicKey
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HomeScreenPresenter(private val context : Context) {
    private val connection = DatabaseHandler()
    private val fileHandler = FileHandler(context)
    private val cryptography = Cryptography()
    private val privateKey = fileHandler.getPrivateKey()
    private lateinit var publicKey: PublicKey
    private var groupDetails: List<GroupDetailsModel>? = null
    val myNum = connection.getMyNum()
    private lateinit var myPublicKey: PublicKey

    companion object {
        private var instance: HomeScreenPresenter? = null

        fun getInstance(context: Context): HomeScreenPresenter {
            if (instance == null) {
                instance = HomeScreenPresenter(context)
            }
            return instance!!
        }
    }
    suspend fun getPublicKey(username: String){
        getMyKey()
        connection.getPublicKey(username).collect{
            publicKey = it
        }
    }

    suspend fun getMyKey(){
        connection.getPublicKey(myNum).collect{
            myPublicKey = it
        }
    }
    suspend fun sendMessage(username: String, message:String, isGroup: Boolean){
        var count = 0
        while (!isGroup && (!(this::publicKey.isInitialized) || !(this::myPublicKey.isInitialized))) {
            delay(500)
            if(count++>20){
                return
            }
        }
        val timeStamp = getCombinedTimestamp()
        if(isGroup){
            while (groupDetails.isNullOrEmpty() || !(this::myPublicKey.isInitialized)){
                delay(500)
                if(count++ > 20){
                    return
                }
            }
            for(contact in groupDetails!!){
                val encryptedMessage = cryptography.encryptMessage(message, contact.pubKey)
                connection.sendGroupMessage(Base64.encodeToString(encryptedMessage, Base64.DEFAULT),contact.username, timeStamp,username)
            }
        }
        else{
            val encryptedMessage = cryptography.encryptMessage(message, publicKey)
            connection.sendMessage(Base64.encodeToString(encryptedMessage, Base64.DEFAULT),username, timeStamp)
        }
        val encryptedMessageStore = cryptography.encryptMessage(message, myPublicKey)
        fileHandler.storeChatMessage(username, Base64.encodeToString(encryptedMessageStore, Base64.DEFAULT), timeStamp)
    }

    fun receiveMessage(username: String) = callbackFlow<Boolean>{
        connection.receiveMessage(username).collect{
            for (message in it){
                fileHandler.storeChatMessage(username, message["message"], message["timestamp"].toString())
            }
            trySend(true)
        }
    }

    fun getNewGroup(username: String) = callbackFlow<ArrayList<HashMap<String,Any>>>
    {
        connection.receiveMessage(username).collect{
            trySend(it)
            close()
        }
    }
    fun retrieveMessage(username: String): ArrayList<HashMap<String, String>>{
        val messageList = fileHandler.retrieveChatMessage(username)
        var messages = ArrayList<HashMap<String,String>>()
        for (message in messageList){
            messages.add(hashMapOf("timestamp" to message.timestamp, "message" to cryptography.decryptMessage(message.message.toString(),privateKey)))
        }
        return messages
    }

    fun saveMessage(username: String, message: Any?, timestamp: String) {
        fileHandler.storeSavedMessage(username, message, timestamp)
    }

    fun getDecrypted(msg:String) : String{
        return cryptography.decryptMessage(msg,privateKey)
    }
    fun getMessageList(): Flow<Map<String, Map<String, ArrayList<HashMap<String, Any>>>>> {
        return connection.getMessagesList()
    }
    fun getMessageListFile(): Map<String, Map<String, ArrayList<HashMap<String, Any>>>>{
        return fileHandler.getHomeMessage()
    }
    fun setMessageList(messageList: Map<String, Map<String, ArrayList<HashMap<String, Any>>>>){
        fileHandler.storeHomeMessage(messageList)
    }
    fun removeMessages(number: String){
        connection.removeList(number)
    }
    fun getMyDp(): Uri {
        return fileHandler.getMyDP()
    }
    fun getDPLink(username: String):Flow<String> {
        return connection.getDPLink(username)
    }

    fun getProfileDetails(): ArrayList<String> {
        return fileHandler.getProfileDetails()
    }

    fun setMyDP(image: Uri){
        DatabaseHandler().updateDP(image)
        fileHandler.storeDP(image)
    }

    fun getContactName(username: String, contentResolver: ContentResolver):String?{
        return AddContactModel().contactName(username, contentResolver)
    }

    fun editProfile(username: String,phoneNumber: String, image:Boolean){
        fileHandler.storeProfileDetails(username,phoneNumber)
        if (image) {
            connection.EditProfile(username,phoneNumber,fileHandler.getMyDP())
        }
        else{
            connection.EditProfile(username,phoneNumber,null)
        }
    }

    suspend fun getGroupDetails(username: String){
        groupDetails = fileHandler.getGroupContacts(username, connection)
    }
    fun grpDetInit(name: String): Boolean{
        return fileHandler.fileExist(name)
    }
    private fun getCombinedTimestamp(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss")
        return currentDateTime.format(formatter)
    }
}