package com.watchcats.bilisharehelper

import android.app.Activity
import android.content.*
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CopyAll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.watchcats.bilisharehelper.ui.theme.BiliShareHelperTheme
import com.watchcats.bilisharehelper.url.restoration.GetURLFromText
import com.watchcats.bilisharehelper.url.restoration.URLRestorationClass

class ShareReceiverActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BiliShareHelperTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ShareLink()
                }
            }
        }
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
private fun ShareLink(){
    val context = LocalContext.current
    val activity = context.findActivity()
    val intent = activity?.intent

    val settingsPreference = context.getSharedPreferences("Settings", 0)
    val isAutoShare by remember {
        mutableStateOf(settingsPreference.getBoolean("isAutoShare", true))
    }
    val isReplaceB23 by remember {
        mutableStateOf(settingsPreference.getBoolean("isReplaceB23", false))
    }

    var displayUrl by remember {
        mutableStateOf("正在转换中")
    }

    var isCopyDisplay by remember {
        mutableStateOf(false)
    }

    var isSheetDisplay by remember {
        mutableStateOf(false)
    }

    if (intent?.action == Intent.ACTION_SEND || intent?.action == Intent.ACTION_VIEW) {

        val runnable = Runnable {
            run {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                val inputUrl = text?.let { GetURLFromText.getURL(it) }
                val urlRestoration = URLRestorationClass()

                when (val originalUrl = urlRestoration.getOriginalUrl(inputUrl)) {
                    "Connect Error" -> {
                        displayUrl = "无法连接至服务器"
                        isCopyDisplay = false
                    }

                    "Error" -> {
                        displayUrl = "内部错误"
                        isCopyDisplay = false
                    }

                    "No matching URL" -> {
                        displayUrl = "没有匹配的URL"
                        isCopyDisplay = false
                    }

                    "Fail to find URL" -> {
                        displayUrl = "请分享/打开正确的URL"
                        isCopyDisplay = false
                    }

                    else -> {
                        displayUrl = if (isReplaceB23) GetURLFromText.replaceBiliToB23(originalUrl)
                        else originalUrl
                        isCopyDisplay = true
                    }
                }

                if (!isSheetDisplay && isAutoShare){
                    when (intent.action){
                        Intent.ACTION_SEND ->{
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, displayUrl)
                                type = "text/plain"
                            }
                            val actionIntent = Intent.createChooser(shareIntent, null)

                            context.startActivity(actionIntent)
                            isSheetDisplay = true
                        }

                        Intent.ACTION_VIEW ->{
                            val uri = Uri.parse(displayUrl)
                            val viewIntent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(viewIntent)

                            isSheetDisplay = true
                        }
                    }
                }
            }
        }

        Thread(runnable).start()
    } else {
        displayUrl = "请通过打开链接/分享方式启动本活动"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = displayUrl,
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(vertical = 20.dp)
        )

        if(isCopyDisplay){
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
                    Text(text = "复制链接")
                }
            }

            Row(
                modifier = Modifier.padding(vertical = 10.dp)
            ){
                Button(onClick = {
                    val uri = Uri.parse(displayUrl)
                    val viewIntent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(viewIntent)
                }) {
                    Text("打开")
                }

                Spacer(modifier = Modifier.width(20.dp))

                Button(onClick = {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, displayUrl)
                        type = "text/plain"
                    }
                    val actionIntent = Intent.createChooser(shareIntent, null)

                    context.startActivity(actionIntent)
                }) {
                    Text("分享")
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 480)
@Composable
fun DefaultPreview2() {
    BiliShareHelperTheme {
        ShareLink()
    }
}