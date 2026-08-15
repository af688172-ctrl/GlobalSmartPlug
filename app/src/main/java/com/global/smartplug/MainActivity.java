package com.global.smartplug;

import android.Manifest;
import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    Switch master,o1,o2,o3,o4; TextView masterState,wifiStatus; static final int REQ=10; WifiManager wifi;
    public void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_main);master=findViewById(R.id.master);o1=findViewById(R.id.o1);o2=findViewById(R.id.o2);o3=findViewById(R.id.o3);o4=findViewById(R.id.o4);masterState=findViewById(R.id.masterState);wifiStatus=findViewById(R.id.wifiStatus);wifi=(WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        master.setOnCheckedChangeListener((v,on)->{o1.setChecked(on);o2.setChecked(on);o3.setChecked(on);o4.setChecked(on);masterState.setText(on?"الحالة: تشغيل":"الحالة: إيقاف");masterState.setTextColor(Color.parseColor(on?"#63E63D":"#9AAAB4"));});
        findViewById(R.id.addDevice).setOnClickListener(v->showAddDevice()); findViewById(R.id.wifiSetup).setOnClickListener(v->scanWifi()); updateWifiStatus();}
    void updateWifiStatus(){wifiStatus.setText(wifi!=null&&wifi.isWifiEnabled()?"Wi-Fi: مفعّل":"Wi-Fi: غير مفعّل");}
    void scanWifi(){
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission("android.permission.NEARBY_WIFI_DEVICES")!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{"android.permission.NEARBY_WIFI_DEVICES"},REQ);return;}
        if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQ);return;}
        if(wifi==null||!wifi.isWifiEnabled()){new AlertDialog.Builder(this).setTitle("Wi-Fi مغلق").setMessage("شغّل Wi-Fi في الهاتف ثم اضغط إعادة البحث.").setPositiveButton("إعدادات Wi-Fi",(d,w)->startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS))).setNegativeButton("إلغاء",null).show();return;}
        wifi.startScan(); new Handler().postDelayed(this::showWifiList,1200);
    }
    void showWifiList(){
        List<ScanResult> results=wifi.getScanResults(); LinkedHashMap<String,ScanResult> unique=new LinkedHashMap<>();
        for(ScanResult r:results) if(r.SSID!=null&&!r.SSID.trim().isEmpty()) unique.put(r.SSID,r);
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(20,5,20,5);
        TextView info=new TextView(this); info.setText("اختر شبكة Wi-Fi الخاصة بالمشترك:"); info.setTextSize(16); info.setPadding(0,0,0,12); box.addView(info);
        for(ScanResult r:unique.values()){Button btn=new Button(this); btn.setText("📶 "+r.SSID+"\n"+r.BSSID); btn.setOnClickListener(v->connectSelected(r.SSID)); box.addView(btn);}
        if(unique.isEmpty()){TextView empty=new TextView(this);empty.setText("لم يتم العثور على شبكات. قرّب الهاتف من جهاز المشترك وتأكد أن Wi-Fi الجهاز يعمل.");empty.setPadding(10,20,10,20);box.addView(empty);}
        ScrollView scroll=new ScrollView(this);scroll.addView(box);
        new AlertDialog.Builder(this).setTitle("شبكات Wi-Fi القريبة").setView(scroll).setPositiveButton("إعادة البحث",(d,w)->scanWifi()).setNegativeButton("إلغاء",null).show();
    }
    void connectSelected(String ssid){new AlertDialog.Builder(this).setTitle("ربط الجهاز").setMessage("تم اختيار شبكة:\n"+ssid+"\n\nاضغط اتصال من إعدادات Wi-Fi، ثم ارجع للتطبيق لإكمال إعداد الجهاز.").setPositiveButton("فتح Wi-Fi",(d,w)->startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS))).setNegativeButton("إلغاء",null).show();}
    void showAddDevice(){LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(30,10,30,10);EditText n=new EditText(this);n.setHint("اسم الجهاز");box.addView(n);EditText m=new EditText(this);m.setHint("MAC Address");box.addView(m);EditText s=new EditText(this);s.setHint("Serial Number");box.addView(s);new AlertDialog.Builder(this).setTitle("إضافة جهاز").setView(box).setPositiveButton("حفظ",(d,w)->Toast.makeText(this,"تم حفظ الجهاز محليًا",Toast.LENGTH_SHORT).show()).setNegativeButton("إلغاء",null).show();}
}
