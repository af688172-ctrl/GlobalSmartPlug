package com.global.smartplug;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {
    Switch master,o1,o2,o3,o4;
    TextView masterState;
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        master=findViewById(R.id.master); o1=findViewById(R.id.o1); o2=findViewById(R.id.o2);
        o3=findViewById(R.id.o3); o4=findViewById(R.id.o4); masterState=findViewById(R.id.masterState);

        master.setOnCheckedChangeListener((v,on)->{
            o1.setChecked(on); o2.setChecked(on); o3.setChecked(on); o4.setChecked(on);
            masterState.setText(on ? "الحالة: تشغيل" : "الحالة: إيقاف");
            masterState.setTextColor(Color.parseColor(on ? "#63E63D" : "#9AAAB4"));
        });

        findViewById(R.id.addDevice).setOnClickListener(v->showAddDevice());
    }
    void showAddDevice(){
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(30,10,30,10);
        EditText name=new EditText(this); name.setHint("اسم الجهاز"); box.addView(name);
        EditText mac=new EditText(this); mac.setHint("MAC Address"); box.addView(mac);
        EditText sn=new EditText(this); sn.setHint("Serial Number"); box.addView(sn);
        new AlertDialog.Builder(this).setTitle("إضافة جهاز").setView(box)
          .setPositiveButton("حفظ", (d,w)->Toast.makeText(this,"تم حفظ الجهاز محليًا",Toast.LENGTH_SHORT).show())
          .setNegativeButton("إلغاء",null).show();
    }
}
