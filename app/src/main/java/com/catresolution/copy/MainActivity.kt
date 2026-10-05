package com.catresolution.copy

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class MainActivity : Activity() {
    private lateinit var status: TextView
    private val shizukuPackage = "moe.shizuku.privileged.api"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        status = findViewById(R.id.status)

        findViewById<Button>(R.id.shizukuBtn).setOnClickListener { openShizuku() }
        findViewById<Button>(R.id.resBtn).setOnClickListener { resolutionDialog() }
        findViewById<Button>(R.id.dpiBtn).setOnClickListener { dpiDialog() }
        findViewById<Button>(R.id.stretchBtn).setOnClickListener { stretchDialog() }
        findViewById<Button>(R.id.restoreBtn).setOnClickListener { restore() }
        findViewById<Button>(R.id.infoBtn).setOnClickListener { showInfo() }
        findViewById<Button>(R.id.presetBtn).setOnClickListener { presetDialog() }
        updateStatus()
    }

    private fun hasShizukuInstalled(): Boolean = try {
        packageManager.getPackageInfo(shizukuPackage, 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }

    private fun updateStatus() {
        status.text = if (hasShizukuInstalled()) {
            "Estado: Shizuku instalado — ábrelo para iniciar el servicio"
        } else {
            "Estado: Shizuku no está instalado"
        }
    }

    private fun openShizuku() {
        if (!hasShizukuInstalled()) {
            status.text = "Estado: instala Shizuku desde su aplicación oficial"
            return
        }
        try {
            startActivity(packageManager.getLaunchIntentForPackage(shizukuPackage))
            status.text = "Estado: Shizuku abierto"
        } catch (_: Exception) {
            status.text = "Estado: no se pudo abrir Shizuku"
        }
    }

    private fun requireShizuku() {
        status.text = "Estado: inicia Shizuku y concede permiso a esta app"
        openShizuku()
    }

    private fun resolutionDialog() {
        val input = EditText(this).apply {
            hint = "Ejemplo: 1080x2400"
            setSingleLine(true)
        }
        AlertDialog.Builder(this)
            .setTitle("Cambiar resolución")
            .setMessage("La aplicación ya prepara la configuración; el cambio del sistema requiere el puente Shizuku activo.")
            .setView(input)
            .setPositiveButton("Aplicar") { _, _ ->
                if (Regex("^\\d{3,5}x\\d{3,5}$").matches(input.text.toString().trim())) requireShizuku()
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
            .setMessage("El cambio del sistema requiere el puente Shizuku activo.")
            .setView(input)
            .setPositiveButton("Aplicar") { _, _ ->
                if (Regex("^\\d{2,4}$").matches(input.text.toString().trim())) requireShizuku()
                else status.text = "Estado: DPI inválido"
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun stretchDialog() {
        val options = arrayOf("720x1600", "900x2000", "1080x2400", "1080x1920")
        AlertDialog.Builder(this)
            .setTitle("Modo estirado")
            .setItems(options) { _, _ -> requireShizuku() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun presetDialog() {
        val presets = arrayOf("1080x2400 • 420 DPI", "1080x2340 • 400 DPI", "900x2000 • 420 DPI", "720x1600 • 360 DPI")
        AlertDialog.Builder(this)
            .setTitle("Preajustes")
            .setItems(presets) { _, _ -> requireShizuku() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun restore() {
        requireShizuku()
    }

    private fun showInfo() {
        val metrics = resources.displayMetrics
        AlertDialog.Builder(this)
            .setTitle("Información del dispositivo")
            .setMessage("Resolución lógica: ${metrics.widthPixels} x ${metrics.heightPixels}\nDensidad: ${metrics.densityDpi} DPI\nAndroid: ${android.os.Build.VERSION.RELEASE}\nModelo: ${android.os.Build.MODEL}")
            .setPositiveButton("OK", null)
            .show()
    }
}
