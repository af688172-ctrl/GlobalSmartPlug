package com.voltra.app

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.*
import android.provider.Settings
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class WifiNetwork(val ssid:String,val bssid:String,val level:Int,val capabilities:String)

class WifiAdapter(private var data:List<WifiNetwork>,private val click:(WifiNetwork)->Unit):RecyclerView.Adapter<WifiAdapter.Holder>(){
 class Holder(v:android.view.View):RecyclerView.ViewHolder(v){val s:TextView=v.findViewById(R.id.txtSsid);val i:TextView=v.findViewById(R.id.txtInfo)}
 override fun onCreateViewHolder(p:android.view.ViewGroup,t:Int)=Holder(android.view.LayoutInflater.from(p.context).inflate(R.layout.item_wifi,p,false))
 override fun onBindViewHolder(h:Holder,n:Int){val x=data[n];h.s.text=x.ssid;h.i.text="الإشارة ${x.level} dBm • ${x.capabilities}";h.itemView.setOnClickListener{click(x)}}
 override fun getItemCount()=data.size
 fun setData(x:List<WifiNetwork>){data=x;notifyDataSetChanged()}
}

class MainActivity:AppCompatActivity(){
 lateinit var wm:WifiManager;lateinit var ad:WifiAdapter;lateinit var status:TextView;lateinit var progress:ProgressBar;lateinit var device:TextView
 val prefs by lazy{getSharedPreferences("voltra",MODE_PRIVATE)}
 val perms=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){scan()}
 val receiver=object:BroadcastReceiver(){override fun onReceive(c:Context?,i:Intent?){progress.visibility=ProgressBar.GONE;show()}}
 override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_main)
  wm=applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
  status=findViewById(R.id.txtStatus);progress=findViewById(R.id.progress);device=findViewById(R.id.txtDevice)
  ad=WifiAdapter(emptyList()){selected(it)}
  findViewById<RecyclerView>(R.id.recyclerWifi).apply{layoutManager=LinearLayoutManager(this@MainActivity);adapter=this@MainActivity.ad}
  findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAddDevice).setOnClickListener{begin()}
  ContextCompat.registerReceiver(this,receiver,IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION),ContextCompat.RECEIVER_NOT_EXPORTED)
  prefs.getString("ssid",null)?.let{device.visibility=TextView.VISIBLE;device.text="الجهاز: $it";status.text="محفوظ داخل التطبيق"}
 }
 fun begin(){if(!wm.isWifiEnabled){startActivity(Intent(Settings.ACTION_WIFI_SETTINGS));status.text="فعّل Wi‑Fi ثم اضغط إضافة جهاز";return}
  val p=if(Build.VERSION.SDK_INT>=33)arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES) else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
  val m=p.filter{ContextCompat.checkSelfPermission(this,it)!=PackageManager.PERMISSION_GRANTED}
  if(m.isNotEmpty())perms.launch(m.toTypedArray()) else scan()
 }
 fun scan(){progress.visibility=ProgressBar.VISIBLE;status.text="جاري البحث عن شبكات المشترك...";if(!wm.startScan()){progress.visibility=ProgressBar.GONE;show()}}
 fun show(){val r=try{wm.scanResults}catch(_:SecurityException){emptyList()};val n=r.filter{it.SSID.isNotBlank()}.distinctBy{it.SSID}.sortedByDescending{it.level}.map{WifiNetwork(it.SSID,it.BSSID?:"",it.level,it.capabilities)};ad.setData(n);status.text=if(n.isEmpty())"لم تظهر شبكات" else "اختر شبكة المشترك"}
 fun selected(n:WifiNetwork){prefs.edit().putString("ssid",n.ssid).putString("bssid",n.bssid).apply();device.visibility=TextView.VISIBLE;device.text="الجهاز: ${n.ssid}";status.text="تم حفظ الجهاز. الاتصال الفعلي يحتاج بيانات بروتوكول MTTL-W01.";Toast.makeText(this,"تمت إضافة ${n.ssid}",Toast.LENGTH_SHORT).show()}
 override fun onDestroy(){runCatching{unregisterReceiver(receiver)};super.onDestroy()}
}
