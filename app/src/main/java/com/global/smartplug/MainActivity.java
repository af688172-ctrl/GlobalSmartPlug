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
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    Switch master,o1,o2,o3,o4; TextView masterState,wifiStatus; static final int REQ=10; WifiManager wifi;
    public void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_main);master=findViewById(R.id.master);o1=findViewById(R.id.o1);o2=findViewById(R.id.o2);o3=findViewById(R.id.o3);o4=findViewById(R.id.o4);masterState=findViewById(R.id.masterState);wifiStatus=findViewById(R.id.wifiStatus);wifi=(WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        master.setOnCheckedChangeListener((v,on)->{o1.setChecked(on);o2.setChecked(on);o3.setChecked(on);o4.setChecked(on);masterState.setText(on?"الحالة: تشغيل":"الحالة: إيقاف");masterState.setTextColor(Color.parseColor(on?"#63E63D":"#9AAAB4"));});
        findViewById(R.id.addDevice).setOnClickListener(v->scanWifi()); findViewById(R.id.wifiSetup).setOnClickListener(v->scanWifi()); updateWifiStatus();}
    void updateWifiStatus(){wifiStatus.setText(wifi!=null&&wifi.isWifiEnabled()?"Wi-Fi: مفعّل":"Wi-Fi: غير مفعّل");}
    void scanWifi(){
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission("android.permission.NEARBY_WIFI_DEVICES")!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{"android.permission.NEARBY_WIFI_DEVICES"},REQ);return;}
        if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQ);return;}
        if(wifi==null||!wifi.isWifiEnabled()){new AlertDialog.Builder(this).setTitle("Wi-Fi مغلق").setMessage("شغّل Wi-Fi في الهاتف ثم اضغط إعادة البحث.").setPositiveButton("إعدادات Wi-Fi",(d,w)->startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS))).setNegativeButton("إلغاء",null).show();return;}
        wifi.startScan(); new Handler().postDelayed(this::showWifiList,1500);
    }
    void showWifiList(){
        List<ScanResult> results=wifi.getScanResults(); LinkedHashMap<String,ScanResult> unique=new LinkedHashMap<>();
        for(ScanResult r:results) if(r.SSID!=null&&!r.SSID.trim().isEmpty()) unique.put(r.SSID,r);
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(16,8,16,8);
        TextView info=new TextView(this); info.setText("📶 شبكات Wi-Fi القريبة\nاختر شبكة جهاز المشترك:"); info.setTextSize(17); info.setPadding(0,0,0,12); box.addView(info);
        for(ScanResult r:unique.values()){Button btn=new Button(this); btn.setText("📶  "+r.SSID+"\n"+(r.capabilities==null||r.capabilities.isEmpty()?"مفتوحة":"محمية")+"  •  "+r.level+" dBm"); btn.setOnClickListener(v->connectSelected(r.SSID)); box.addView(btn);}
        if(unique.isEmpty()){TextView empty=new TextView(this);empty.setText("لم يتم العثور على شبكات. قرّب الهاتف من جهاز المشترك، وتأكد أن Wi-Fi والموقع مفعّلان.");empty.setTextSize(16);empty.setPadding(10,25,10,25);box.addView(empty);}
        ScrollView scroll=new ScrollView(this);scroll.addView(box);
        new AlertDialog.Builder(this).setTitle("إضافة جهاز — البحث عن Wi-Fi").setView(scroll).setPositiveButton("🔄 إعادة البحث",(d,w)->scanWifi()).setNegativeButton("إلغاء",null).show();
    }
    void connectSelected(String ssid){
        final EditText pass=new EditText(this); pass.setHint("كلمة مرور الشبكة (إن وجدت)"); pass.setInputType(0x00000081);
        new AlertDialog.Builder(this).setTitle("اختيار جهاز المشترك").setMessage("شبكة Wi-Fi:\n"+ssid).setView(pass).setPositiveButton("اتصال",(d,w)->Toast.makeText(this,"تم اختيار "+ssid+". سيكمل Android الاتصال بالشبكة.",Toast.LENGTH_LONG).show()).setNegativeButton("إلغاء",null).show();
    }
}
