package arush.baatcheet.view

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import arush.baatcheet.R
import arush.baatcheet.presenter.AddContactPresenter
import arush.baatcheet.view.ui.theme.BaatcheetTheme
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddContactActivity : ComponentActivity() {

    private val sendSmsPermReqCode = 1004
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val myNum = intent.getStringExtra("myNum")
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.SEND_SMS), sendSmsPermReqCode)
        }
        setContent {
            BaatcheetTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (myNum != null) {
                        AddContact(myNum){finish()}
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
@Composable
fun AddContact(myNum: String, finishActivity: ()->Unit) {
    var textVisibility by remember { mutableStateOf(false) }
    var searchVisibility by remember { mutableStateOf(false) }
    var contactSelectionList = mutableSetOf<String>()
    var groupNameText by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }
    val addContactPresenter = AddContactPresenter()
    val context = LocalContext.current
    var contactList = addContactPresenter.getContactList(context.contentResolver)

    val filteredList = contactList.filter { contact ->
        contact.name.contains(searchText, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.085f),
            contentAlignment = Alignment.BottomStart) {
            if(searchVisibility){
                BackHandler {
                    searchVisibility = false
                }
                SearchContacts(
                    onSearchBarClose = {
                        searchText = ""
                        searchVisibility = false
                    },
                    onInput = {
                        searchText = it
                    }
                )
            }
            else{
                if(textVisibility){
                    TextField(value = groupNameText, onValueChange = { groupNameText=it }, placeholder = {Text("Enter Group Name",
                        modifier = Modifier.padding(start = 10.dp),color = if(isSystemInDarkTheme()){
                            Color.LightGray
                        }else{
                            Color.DarkGray
                        }, style = TextStyle(fontSize = 22.sp,fontFamily = FontFamily(Font((R.font.lexend_regular))))
                    )},
                        colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent),
                        modifier = Modifier.fillMaxSize(),
                        textStyle = TextStyle( fontSize = 22.sp,fontFamily = FontFamily(Font((R.font.lexend_regular))) )
                    )
                    BackHandler {
                        textVisibility = !textVisibility
                    }
                }
                else{
                    Row (modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically){
                        Row (modifier = Modifier
                            .fillMaxHeight()
                            .clickable { textVisibility = !textVisibility },
                            verticalAlignment = Alignment.CenterVertically){
                            Icon(imageVector = Icons.Filled.Add, contentDescription = "new group",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(top = 4.dp))
                            Text("Create New Group",
                                style = TextStyle(fontFamily = FontFamily(Font((R.font.lexend_regular))), fontSize = 23.sp),
                                modifier = Modifier.padding(start = 6.dp))
                        }
                        Icon(imageVector = Icons.Outlined.Search, contentDescription = "search",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .size(40.dp)
                                .padding(top = 6.dp)
                                .clickable { searchVisibility = true })
                    }
                }
            }
        }
        Divider(color = Color(0xFFA3A3A3), modifier = Modifier
            .fillMaxWidth()
            .height(1.dp))
        Column(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally) {
            LazyColumn(modifier = Modifier.fillMaxWidth(0.92f)){
                items(filteredList){
                    var isSelected by remember { mutableStateOf(false) }
                    var bgColor = if(isSelected && textVisibility) {
                        if(isSystemInDarkTheme())Color(0xFF179B38) else Color(0xFF02EC3D)
                    } else {
                        contactSelectionList.clear()
                        isSelected = false
                        Color.Transparent
                    }
                    Column (modifier = Modifier
                        .fillParentMaxHeight(0.09f)
                        .background(color = bgColor)){
                        ContactDisplay(it.name, it.phoneNumber, addContactPresenter){name,num,image->
                            if(textVisibility){
                                isSelected = !isSelected
                                if(num in contactSelectionList){
                                    contactSelectionList.remove(num)
                                }
                                else if(contactSelectionList.size < 5){
                                    contactSelectionList.add(num)
                                }
                                else{
                                    Toast.makeText(context, "Sorry you cannot add more than 5 people", Toast.LENGTH_SHORT).show()
                                }
                            }
                            else{
                                val intent = Intent(context, ChatActivity::class.java)
                                intent.putExtra("contactName", name)
                                intent.putExtra("contactNumber", num)
                                intent.putExtra("imageLink", image)
                                context.startActivity(intent)
                                addContactPresenter.addContact(num,context)
                                finishActivity()
                            }
                        }
                    }
                    Divider(color = Color(0xFFA3A3A3), modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp))
                }
            }
                if(textVisibility){
                    Box(modifier = Modifier.fillMaxSize()){
                        FloatingActionButton(onClick = {
                            if(contactSelectionList.size < 2){
                                Toast.makeText(context, "Please select at least 2 contacts.", Toast.LENGTH_SHORT).show()
                            }
                            else if(groupNameText.length < 3){
                                Toast.makeText(context, "The group name should have a minimum length of 3.", Toast.LENGTH_SHORT).show()
                            }
                            else {
                                GlobalScope.launch {
                                    addContactPresenter.createGroup(contactSelectionList, groupNameText, context, myNum, true)
                                }
                                finishActivity()
                            }
                        },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContacts(
    onSearchBarClose: () -> Unit,
    onInput: (String) -> Unit
) {
    var searchText by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = null,
            modifier = Modifier
                .size(30.dp)
                .clickable { onSearchBarClose() }
        )

        Spacer(modifier = Modifier.width(16.dp))

        Surface(
            modifier = Modifier.weight(1f),
            color = Color.Transparent,
            shape = RoundedCornerShape(16.dp)
        ) {
            TextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    onInput(it)
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 18.sp,fontFamily = FontFamily(Font((R.font.lexend_regular)))),
                placeholder = { Text(text = "Search", style = TextStyle(fontSize = 18.sp,fontFamily = FontFamily(Font((R.font.lexend_regular))))) },
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable

fun ContactDisplay(name:String, number:String, addContactPresenter: AddContactPresenter, select:(String,String,String) -> Unit){
    var image by remember { mutableStateOf<Painter?>(null) }
    var imageLink by remember { mutableStateOf("") }
    val context = LocalContext.current
    LaunchedEffect(addContactPresenter){
        addContactPresenter.getDPLink(number).collect{
            imageLink = it
        }
    }
    image = if (imageLink.isNotEmpty()) {
        rememberImagePainter(data = imageLink)
    } else {
        painterResource(id = R.drawable.no_dp_logo)
    }
    Row(modifier = Modifier
        .fillMaxSize()
        .clickable {
            if (imageLink.isNotEmpty()) {
                select(name,number, imageLink)
            }
        },
        verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = image!!, contentScale = ContentScale.Crop, contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
        )
        Text(text = name,
            style = TextStyle(fontFamily = FontFamily(Font((R.font.lexend_regular))), fontSize = 20.sp),
            modifier = Modifier.padding(start = 8.dp))
        if(imageLink.isEmpty()){
            Row(modifier = Modifier
                .fillMaxSize(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically) {
                Row(modifier = Modifier.clickable { addContactPresenter.sendInvite(number, context) }) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "new group",
                        tint = if(isSystemInDarkTheme()){
                            Color(0xFF13B900)
                        }else{
                            Color(0xFF2ECE1B)
                        },
                        modifier = Modifier
                            .size(30.dp)
                            .padding(top = 4.dp))
                    Text("Invite",
                        style = TextStyle(fontFamily = FontFamily(Font((R.font.lexend_regular))), fontSize = 20.sp),
                        color = if(isSystemInDarkTheme()){
                            Color(0xFF13B900)
                        }else{
                            Color(0xFF2ECE1B)
                        },)
                }
            }
        }
    }
}
