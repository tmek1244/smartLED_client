package com.example.ledcontroller

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ToggleButton
import androidx.activity.ComponentActivity
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.example.ledcontroller.ui.theme.LEDcontrollerTheme
import com.madrapps.pikolo.ColorPicker
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener
import kotlinx.coroutines.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.Socket
import java.nio.ByteBuffer

class MainActivity : ComponentActivity() {
    var brightness: Int = 50
//    private  color: Int = 150.0
    var isOn: Boolean = false
//     pref: SharedPreferences = null


    override fun onCreate(savedInstanceState: Bundle?) {
//        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
//        brightness = sharedPref.getInt("BRIGHTNESS", 0)
//        color = sharedPref.getInt("COLOR", 0x000000).toFloat()
        val pref = applicationContext.getSharedPreferences("MyPref", Context.MODE_PRIVATE)
        val color = pref.getInt("COLOR", 0)
        Log.d("RED READ", color.toString())


        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)

        val colorPicker: ColorPicker = findViewById(R.id.mainColorPicker)
        val colorBox: View = findViewById(R.id.colorBox)


//        colorPicker.setColor(ColorUtils.HSLToColor(floatArrayOf(color.toFloat(), 1F, 0.5F)))
        colorPicker.setColor(color)
//        colorPicker.backgroundTintList = ColorStateList.valueOf(color)
//        colorBox.setBackgroundColor()
//        colorBox.backgroundTintList = ColorStateList.valueOf(ColorUtils.HSLToColor(floatArrayOf(color, 1F, 0.5F)))
        colorBox.backgroundTintList = ColorStateList.valueOf(color)

        colorPicker.setColorSelectionListener(object : SimpleColorSelectionListener() {
            override fun onColorSelected(color: Int) {
//                colorBox.setBackgroundColor(color)
                colorBox.backgroundTintList = ColorStateList.valueOf(color)
                val hslColor: FloatArray = FloatArray(3)
                ColorUtils.colorToHSL(color, hslColor)
                Log.d("RED", color.red.toString())
                Log.d("GREEN", color.green.toString())
                Log.d("BLUE", color.blue.toString())
                Log.d("COLOR", color.toString())

//                val pref = applicationContext.getSharedPreferences("MyPref", Context.MODE_PRIVATE)

                with (pref.edit()) {
                    putInt("COLOR", color)
                    commit()
                }

//                Log.d("Saturation", hslColor[1].toString())
//                Log.d("Hue", hslColor[0].toString())
//                colorText.text = color.toString()
                CoroutineScope(Dispatchers.IO).launch {
                    kotlin.runCatching {
//                        val serverSocket = DatagramSocket()
//                        serverSocket.broadcast = true
//
//                        val message = "hello world\n".toByteArray()
                        val message = ByteBuffer.allocate(4).putInt(color).array()
                        val serverSocket = DatagramSocket()
                        serverSocket.broadcast = true
                        serverSocket.send(DatagramPacket(message, message.size, InetAddress.getByName("192.168.0.80"), 3333))
                        serverSocket.close()

                    }
                }


            }
        })

    }
}


@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LEDcontrollerTheme {
        Greeting("Android")
    }
}