package com.nsmod.blockblast;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class FloatingService extends Service {
    private WindowManager wm;
    private View bubble;
    private WindowManager.LayoutParams params;

    @Override public void onCreate() {
        super.onCreate();
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return; }
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        TextView v=new TextView(this);
        v.setText("BB");
        v.setTextColor(0xffffffff);
        v.setTextSize(16);
        v.setGravity(Gravity.CENTER);
        v.setBackgroundColor(0xff1769aa);
        int type=Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE;
        params=new WindowManager.LayoutParams(54,54,type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        params.gravity=Gravity.TOP|Gravity.START;
        params.x=24; params.y=180;
        v.setOnClickListener(view -> {
            Intent i=new Intent(this,MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        });
        v.setOnTouchListener(new View.OnTouchListener() {
            float sx,sy; int ox,oy;
            public boolean onTouch(View view,MotionEvent e) {
                if(e.getAction()==MotionEvent.ACTION_DOWN){sx=e.getRawX();sy=e.getRawY();ox=params.x;oy=params.y;return false;}
                if(e.getAction()==MotionEvent.ACTION_MOVE){
                    params.x=ox+(int)(e.getRawX()-sx); params.y=oy+(int)(e.getRawY()-sy);
                    wm.updateViewLayout(bubble,params); return true;
                }
                return false;
            }
        });
        bubble=v; wm.addView(bubble,params);
    }
    @Override public int onStartCommand(Intent intent,int flags,int startId){return START_STICKY;}
    @Override public void onDestroy(){if(bubble!=null)wm.removeView(bubble);super.onDestroy();}
    @Override public IBinder onBind(Intent intent){return null;}
}
