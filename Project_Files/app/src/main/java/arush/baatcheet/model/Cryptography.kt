package arush.baatcheet.model

import java.security.KeyPairGenerator
import java.security.PrivateKey
import android.util.Base64
import java.security.PublicKey
import javax.crypto.Cipher
import kotlin.io.encoding.ExperimentalEncodingApi

class Cryptography {

    @OptIn(ExperimentalEncodingApi::class)
    fun generateKey() : Array<*>{
        val genKey = KeyPairGenerator.getInstance("RSA")
        genKey.initialize(1024)
        val keyPair = genKey.genKeyPair()
        val privateKey = keyPair.private
        val publicKey = keyPair.public
        return arrayOf(kotlin.io.encoding.Base64.encode(publicKey.encoded), privateKey.encoded)
    }

    fun encryptMessage(message:String, publicKey: PublicKey): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return cipher.doFinal(message.toByteArray())
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun decryptMessage(encryptedText: String, privateKey: PrivateKey):String{
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val encryptedBytes = Base64.decode(encryptedText, Base64.DEFAULT)
        return try{
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes)
        } catch (e: Exception){
            "Error with decryption key"
        }
    }
}