package arush.baatcheet.view

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import arush.baatcheet.R
import arush.baatcheet.databinding.ActivityLoginBinding
import arush.baatcheet.model.FileHandler
import arush.baatcheet.presenter.LoginPresenter
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var loginBinding: ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()
    private var checker = true
    private val reqCode = 1000
    private lateinit var imageUri : Uri
    private val internetPermReqCode = 1001
    private val readContactsPermReqCode = 1002
    private val writeContactPermReqCode = 1003

    override fun onStart() {
        super.onStart()

        val user = auth.currentUser
        if (user != null) {
            authCompleted()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        val view = loginBinding.root
        setContentView(view)
        imageUri = Uri.parse("android.resource://${this.packageName}/${R.drawable.no_dp_logo}")

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.INTERNET), internetPermReqCode)
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_CONTACTS), readContactsPermReqCode)
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_CONTACTS), writeContactPermReqCode)
        }

        val loginPresenter = LoginPresenter(this)

        loginBinding.getOTPButton.setOnClickListener {
            if (checker){
                if (loginBinding.nameInput.text.isNullOrEmpty() || loginBinding.numberInput.text.isNullOrEmpty()) {
                    Toast.makeText(this@LoginActivity, "Name or number missing", Toast.LENGTH_SHORT).show()
                }
                else if(loginBinding.numberInput.text?.length!! != 10 ){
                    Toast.makeText(this@LoginActivity, "Enter 10 digit phone number", Toast.LENGTH_SHORT).show()
                }
                else {
                    loginPresenter.login(loginBinding.nameInput.text.toString(),
                        "+91${loginBinding.numberInput.text.toString()}", imageUri)
                    FileHandler(applicationContext).storeDP(imageUri)
                    loginBinding.otpLayout.isVisible = true
                    loginBinding.getOTPButton.text = "VERIFY"
                    loginBinding.textInputLayout.isEnabled = false
                    loginBinding.textInputLayout2.isEnabled = false
                    loginBinding.profileAddButton.isEnabled = false
                    checker = false
                }
            }
            else{
                if(loginBinding.otpInput.text.isNullOrEmpty()){
                    Toast.makeText(this@LoginActivity, "Enter OTP", Toast.LENGTH_SHORT).show()
                }
                else{
                    loginPresenter.verifier(loginBinding.otpInput.text.toString())
                    loginBinding.getOTPButton.isEnabled = false
                    loginBinding.progressBar.isVisible = true
                }
            }
        }
        loginBinding.profileAddButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            startActivityForResult(intent, reqCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== RESULT_OK){
            if(requestCode == reqCode){
                if (data != null) {
                    imageUri = data.data!!
                    FileHandler(applicationContext).storeDP(imageUri!!)
                    loginBinding.profileImage.setImageURI(imageUri)
                }
                else{
                    imageUri = Uri.parse("android.resource://${this.packageName}/${R.drawable.no_dp_logo}")
                    FileHandler(applicationContext).storeDP(imageUri)
                    loginBinding.profileImage.setImageURI(imageUri)
                }
            }
        }
    }
    fun authCompleted(){
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}