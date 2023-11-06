package arush.baatcheet.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import arush.baatcheet.view.ui.theme.BaatcheetTheme
import arush.baatcheet.R
import arush.baatcheet.model.BackgroundService
import arush.baatcheet.presenter.HomeScreenPresenter
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter

class MainActivity : ComponentActivity() {

    override fun onStart() {
        super.onStart()
        val serviceIntent = Intent(applicationContext, BackgroundService::class.java)
        stopService(serviceIntent)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BaatcheetTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        val serviceIntent = Intent(applicationContext, BackgroundService::class.java)
        startService(serviceIntent)
    }
}

@Composable
fun MainScreen() {
    var isSearchBarActive by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val homeScreenPresenter = HomeScreenPresenter.getInstance(context)

    Box {
        Column {
            if (isSearchBarActive) {
                BackHandler {
                    isSearchBarActive = false
                }
                SearchBar(
                    onSearchQueryChange = {
                        searchText = it
                    },
                    onSearchBarClose = {
                        searchText = ""
                        isSearchBarActive = false
                    }
                )
            } else {
                AppBar( homeScreenPresenter,
                    onSearchIconClick = {
                        isSearchBarActive = true
                    }
                )
            }
            Divider(
                color = Gray,
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )
            ChatList(homeScreenPresenter, searchText)
        }

        FloatingActionButton(
            onClick = {
                val intent = Intent(context, AddContactActivity::class.java)
                intent.putExtra("myNum", homeScreenPresenter.myNum)
                context.startActivity(intent)
            },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun AppBar(homeScreenPresenter: HomeScreenPresenter, onSearchIconClick: () -> Unit) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = stringResource(id = R.string.Baatcheet_title),
            style = TextStyle(
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily(Font((R.font.lexend_regular))),
                color = MaterialTheme.colorScheme.secondary
            )
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier
                .size(30.dp)
                .clickable {
                    onSearchIconClick()
                }
        )
        Spacer(modifier = Modifier.width(16.dp))
        Image(
            painter = rememberImagePainter(data = homeScreenPresenter.getMyDp()),
            contentScale = ContentScale.Crop,
            contentDescription = null,
            modifier = Modifier
                .size(42.dp)
                .fillMaxSize()
                .clip(CircleShape)
                .clickable {
                    val intent = Intent(context, ProfileActivity::class.java)
                    context.startActivity(intent)
                    /*TODO*/
                }
        )
    }
}

@Composable
fun ChatList(homeScreenPresenter: HomeScreenPresenter, searchText: String) {
    var homeData by remember { mutableStateOf(homeScreenPresenter.getMessageListFile()) }
    var tempHomeData by remember { mutableStateOf(homeScreenPresenter.getMessageListFile()) }
    var chatsData by remember { mutableStateOf(homeData.keys.toList()) }
    val context = LocalContext.current

    LaunchedEffect(homeScreenPresenter) {
        homeScreenPresenter.getMessageList().collect{
            val newData = it.toMutableMap()
            for (item in homeData) {
                if (item.key !in newData) {
                    newData[item.key] = item.value
                }
            }
            homeData = newData
            homeScreenPresenter.setMessageList(homeData)
        }
    }
    if(homeData.isNotEmpty()){
        chatsData = homeData.keys.toList().filter { contact ->
            contact.contains(searchText, ignoreCase = true)
        }
        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            items(chatsData) { chat ->
                homeData[chat]?.get("messages")?.let {
                    if (it.last()["message"] != tempHomeData[chat]?.get("messages")?.last()
                            ?.get("message")
                    ) {
                        if(chat in tempHomeData){
                            ChatListItem(
                                chat, it, homeScreenPresenter, it.size, context
                            )
                        }
                        else {
                            if(chat.length>13){
                                ChatListItem(chat, ArrayList(), homeScreenPresenter, 0, context)
                            }else{
                                ChatListItem(chat, it, homeScreenPresenter, it.size, context)
                            }
                        }
                    } else {
                        ChatListItem(chat, it, homeScreenPresenter, 0, context)
                    }

                }

                Divider(
                    color = Gray,
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun ChatListItem(contact: String, messages: ArrayList<HashMap<String, Any>>,
                 homeScreenPresenter: HomeScreenPresenter, msgChange: Int, context: Context) {
    var image by remember { mutableStateOf<Painter?>(null) }
    var imageLink by remember { mutableStateOf("") }
    var msgCount by remember { mutableIntStateOf(msgChange) }
    var contactDisplay : String = if(contact.length > 15) {
        contact.substring(20)
    }
    else if(homeScreenPresenter.myNum == contact){
        "Me (You)"
    } else {
        val name = homeScreenPresenter.getContactName(contact, context.contentResolver)
        if(name.isNullOrEmpty()) contact
        else name
    }
    LaunchedEffect(homeScreenPresenter ){
        homeScreenPresenter.getDPLink(contact).collect{
            imageLink = it
        }
    }
    image = if (imageLink.isNotEmpty()) {
        rememberImagePainter(data = imageLink)
    } else {
        painterResource(id = R.drawable.no_dp_logo)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("contactName", contactDisplay)
                intent.putExtra("contactNumber", contact)
                intent.putExtra("imageLink", imageLink)
                context.startActivity(intent)
                msgCount = 0
            }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = image!!,
            contentScale = ContentScale.Crop,
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = contactDisplay,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.secondary,
                fontFamily = FontFamily(Font((R.font.lexend_regular))),
            )
            if (messages.isNotEmpty()){
                messages.last()["message"]?.let {
                    var lastMsg = homeScreenPresenter.getDecrypted(it.toString())
                    Text(
                        text = if (lastMsg == "Error with decryption key"){""}
                        else
                        {
                            lastMsg.substring(13)
                        },
                        color = if(isSystemInDarkTheme()){
                            LightGray
                        }else{
                            Gray},
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = FontFamily(Font((R.font.lexend_regular))))
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        msgCount = msgChange
        CustomBadge(msgCount)
    }
}

@Composable
fun CustomBadge(unreadCount: Int) {
    if (unreadCount > 0) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(Color(0xFF3AC948), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$unreadCount",
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                fontFamily = FontFamily(Font((R.font.lexend_regular))),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    onSearchQueryChange: (String) -> Unit,
    onSearchBarClose: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
                    onSearchQueryChange(it)
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 16.sp,fontFamily = FontFamily(Font((R.font.lexend_regular))),),
                placeholder = { Text(text = "Search") },
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



@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    BaatcheetTheme {
        MainScreen()
    }
}
