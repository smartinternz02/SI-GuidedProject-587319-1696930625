package arush.baatcheet.view

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import arush.baatcheet.R
import arush.baatcheet.presenter.HomeScreenPresenter
import arush.baatcheet.view.ui.theme.BaatcheetTheme

class EditProfileActivity : ComponentActivity() {

    private val reqCode = 1000
    private lateinit var filePresenter : HomeScreenPresenter
    private lateinit var phoneNum : String
    private lateinit var name : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        filePresenter = HomeScreenPresenter.getInstance(applicationContext)
        phoneNum = intent.getStringExtra("phone").toString()
        name = intent.getStringExtra("name").toString()
        setContent {
            BaatcheetTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EditProfile(filePresenter, phoneNum, name, { changeDP() },{finish()})
                }
            }
        }
    }
    private fun changeDP(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        startActivityForResult(intent, reqCode)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== RESULT_OK){
            if(requestCode == reqCode){
                if (data != null) {
                    filePresenter.setMyDP(data.data!!)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfile(filePresenter: HomeScreenPresenter, phoneNum:String, name:String,
                callback: ()->(Unit), goBack: ()->Unit) {
    var name by remember { mutableStateOf(name) }
    var number by remember { mutableStateOf(phoneNum) }
    var editable by remember { mutableStateOf(true) }
    var imageEdit by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
    ) {
        Text(
            text = "Edit Profile",
            style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.lexend_regular))
            ),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally) {
            ConstraintLayout(modifier = Modifier.fillMaxHeight(0.4f).fillMaxWidth()) {
                val (profile, add) = createRefs()
                ProfilePicture(filePresenter, modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .fillMaxSize()
                    .constrainAs(profile) {
                        top.linkTo(parent.top)
                        centerHorizontallyTo(parent)
                    }
                )
                Box(modifier = Modifier.fillMaxSize(0.14f)
                    .constrainAs(add){
                        bottom.linkTo(profile.bottom, margin = 5.dp)
                        end.linkTo(profile.end, margin = 20.dp)
                    }.clip(shape = CircleShape)
                    .clickable {
                        callback()
                        imageEdit = true
                    })
                {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "New pic",
                        tint = Color(0xFFFF6D00), modifier = Modifier.size(60.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                }, enabled = editable,
                modifier = Modifier.fillMaxWidth(0.75f),
                placeholder = {
                    Text(text = "Name",
                        style = TextStyle(fontFamily = FontFamily(Font((R.font.lexend_regular))), fontSize = 23.sp))
                }, textStyle = TextStyle(fontFamily = FontFamily(Font((R.font.lexend_regular))), fontSize = 23.sp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = number,
                onValueChange = {
                    number = it
                }, enabled = editable,
                modifier = Modifier.fillMaxWidth(0.75f),
                placeholder = {
                    Text(text = "New Number",
                        style = TextStyle(fontFamily = FontFamily(Font((R.font.lexend_regular))), fontSize = 23.sp))
                }, textStyle = TextStyle(fontFamily = FontFamily(Font((R.font.lexend_regular))), fontSize = 23.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = {
                    editable = false
                    filePresenter.editProfile(name,number, imageEdit)
                },
                modifier = Modifier
                    .border(2.dp, Color(0xFF808080),
                        shape = RoundedCornerShape(8.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF311B92)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "SAVE CHANGES",
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.lexend_regular))
                )
            }

        }
    }
}