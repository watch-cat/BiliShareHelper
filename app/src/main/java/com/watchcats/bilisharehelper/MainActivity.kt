package com.watchcats.bilisharehelper

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.CopyAll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.watchcats.bilisharehelper.ui.theme.BiliShareHelperTheme
import com.watchcats.bilisharehelper.url.restoration.GetAVorBVByAPI
import com.watchcats.bilisharehelper.url.restoration.GetURLFromText
import com.watchcats.bilisharehelper.url.restoration.URLRestorationClass


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BiliShareHelperTheme {
                // A surface container using the 'background' color from the theme
                MainNavigate()
            }
        }
    }

//    override fun onBackPressed() {
//        if () {
//
//        }else{
//            super.onBackPressed()
//        }
//    }
}

@Composable
private fun MainNavigate(){
    var isSettingsInterface by remember {
        mutableStateOf(false)
    }

    val callback = remember {
        object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                isSettingsInterface = !isSettingsInterface
                isEnabled = !isEnabled
            }
        }
    }
    val dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    DisposableEffect(key1 = Unit, effect = {
        dispatcher?.addCallback(callback)
        onDispose {
            callback.remove()
        }
    })

    val interfaceSwitch = {
        isSettingsInterface = !isSettingsInterface
        callback.isEnabled = !callback.isEnabled
    }

    if (!isSettingsInterface) MainColumn(interfaceSwitch, isSettingsInterface)
    else SettingsInterface(interfaceSwitch)
}

@Composable
private fun MainColumn(interfaceSwitch: () ->(Unit), isSettingsInterface: Boolean){
    Surface {
        Column {
            TitleBar(interfaceSwitch, isSettingsInterface, "??????????????????")
            Spacer(modifier = Modifier.height(10.dp))
            ShortURLTransformInterface()
            Spacer(modifier = Modifier.height(15.dp))
            Divider(modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .padding(horizontal = 2.dp),
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(15.dp))
            BVtoAVTransformInterface()
        }
    }
}

@Composable
private fun SettingsInterface(interfaceSwitch: () ->(Unit)){
    val context = LocalContext.current
    val settingsPreference = context.getSharedPreferences("Settings", 0)
    val settingsEditor = settingsPreference.edit()

    var isAutoShare by remember {
        mutableStateOf(settingsPreference.getBoolean("isAutoShare", true))
    }
    var isReplaceB23 by remember {
        mutableStateOf(settingsPreference.getBoolean("isReplaceB23", false))
    }

    Column {
        TitleBar(interfaceSwitch = interfaceSwitch, isSettingsInterface = true, titleText = "??????")

        Row(
            modifier = Modifier.padding(vertical = 20.dp)
        ) {
            Text(
                text = "?????????????????????????????????????????????/??????",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = isAutoShare,
                onCheckedChange = {
                    isAutoShare = !isAutoShare
                    settingsEditor.putBoolean("isAutoShare", isAutoShare).apply()
                }
            )
        }

        Row(
            modifier = Modifier.padding(vertical = 20.dp)
        ) {
            Text(
                text = "??????b23.tv??????www.bilibili.com????????????",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = isReplaceB23,
                onCheckedChange = {
                    isReplaceB23 = !isReplaceB23
                    settingsEditor.putBoolean("isReplaceB23", isReplaceB23).apply()
                }
            )
        }
    }
}

@Composable
private fun TitleBar(interfaceSwitch: () ->(Unit), isSettingsInterface: Boolean, titleText: String){
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSettingsInterface){
            IconButton(onClick = interfaceSwitch) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back to main interface"
                )
            }
        }

        Text(
            text = titleText,
            modifier = Modifier
                .padding(vertical = 10.dp)
                .weight(1f),
            style = MaterialTheme.typography.h3
        )

        if (!isSettingsInterface){
            IconButton(onClick = interfaceSwitch) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colors.primary
                )
            }
        }
    }
}

