package com.example.arduino_makerton

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.util.UUID


class MainActivity : AppCompatActivity() {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var bluetoothDevice: BluetoothDevice
    private var bluetoothSocket: BluetoothSocket? = null
    private val btUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private lateinit var tvBluetoothStatus: TextView
    private lateinit var tvPressureData1: TextView
    private lateinit var tvPressureData2: TextView
    private lateinit var tvParkingStatus: TextView
    private lateinit var btnConnectBluetooth: Button
    private lateinit var searchButton: Button
    private lateinit var simpleChronometer: Chronometer
    private lateinit var bikeOne: ImageView
    private lateinit var bikeTwo: ImageView
    private lateinit var parkingRule: Button

    private var parkingLot1 = false
    private var parkingLot2 = false
    private var parkingLotOneStatus = 0
    private var parkingLotTwoStatus = 0
    private val parkingTimeCheckInterval = 10000L // 10초마다 확인
    private var isCheckingParkingTime = false

    private val handler = Handler(Looper.getMainLooper())

    private val hc06MacAddress = "98:DA:40:02:C4:1C"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvBluetoothStatus = findViewById(R.id.tvBluetoothStatus)
        tvPressureData1 = findViewById(R.id.tvPressureData1)
        tvPressureData2 = findViewById(R.id.tvPressureData2)
        tvParkingStatus = findViewById(R.id.tvParkingStatus)
        btnConnectBluetooth = findViewById(R.id.btnConnectBluetooth)
        searchButton = findViewById(R.id.searchButton)
        simpleChronometer = findViewById(R.id.simpleChronometer)
        bikeOne = findViewById(R.id.bikeOne)
        bikeTwo = findViewById(R.id.bikeTwo)
        parkingRule = findViewById(R.id.parkingRule)

        bikeOne.visibility = GONE
        bikeTwo.visibility = GONE

