package arush.baatcheet.presenter

import android.net.Uri
import arush.baatcheet.model.FileHandler
import arush.baatcheet.model.LoginModel
import arush.baatcheet.view.LoginActivity

class LoginPresenter(context: LoginActivity) {
    private lateinit var loginModel: LoginModel
    private val context = context
    fun login(username: String, phoneNumber: String, imageUri: Uri?){
        FileHandler(context).storeProfileDetails(username,phoneNumber)
        loginModel = LoginModel(this,username, phoneNumber, context, imageUri)
    }
    fun verifier(code: String){
        loginModel.verifyCode(code)
    }
    fun verified(){
        context.authCompleted()
    }
}