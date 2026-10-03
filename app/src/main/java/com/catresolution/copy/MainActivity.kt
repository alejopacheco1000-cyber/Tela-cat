package com.catresolution.copy
import android.app.*
import android.os.Bundle
import android.widget.*
import com.rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
class MainActivity:Activity(){
 private lateinit var status:TextView
 private var originalSize:String?=null
 private var originalDensity:String?=null
 override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_main);status=findViewById(R.id.status);findViewById<Button>(R.id.resBtn).setOnClickListener{askResolution()};findViewById<Button>(R.id.dpiBtn).setOnClickListener{askDpi()};findViewById<Button>(R.id.stretchBtn).setOnClickListener{askStretch()};findViewById<Button>(R.id.applyBtn).setOnClickListener{status.text="Estado: usa Shizuku o Root para aplicar la configuración"};findViewById<Button>(R.id.restoreBtn).setOnClickListener{restore()};findViewById<Button>(R.id.infoBtn).setOnClickListener{info()};findViewById<Button>(R.id.shizukuBtn).setOnClickListener{requestShizuku()}}
 private fun requestShizuku(){if(!Shizuku.pingBinder()){status.text="Estado: Shizuku no está activo";return};if(Shizuku.checkSelfPermission()==0)status.text="Estado: Shizuku conectado" else Shizuku.requestPermission(100)}
 private fun shell(cmd:String):String?=try{val p=Shizuku.newProcess(arrayOf("sh","-c",cmd),null,null);val out=BufferedReader(InputStreamReader(p.inputStream)).readText();p.waitFor();out.trim()}catch(e:Exception){null}
 private fun apply(cmd:String){if(Shizuku.pingBinder()&&Shizuku.checkSelfPermission()==0){shell(cmd);status.text="Estado: aplicado"}else status.text="Estado: conecta Shizuku primero"}
 private fun askResolution(){val input=EditText(this);input.hint="Ej. 1080x2400";AlertDialog.Builder(this).setTitle("Cambiar resolución").setView(input).setPositiveButton("Aplicar"){_,_->val v=input.text.toString().trim();if(v.matches(Regex("\\d+x\\d+"))){originalSize=shell("wm size");apply("wm size $v")}else status.text="Formato inválido"}.setNegativeButton("Cancelar",null).show()}
 private fun askDpi(){val input=EditText(this);input.hint="Ej. 420";AlertDialog.Builder(this).setTitle("Cambiar DPI").setView(input).setPositiveButton("Aplicar"){_,_->val v=input.text.toString().trim();if(v.matches(Regex("\\d+"))){originalDensity=shell("wm density");apply("wm density $v")}else status.text="DPI inválido"}.setNegativeButton("Cancelar",null).show()}
 private fun askStretch(){AlertDialog.Builder(this).setTitle("Modo estirado").setMessage("Selecciona una resolución para conseguir el efecto deseado.").setPositiveButton("Configurar"){_,_->askResolution()}.setNegativeButton("Cancelar",null).show()}
 private fun restore(){originalSize?.let{Regex("(\\d+x\\d+)").find(it)?.groupValues?.get(1)?.let{v->apply("wm size $v")}};originalDensity?.let{Regex("(\\d+)").find(it)?.groupValues?.get(1)?.let{v->apply("wm density $v")}};status.text="Estado: restauración solicitada"}
 private fun info(){val size=shell("wm size")?:"no disponible";val dpi=shell("wm density")?:"no disponible";AlertDialog.Builder(this).setTitle("Información del dispositivo").setMessage("Resolución: $size\nDPI: $dpi\nAndroid: " + android.os.Build.VERSION.RELEASE).setPositiveButton("OK",null).show()}
}