        clickSearchButton()
        showRuleDialog()

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        btnConnectBluetooth.setOnClickListener {
            if (bluetoothAdapter == null) {
                Toast.makeText(this, "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_LONG).show()
            } else if (!bluetoothAdapter!!.isEnabled) {
                Toast.makeText(this, "블루투스가 비활성화되어 있습니다.", Toast.LENGTH_LONG).show()
            } else {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                        1
                    )
                } else {
                    connectToHC06()
                }
            }
        }
    }

    private fun connectToHC06() {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            if (device.address == hc06MacAddress) {
                bluetoothDevice = device
                try {
                    bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(btUUID)
                    bluetoothSocket?.connect()
                    tvBluetoothStatus.text = "Bluetooth : Connected"
                    Toast.makeText(this, "블루투스에 연결되었습니다.", Toast.LENGTH_SHORT).show()
                    listenForData()
                } catch (e: IOException) {
                    e.printStackTrace()
                    tvBluetoothStatus.text = "Bluetooth : Disconnected"
                    Toast.makeText(this, "블루투스에 연결할 수 없습니다.", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
        Toast.makeText(this, "HC-06 기기를 찾을 수 없습니다.", Toast.LENGTH_LONG).show()
    }

    private fun listenForData() {
        val inputStream = bluetoothSocket?.inputStream
        Thread {
            val buffer = ByteArray(1024)
            var bytes: Int

            while (true) {
                try {
                    bytes = inputStream?.read(buffer) ?: 0
                    val readMessage = String(buffer, 0, bytes).trim()
                    handler.post {
                        updatePressureData(readMessage)
                    }
                } catch (e: IOException) {
                    break
                }
            }
        }.start()
    }

    private fun updatePressureData(sensorData: String) {
        val data = sensorData.split(";")
        var sensor1Status = "압력 센서 데이터1: 없음"
        var sensor2Status = "압력 센서 데이터2: 없음"
        var parkingStatus = "주차장 상태: 비어있음"


        data.forEach {
            when {
                it.startsWith("SENSOR1:") -> {
                    sensor1Status = "압력 센서 데이터1: ${it.substringAfter("SENSOR1:")}"
                    if (it.contains("OCCUPIED") && !parkingLot1) {
                        parkingLot1 = true
                        simpleChronometer.base = SystemClock.elapsedRealtime()
                        simpleChronometer.start()
                        parkingStatus = "1번 주차장에 자전거가 주차되었습니다."
                        parkingLotOneStatus = 1
                        bikeOne.visibility = VISIBLE
                        Toast.makeText(this, "1번 주차장에 자전거가 주차되었습니다.", Toast.LENGTH_SHORT).show()
                        if (!isCheckingParkingTime) {
                            isCheckingParkingTime = true
                            startParkingTimeCheck()
                        }
                    } else if (it.contains("OCCUPIED") && parkingLot1) {
                        parkingLot1 = false
                        simpleChronometer.stop()
                        val elapsedTime = SystemClock.elapsedRealtime() - simpleChronometer.base
                        parkingStatus = "1번 주차장에서 ${elapsedTime / 1000}초 동안 주차되었습니다."
                        Toast.makeText(this, "1번 주차장에서 ${elapsedTime / 1000}초 동안 주차되었습니다.", Toast.LENGTH_SHORT).show()
                        bikeOne.visibility = GONE
                        parkingLotOneStatus = 0
                    }
                }

                it.startsWith("SENSOR2:") -> {
                    sensor2Status = "압력 센서 데이터2: ${it.substringAfter("SENSOR2:")}"
                    if (it.contains("OCCUPIED") && !parkingLot2) {
                        parkingLot2 = true
                        simpleChronometer.base = SystemClock.elapsedRealtime()
                        simpleChronometer.start()
                        bikeTwo.visibility = VISIBLE
                        parkingStatus = "2번 주차장에 자전거가 주차되었습니다."
                        parkingLotTwoStatus = 1
                        Toast.makeText(this, "2번 주차장에 자전거가 주차되었습니다.", Toast.LENGTH_SHORT).show()
                        if (!isCheckingParkingTime) {
                            isCheckingParkingTime = true
                            startParkingTimeCheck()
                        }
                    } else if (it.contains("OCCUPIED") && parkingLot2) {
                        parkingLot2 = false
                        simpleChronometer.stop()
                        val elapsedTime = SystemClock.elapsedRealtime() - simpleChronometer.base
                        parkingStatus = "2번 주차장에서 ${elapsedTime / 1000}초 동안 주차되었습니다."
                        Toast.makeText(this, "2번 주차장에서 ${elapsedTime / 1000}초 동안 주차되었습니다.", Toast.LENGTH_SHORT).show()
                        bikeTwo.visibility = GONE
                        parkingLotTwoStatus = 0
                    }
                }
            }
        }

        tvPressureData1.text = sensor1Status
        tvPressureData2.text = sensor2Status
        tvParkingStatus.text = parkingStatus
    }

    private fun startParkingTimeCheck() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                val elapsedTime = SystemClock.elapsedRealtime() - simpleChronometer.base
                if (parkingLot1 || parkingLot2) {
                    if (elapsedTime / 1000 > 15) {
                        showWarningDialog()
                    }
                    handler.postDelayed(this, parkingTimeCheckInterval)
                } else {
                    isCheckingParkingTime = false
                }
            }
        }, parkingTimeCheckInterval)
    }

    private fun showWarningDialog() {
        AlertDialog.Builder(this)
            .setTitle("경고")
            .setMessage("지정된 주차 시간이 지났습니다. \n 신속히 빼주시기 바랍니다.")
            .setPositiveButton("확인") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showRuleDialog() {
        parkingRule.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("이용 규칙")
                .setMessage(getString(R.string.rules))
                .setPositiveButton("확인") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                connectToHC06()
            } else {
                Toast.makeText(this, "블루투스 권한이 필요합니다.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun clickSearchButton() {
        searchButton.setOnClickListener {
            Intent(this, SearchBikeActivity::class.java).apply {
                putExtra("PARKING_LOT_ONE_STATUS", parkingLotOneStatus)
                putExtra("PARKING_LOT_TWO_STATUS", parkingLotTwoStatus)
                startActivity(this)
            }
        }
    }
}
