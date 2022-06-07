package com.example.smartLED

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.madrapps.pikolo.ColorPicker
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    private var currentColor: Int = -6871500
    private val dataStoreManager: DataStoreManager by lazy { DataStoreManager(this) }
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)

        val colorPicker: ColorPicker = findViewById(R.id.mainColorPicker)
        val colorBox: View = findViewById(R.id.colorBox)

        scope.launch {
            val color = dataStoreManager.colorFlow
            color.collect {
                colorPicker.setColor(it)
                colorBox.backgroundTintList = ColorStateList.valueOf(it)
                currentColor = it
                Log.d("onCreate", "Color: $it")
            }
        }

        colorPicker.setColorSelectionListener(object : SimpleColorSelectionListener() {
            override fun onColorSelected(color: Int) {
//                colorBox.setBackgroundColor(color)
                Log.d("Difference", abs(color - currentColor).toString())
                colorBox.backgroundTintList = ColorStateList.valueOf(color)
//                val hslColor: FloatArray = FloatArray(3)
//                ColorUtils.colorToHSL(color, hslColor)
//                Log.d("RED", color.red.toString())
//                Log.d("GREEN", color.green.toString())
//                Log.d("BLUE", color.blue.toString())

                if (
                    (abs(color.red - currentColor.red) > 50)
                    or (abs(color.green - currentColor.green) > 50)
                    or (abs(color.blue - currentColor.blue) > 50)
                ) {
                    Log.d("Threshold", "Skipping...")
                    currentColor = color
                    return
                }


                Log.d(
                    "COLOR",
                    color.red.toString() + " " + color.green.toString() + " " + color.blue.toString()
                )
                currentColor = color
                Log.d("onColorSelected", currentColor.toString())


                CoroutineScope(Dispatchers.IO).launch {
                    kotlin.runCatching {
//                        val serverSocket = DatagramSocket()
//                        serverSocket.broadcast = true
//
//                        val message = "hello world\n".toByteArray()
                        val message = ByteBuffer.allocate(4).putInt(color).array()
                        val serverSocket = DatagramSocket()
                        serverSocket.send(
                            DatagramPacket(
                                message,
                                message.size,
                                InetAddress.getByName("192.168.0.80"),
                                3333
                            )
                        )
                        serverSocket.close()
                    }
                }
            }
        })

    }

    override fun onPause() {
        Log.d("onPause", "Saving color...")
        scope.launch {
            dataStoreManager.saveToDataStore(currentColor)
        }

        super.onPause()
    }
}
