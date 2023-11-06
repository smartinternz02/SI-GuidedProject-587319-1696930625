package arush.baatcheet.model

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.widget.EditText
import android.widget.Toast
import arush.baatcheet.presenter.LoginPresenter
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class LoginModel (private val presenter: LoginPresenter, private val username: String,
                  private val phoneNumber: String, private val context: Context, private val imageUri: Uri?) {
    private val auth = FirebaseAuth.getInstance()
    private var verificationId : String? = null
    private var code : String? = null
    private  var edtOTP: EditText? = null

    init {
        sendVerificationCode(phoneNumber)
    }

    private fun sendVerificationCode(phoneNum : String)
    {
        val firebaseAuth = Firebase.auth
        val firebaseAuthSettings = firebaseAuth.firebaseAuthSettings
        firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(phoneNum, code)

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNum)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(context as Activity)
            .setCallbacks(
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onCodeSent(s: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                        super.onCodeSent(s, forceResendingToken)
                        verificationId = s
                    }

                    override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                        code = phoneAuthCredential.smsCode
                        if (code != null) {
                            edtOTP?.setText(code)
                            verifyCode(code!!)
                        }
                    }
                    override fun onVerificationFailed(e: FirebaseException) {
                        Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            )
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(OnCompleteListener<AuthResult?> { task ->
                if (task.isSuccessful) {
                    DatabaseHandler().login(username, phoneNumber, imageUri, FileHandler(context).keyGenCaller())
                    Toast.makeText(context,"Let's start baatcheet",Toast.LENGTH_SHORT).show()
                    presenter.verified()
                } else {
                    Toast.makeText(context, task.exception!!.message, Toast.LENGTH_LONG)
                        .show()
                }
            })
    }
}