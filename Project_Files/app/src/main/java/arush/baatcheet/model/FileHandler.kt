package arush.baatcheet.model

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import arush.baatcheet.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class FileHandler (private val context: Context){
    private val dir = context.filesDir
    private val subdir = File(dir, "BaatCheet")
    init {
        if(!subdir.exists()){
            subdir.mkdirs()
        }
    }

    fun storeProfileDetails(username: String,phoneNumber: String){
        val file = File(subdir, "profileData.json")
        if(!file.exists()){file.createNewFile()}
        val gson = Gson()
        val data = ArrayList<String>()
        data.add(username)
        data.add(phoneNumber)
        val jsonData = gson.toJson(data)
        val fileWriter = FileWriter(file, false)
        fileWriter.write(jsonData)
        fileWriter.close()
    }

    fun getProfileDetails(): ArrayList<String> {
        val file = File(subdir, "profileData.json")
        val gson = Gson()
        val jsonData = file.readText()
        return gson.fromJson<ArrayList<String>>(jsonData, ArrayList::class.java)
    }

    fun storeDP(imageUri: Uri?){
        val inputStream = if (imageUri != null) {
            context.contentResolver.openInputStream(imageUri)
        } else {
            val drawable = ContextCompat.getDrawable(context, R.drawable.no_dp_logo)
            val bitmap = drawable?.toBitmap()
            bitmap?.let {
                val stream = ByteArrayOutputStream()
                it.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                ByteArrayInputStream(stream.toByteArray())
            }
        }
        inputStream?.use { input ->
            val outputStream = FileOutputStream(File(subdir, "dp.jpg"))
            input.copyTo(outputStream)
        }
    }
    fun getMyDP(): Uri {
        val file = File(subdir, "dp.jpg")
        return Uri.fromFile(file)
    }

    fun keyGenCaller() : String{
        val keyArray = Cryptography().generateKey()
        storePrivateKey(keyArray[1] as ByteArray)
        return keyArray[0].toString()
    }

    private fun storePrivateKey(privateKey: ByteArray){
        val privateFile = File(subdir, "privateKey.key")
        if(!privateFile.exists()){
            privateFile.createNewFile()
        }
        val fileWriter = FileWriter(privateFile, false)

        privateFile.writeBytes(privateKey)
        fileWriter.close()
    }
    fun getPrivateKey():PrivateKey{
        val privateFile = File(subdir, "privateKey.key")
        val keyBytes = privateFile.readBytes()

        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(keySpec)
    }

    fun storePublicKey(publicKey: ByteArray, username: String){
        val mainDir = File(subdir, username)
        if(!mainDir.exists()){
            mainDir.mkdir()
        }
        val file = File(mainDir, username+"Key.key")
        if(!file.exists()){
            file.createNewFile()
        }
        val fileWriter = FileWriter(file, false)

        fileWriter.write("")
        file.writeBytes(publicKey)
        fileWriter.close()

    }
    @OptIn(ExperimentalEncodingApi::class)
    fun getPublicKey(username: String):PublicKey?{
        val mainDir = File(subdir, username)
        val file = File(mainDir, username+"Key.key")
        val publicKeyBytes = Base64.decode(file.readBytes())
        val keySpec = X509EncodedKeySpec(publicKeyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec)
    }

    fun storeSavedMessage(username: String, message: Any?, timestamp: String){
        val data = SaveMessageModel(username, message, timestamp)
        val file = File(subdir, "saveMessage.json")
        val storedData = retrieveSavedMessage()
        if(!file.exists()){
            file.createNewFile()
        }
        val gson = Gson()
        storedData.add(data)

        val jsonData = gson.toJson(storedData)
        val fileWriter = FileWriter(file, false)
        fileWriter.write(jsonData)
        fileWriter.close()
    }

    fun retrieveSavedMessage() : ArrayList<SaveMessageModel>{
        val file = File(subdir, "saveMessage.json")
        if(file.exists()){
            val gson = Gson()
            val jsonData = file.readText()
            val dataList = object : TypeToken<ArrayList<SaveMessageModel>>() {}.type
            return gson.fromJson(jsonData,dataList)
        }
        return ArrayList<SaveMessageModel>()
    }

    fun storeHomeMessage(messageList: Map<String, Map<String, ArrayList<HashMap<String, Any>>>>){
        val file = File(subdir, "messageList.json")
        if(!file.exists()){file.createNewFile()}
        val fileWriter = FileWriter(file,false)
        val gson = Gson()
        val jsonData = gson.toJson(messageList)
        fileWriter.write(jsonData)
        fileWriter.close()
    }
    fun getHomeMessage() : Map<String, Map<String, ArrayList<HashMap<String, Any>>>>{
        val file = File(subdir, "messageList.json")
        if(!file.exists()){
            file.createNewFile()
            return emptyMap()
        }
        val gson = Gson()
        val jsonData = file.readText()
        val mapType = object : TypeToken<Map<String, Map<String, ArrayList<HashMap<String, Any>>>>>() {}.type
        if(jsonData.isEmpty()){
            return emptyMap()
        }
        return gson.fromJson(jsonData, mapType)
    }

    fun addContact(username: String){
        val storedList = getHomeMessage().toMutableMap()
        storedList[username] = mapOf("messages" to ArrayList(listOf(HashMap())))
        storeHomeMessage(storedList)
    }

    fun storeGroup(name: String, contactList: String){
        val file = File(subdir, "$name.json")
        if(!file.exists()){
            file.createNewFile()
            val gson = Gson()
            val jsonData = gson.toJson(contactList)
            val fileWriter = FileWriter(file, false)
            fileWriter.write(jsonData)
            fileWriter.close()
        }
        else{
            return
        }
    }

    suspend fun getGroupContacts(name: String, connection: DatabaseHandler): List<GroupDetailsModel>{
        val file = File(subdir, "$name.json")
        if(!file.exists()){
            return emptyList()
        }
        val gson = Gson()
        val jsonData = file.readText()
        val contactList = mutableListOf<GroupDetailsModel>()
        val contacts = gson.fromJson(jsonData, String::class.java).split(" ").toMutableList()
        contacts.removeLast()
        for (item in contacts){
            connection.getPublicKey(item).collect{
                contactList.add(GroupDetailsModel(item, it))
            }
        }
        return contactList
    }
    fun storeChatMessage(username: String, message: Any?, timestamp: String){
        val mainDir = File(subdir, username)
        if(!mainDir.exists()){
            mainDir.mkdir()
        }
        val file = File(mainDir, username+"DM.json")
        if(!file.exists()){
            file.createNewFile()
        }
        val gson = Gson()
        val jsonData = gson.toJson(SaveMessageModel(username,message, timestamp))
        val fileWriter = FileWriter(file, true)
        fileWriter.write(jsonData)
        fileWriter.close()
    }

    fun retrieveChatMessage(username: String) : ArrayList<SaveMessageModel>{
        val mainDir = File(subdir, username)
        val file = File(mainDir, username+"DM.json")
        val gson = Gson()
        if(file.exists()){
            val messageArray = ArrayList<SaveMessageModel>()
            val jsonData = file.readText()
            val jsonObjects = jsonData.split("}").toMutableList()
            jsonObjects.removeLast()
            for (message in jsonObjects){
                val orgMessage = gson.fromJson("$message}", SaveMessageModel::class.java)
                messageArray.add(orgMessage)
            }
            return messageArray
        }
        return ArrayList<SaveMessageModel>()
    }

    fun fileExist(name:String): Boolean{
        val file = File(subdir, "$name.json")
        return file.exists()
    }
}