@Composable
private fun ShortURLTransformInterface(){
    val context = LocalContext.current
    val settingsPreference = context.getSharedPreferences("Settings", 0)
    val isReplaceB23 by remember {
        mutableStateOf(settingsPreference.getBoolean("isReplaceB23", false))
    }

    var shortURLInput by remember{
        mutableStateOf("")
    }

    var originalUrl by remember {
        mutableStateOf("")
    }

    var displayUrl by remember {
        mutableStateOf("")
    }

    var isUrlCopyDisplay by remember {
        mutableStateOf(false)
    }

    Column {
        Text(
            text = "???????????????(????????????)",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(horizontal = 5.dp)
        )

        Row(
            modifier = Modifier
                .padding(10.dp)
                .height(50.dp)
        ) {
            TextField(
                value = shortURLInput,
                placeholder = @Composable{ Text(text = "?????????b23.tv?????????")},
                onValueChange = {shortURLInput = it},
                modifier = Modifier
                    .weight(1f)
            )

            IconButton(onClick = {
                val runnable = Runnable{
                    kotlin.run {
                        val urlRestoration = URLRestorationClass()
                        originalUrl = urlRestoration.getOriginalUrl(GetURLFromText.getURL(shortURLInput))
                        when (originalUrl){
                            "Connect Error" -> {
                                displayUrl = "????????????????????????"
                                isUrlCopyDisplay = false
                            }

                            "Error" -> {
                                displayUrl = "????????????"
                                isUrlCopyDisplay = false
                            }

                            "No matching URL" -> {
                                displayUrl = "???????????????URL"
                                isUrlCopyDisplay = false
                            }

                            "Fail to find URL" -> {
                                displayUrl = "??????????????????URL"
                                isUrlCopyDisplay = false
                            }

                            else -> {
                                displayUrl = if (isReplaceB23) GetURLFromText.replaceBiliToB23(originalUrl)
                                else originalUrl
                                isUrlCopyDisplay = true
                            }
                        }
                    }
                }

                Thread(runnable).start()
            }) {
                Icon(
                    imageVector = Icons.Rounded.ArrowForwardIos,
                    contentDescription = "Convert the URL you just inputted",
                    tint = MaterialTheme.colors.primary,
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (originalUrl != ""){
                Text(
                    text = displayUrl,
                    style = MaterialTheme.typography.h6
                )
            }

            if (isUrlCopyDisplay){
                Button(onClick = {
                    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData = ClipData.newPlainText("Converted video URL", displayUrl)
                    clipboardManager.setPrimaryClip(clipData)
                }) {
                    Row {
                        Icon(
                            imageVector = Icons.Rounded.CopyAll,
                            contentDescription = "Copy the converted URL"
                        )
                        Text(text = "????????????")
                    }
                }
            }
        }

    }
}

@Composable
private fun BVtoAVTransformInterface(){
    val context = LocalContext.current
    var avOrBvInput by remember {
        mutableStateOf("")
    }

    var isAvBvCopyDisplay by remember {
        mutableStateOf(false)
    }

    var avOrBvOutput by remember {
        mutableStateOf("")
    }

    var avOrBvDisplay by remember {
        mutableStateOf("")
    }

    Column {
        Text(
            text = "AV??????BV?????????",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(horizontal = 5.dp)
        )

        Row(
            modifier = Modifier
                .padding(10.dp)
                .height(50.dp)
        ) {
            TextField(
                value = avOrBvInput,
                placeholder = @Composable{ Text(text = "?????????AV/BV??????")},
                onValueChange = {avOrBvInput = it},
                modifier = Modifier
                    .weight(1f)
            )

            IconButton(onClick = {
                val runnable = Runnable{
                    kotlin.run {
                        avOrBvOutput =
                            if (avOrBvInput.substring(0,1) == "AV" || avOrBvInput.substring(0,1) == "av"){
                                GetAVorBVByAPI.getBV(avOrBvInput)
                            }else if (avOrBvInput.substring(0,1) == "BV" || avOrBvInput.substring(0,1) == "bv"){
                                GetAVorBVByAPI.getAV(avOrBvInput)
                            }else if (avOrBvInput.isDigitsOnly()){
                                GetAVorBVByAPI.getBV(avOrBvInput)
                            }else{
                                GetAVorBVByAPI.getAV(avOrBvInput)
                            }

                        when (avOrBvOutput){
                            "No video found" -> {
                                avOrBvDisplay = "??????????????????"
                                isAvBvCopyDisplay = false
                            }

                            "Connect Error" -> {
                                avOrBvDisplay = "????????????????????????"
                            }

                            else -> {
                                avOrBvDisplay = avOrBvOutput
                                isAvBvCopyDisplay = true
                            }
                        }
                    }
                }

                Thread(runnable).start()
            }) {
                Icon(
                    imageVector = Icons.Rounded.ArrowForwardIos,
                    contentDescription = "Convert the AV or BV number you just inputted",
                    tint = MaterialTheme.colors.primary,
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (avOrBvOutput != ""){
                Text(
                    text = avOrBvDisplay,
                    style = MaterialTheme.typography.h6
                )
            }

            if (isAvBvCopyDisplay){
                Button(onClick = {
                    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData = ClipData.newPlainText("AV or BV number", avOrBvDisplay)
                    clipboardManager.setPrimaryClip(clipData)
                }) {
                    Row {
                        Icon(
                            imageVector = Icons.Rounded.CopyAll,
                            contentDescription = "Copy the converted URL"
                        )
                        Text(text = "????????????")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 480)
@Composable
fun DefaultPreview() {
    BiliShareHelperTheme {
        MainNavigate()
    }
}