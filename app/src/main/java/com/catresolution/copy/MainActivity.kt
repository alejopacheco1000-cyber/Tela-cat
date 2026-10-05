package com.catresolution.copy

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : Activity() {
    private lateinit var status: TextView
    private val permissionCode = 42

    private val permissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == permissionCode) {
            status.text = if (grantResult == PackageManager.PERMISSION_GRANTED) {
                "Estado: Shizuku conectado ✓"
            } else {
                "Estado: permiso de Shizuku rechazado"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        status = findViewById(R.id.status)

        Shizuku.addRequestPermissionResultListener(permissionListener)

        findViewById<Button>(R.id.shizukuBtn).setOnClickListener { connectShizuku() }
        findViewById<Button>(R.id.resBtn).setOnClickListener { resolutionDialog() }
        findViewById<Button>(R.id.dpiBtn).setOnClickListener { dpiDialog() }
        findViewById<Button>(R.id.stretchBtn).setOnClickListener { stretchDialog() }
        findViewById<Button>(R.id.restoreBtn).setOnClickListener { restore() }
        findViewById<Button>(R.id.infoBtn).setOnClickListener { showInfo() }
        findViewById<Button>(R.id.presetBtn).setOnClickListener { presetDialog() }
        updateStatus()
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permissionListener)
        super.onDestroy()
    }

    private fun updateStatus() {
        status.text = when {
            !Shizuku.pingBinder() -> "Estado: Shizuku no está activo"
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED -> "Estado: Shizuku conectado ✓"
            else -> "Estado: Shizuku detectado — falta permiso"
        }
    }

    private fun connectShizuku() {
        if (!Shizuku.pingBinder()) {
            status.text = "Estado: instala/inicia Shizuku primero"
            try {
                startActivity(Intent().apply { `package` = "moe.shizuku.privileged.api" })
            } catch (_: Exception) { }
            return
        }
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            status.text = "Estado: Shizuku conectado ✓"
        } else {
            Shizuku.requestPermission(permissionCode)
        }
    }

    private fun shell(command: String): String? {
        if (!Shizuku.pingBinder() || Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            return null
        }
        return try {
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            val output = BufferedReader(InputStreamReader(process.inputStream)).readText().trim()
            process.waitFor()
            output
        } catch (_: Exception) {
            null
        }
    }

    private fun runCommand(command: String) {
        val result = shell(command)
        status.text = if (result != null) "Estado: configuración aplicada ✓" else "Estado: conecta Shizuku y vuelve a intentarlo"
    }

    private fun resolutionDialog() {
        val input = EditText(this).apply {
            hint = "Ejemplo: 1080x2400"
            setSingleLine(true)
        }
        AlertDialog.Builder(this)
            .setTitle("Cambiar resolución")
            .setMessage("Introduce ancho x alto")
            .setView(input)
            .setPositiveButton("Aplicar") { _, _ ->
                val value = input.text.toString().trim()
                if (Regex("^\\d{3,5}x\\d{3,5}$").matches(value)) runCommand("wm size $value")
                else status.text = "Estado: formato inválido"
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun dpiDialog() {
        val input = EditText(this).apply {
            hint = "Ejemplo: 420"
            setSingleLine(true)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        AlertDialog.Builder(this)
            .setTitle("Cambiar DPI")
            .setMessage("Introduce la densidad DPI")
            .setView(input)
            .setPositiveButton("Aplicar") { _, _ ->
                val value = input.text.toString().trim()
                if (Regex("^\\d{2,4}$").matches(value)) runCommand("wm density $value")
                else status.text = "Estado: DPI inválido"
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun stretchDialog() {
        val options = arrayOf("720x1600", "900x2000", "1080x2400", "1080x1920")
        AlertDialog.Builder(this)
            .setTitle("Modo estirado")
            .setItems(options) { _, which -> runCommand("wm size ${options[which]}") }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun presetDialog() {
        val presets = arrayOf("1080x2400 • 420 DPI", "1080x2340 • 400 DPI", "900x2000 • 420 DPI", "720x1600 • 360 DPI")
        AlertDialog.Builder(this)
            .setTitle("Preajustes")
            .setItems(presets) { _, which ->
                val values = arrayOf("1080x2400 420", "1080x2340 400", "900x2000 420", "720x1600 360")[which].split(" ")
                runCommand("wm size ${values[0]}")
                runCommand("wm density ${values[1]}")
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun restore() {
        if (!Shizuku.pingBinder() || Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            status.text = "Estado: conecta Shizuku para restaurar"
            return
        }
        shell("wm size reset")
        shell("wm density reset")
        status.text = "Estado: resolución y DPI restaurados ✓"
    }

    private fun showInfo() {
        val size = shell("wm size") ?: "No disponible"
        val density = shell("wm density") ?: "No disponible"
        AlertDialog.Builder(this)
            .setTitle("Información del dispositivo")
            .setMessage("Resolución actual:\n$size\n\nDensidad:\n$density\n\nAndroid: ${android.os.Build.VERSION.RELEASE}\nModelo: ${android.os.Build.MODEL}")
            .setPositiveButton("OK", null)
            .show()
    }
}
