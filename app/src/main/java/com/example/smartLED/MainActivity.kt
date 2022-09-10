package com.example.smartLED

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.madrapps.pikolo.ColorPicker
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    private var currentColor: Int = -6871500
    private var powerState: Boolean = false

    private val dataStoreManager: DataStoreManager by lazy { DataStoreManager(this) }
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.layout)

        val colorPicker: ColorPicker = findViewById(R.id.mainColorPicker)
        val powerButton: View = findViewById(R.id.powerButton)

        loadSettings(colorPicker, powerButton)

        powerButton.setOnClickListener {
            powerState = !powerState
            Log.d("PowerButton", "Power button state $powerState")
            updatePowerButton(powerButton)
        }

        colorPicker.setColorSelectionListener(object : SimpleColorSelectionListener() {
            override fun onColorSelected(color: Int) {
//                Log.d("onCreate", "ReadColor: ${currentColor.red} ${currentColor.green} ${currentColor.blue}")

//                Log.d("Difference", abs(color - currentColor).toString())
                powerButton.backgroundTintList = ColorStateList.valueOf(color)
                powerState = true
                if (
                    (abs(color.red - currentColor.red) > 50)
                    or (abs(color.green - currentColor.green) > 50)
                    or (abs(color.blue - currentColor.blue) > 50)
                ) {
                    Log.d("Threshold", "Skipping...")
                    currentColor = color
                    return
                }

//                Log.d(
//                    "COLOR",
//                    color.red.toString() + " " + color.green.toString() + " " + color.blue.toString()
//                )
                currentColor = color
                Log.d(
                    "onColorSelected",
                    "${currentColor.red} ${currentColor.green} ${currentColor.blue}"
                )

                sendColorRequest(color)

            }
        })

    }

    override fun onPause() {
        Log.d(
            "onPause",
            "Saving settings... color ${currentColor.red} ${currentColor.green} ${currentColor.blue}"
        )
        scope.launch {
            dataStoreManager.saveColor(currentColor)
            dataStoreManager.savePowerState(powerState)
        }

        super.onPause()
    }

    fun sendColorRequest(color: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            kotlin.runCatching {
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

    private fun loadSettings(colorPicker: ColorPicker, powerButton: View) {
        scope.launch {
            powerState = dataStoreManager.getPowerState().first()
            currentColor = dataStoreManager.getColor().first()
            withContext(Dispatchers.Main) {
                colorPicker.setColor(currentColor)
                powerButton.backgroundTintList = ColorStateList.valueOf(currentColor)
                updatePowerButton(powerButton)
            }
        }
    }

    private fun updatePowerButton(button: View) {
        if (powerState) {
            sendColorRequest(currentColor)
            button.backgroundTintList = ColorStateList.valueOf(currentColor)
        } else {
            sendColorRequest(0)
            button.backgroundTintList = ColorStateList.valueOf(14474460)
        }
    }
}
