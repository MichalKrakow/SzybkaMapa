package com.ranger.szybkamapa.app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ranger.szybkamapa.app.GlMapView.MyGlSurfaceView;
import com.ranger.szybkamapa.app.Map.AverageAngle;
import com.ranger.szybkamapa.app.Map.Map;
import com.ranger.szybkamapa.app.services.ReLocation;

import java.io.File;


public class MainActivity extends Activity implements View.OnClickListener, ServiceConnection,SensorEventListener {

    public final static String PREFERENCJE = "PREFERENCJE";
    public final static String SEND_LAT = "com.ranger.szybkamapa.app.lat";
    public final static String SEND_LON = "com.ranger.szybkamapa.app.lon";
    public static final int POBIERZ_PLIK_MAPY = 15002900;


    private Button btn_home, btn_orders,btn_mark, btn_gps,btn_map;
    private final Messenger mMessenger = new Messenger(new IncomingMessageHandler());
    private Location curLoc;
    private TextView textStatus, textIntValue, textStrValue, gps_status, gps_status_2, gps_status_3;
    private ServiceConnection mConnection = this;
    boolean mIsBound, gps_flag;
    private Messenger mServiceMessenger = null;

    private String mapFile;
    private int minScale,medScale,maxScale;

    private static SensorManager mSensorManager;
    private Sensor mSensorAccelerometer;
    private Sensor mSensorMagneticField;

    private double mMinDiffForEvent;
    private double mThrottleTime;

    private float[] mValuesAccelerometer;
    private float[] mValuesMagneticField;
    private float[] mMatrixR;
    private float[] mMatrixI;
    private float[] mMatrixValues;

    private double mAzimuth = Double.NaN;

    private AverageAngle mAzimuthRadians;

    private Sensor sensor;
    private float heading;
    private Bitmap arrow;
    private Map mapa;

    private MyGlSurfaceView glmap;

    float c;

    private Crd coordStart,coordEnd,coordPixelShift,mapPixelSize,coordEndPixelShift,positionCoords;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        resumeFromPreferences(); // Wczytaj dane konfiguracji

        setContentView(R.layout.activity_main);

        glmap = (MyGlSurfaceView) findViewById(R.id.glMapa);

        gps_status = (TextView) findViewById(R.id.gps_status);
        gps_status_2 = (TextView) findViewById(R.id.gps_status_2);
        gps_status_3 = (TextView) findViewById(R.id.gps_status_3);
        btn_gps = (Button) findViewById(R.id.btn_gps);

        positionCoords = new Crd(0,0);
        heading = 0;

        c = (float) 1.33754; // W H Y ?????????????????????
        c = 1;

        btn_gps.setOnClickListener(this);
        gps_flag = false;

        arrow = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mValuesAccelerometer = new float[3];
        mValuesMagneticField = new float[3];

        mMatrixR = new float[9];
        mMatrixI = new float[9];
        mMatrixValues = new float[3];

        mMinDiffForEvent = 2;
        mThrottleTime = 1000;

        mAzimuthRadians = new AverageAngle(36);

        //Drawable bitmap = getResources().getDrawable(R.drawable.blank_map);
        //mImageView.setImageDrawable(bitmap);
        //mAttacher = new PhotoViewAttacher(mImageView);

        if(mapFile != "")
        {
            setMap(mapFile);
        }


        /*   // TAK ROBIMY Z RENDEROWANIEM PRZY ZMIANIE OKNA
        mcl = new PhotoViewAttacher.OnMatrixChangedListener() {
            @Override
            public void onMatrixChanged(RectF rect) {
                if(mapa != null)
                if(dot !=null)
                {
                    if(gps_flag)
                    {
                        redrawAllShit();
                    }
                }
            }
        };
        */
        automaticBind();

    }
    private void showToastMsg(String message)
    {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }
    private void resumeFromPreferences()
    {
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFERENCJE,0);
        mapFile = prefs.getString("mapa","");
        minScale = prefs.getInt("minScale",2);
        medScale = prefs.getInt("medScale",4);
        maxScale = prefs.getInt("maxScale",10);
    }
    @Override
    protected void onStop()
    {
        super.onStop();
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFERENCJE,0);
        SharedPreferences.Editor editor = prefs.edit();
        if(mapa != null) {
            editor.putString("mapa", mapa.file.getName());
            editor.apply();
            Log.v("*MAPA ZAPIS:",mapa.file.getName());
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        glmap.onResume();

        mSensorManager.registerListener(this, mSensorAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mSensorMagneticField, SensorManager.SENSOR_DELAY_UI);
        Log.v("SENSOR","CONNECTED");
    }

    @Override
    protected void onPause() {
        super.onPause();
        glmap.onPause();

        mSensorManager.unregisterListener(this, mSensorAccelerometer);
        mSensorManager.unregisterListener(this, mSensorMagneticField);
        Log.v("SENSOR","DISconnected");
    }

    private void setMap(String mapFilePath)
    {
        Log.wtf("APKA",mapFilePath);

        try {
            String path = Environment.getExternalStorageDirectory().toString() + "/mapy";
            mapa = new Map(new File(path + "/" + mapFilePath));
            mapa.setupMap();
            glmap.setMap(mapa);
        }
        catch (Exception e)
        {
            showToastMsg("Błąd wczytwania mapy");
            Log.wtf("WCZYTYWANIE MAPY",e.getMessage());
            if(mapa.initialized)
                mapa.initialized = false; // przy bledach przy pobieraniu jpg zmien flage mapy na niezainicjowana

        }
    }
    private void redrawAllShit()
    {
        if(mIsBound && mapa != null)
        {
            if(!gps_flag)
            return;

            if(!mapa.initialized)
            return;

            //mapPixelSize.x = mapa.getMapCalibrationPoint(2).pxLoc.x - mapa.getMapCalibrationPoint(0).pxLoc.x;
            //mapPixelSize.y = mapa.getMapCalibrationPoint(2).pxLoc.y - mapa.getMapCalibrationPoint(0).pxLoc.y;

            //positionCoords.x = (((curLoc.getLongitude()-mapa.getMapCalibrationPoint(0).geoLoc.x)/(mapa.getMapCalibrationPoint(2).geoLoc.x-mapa.getMapCalibrationPoint(0).geoLoc.x))*mapPixelSize.x*c)+(mapa.getMapCalibrationPoint(0).pxLoc.x*c);
            //positionCoords.y = (((curLoc.getLatitude()-mapa.getMapCalibrationPoint(0).geoLoc.y)/(mapa.getMapCalibrationPoint(2).geoLoc.y-mapa.getMapCalibrationPoint(0).geoLoc.y)*mapPixelSize.y*c))+(mapa.getMapCalibrationPoint(0).pxLoc.y*c);

            /*
            matrix = mAttacher.getDrawMatrix();

            dotCanvas.setMatrix(matrix);
            dotCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
            p.setARGB(255, 200, 0, 0);
            dotCanvas.drawCircle((float) positionCoords.x, (float) positionCoords.y, 4, p);
            p.setARGB(50, 200, 0, 0);
            dotCanvas.drawCircle((float) positionCoords.x, (float) positionCoords.y, 16, p);
            p.setARGB(200, 200, 0, 0);
            float tx = (float) (50 * Math.cos((heading - 90) * (3.14 / 180)));
            float ty = (float) (50 * Math.sin((heading - 90) * (3.14 / 180)));
            tx += (float) positionCoords.x;
            ty += (float) positionCoords.y;
            p.setStrokeWidth(2);
            dotCanvas.drawLine((float) positionCoords.x, (float) positionCoords.y, tx, ty, p);
            p.setStrokeWidth(0);
            */

        }
    }

    @Override
    public void onClick(View v) {

        if(v.equals(btn_gps)) {
            if(!gps_flag)
            {
                doBindService();
            }
            else
            {
                gps_flag = false;

                doUnbindService();
                stopService(new Intent(MainActivity.this, ReLocation.class));

            }
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            super.onRestoreInstanceState(savedInstanceState);
        }
    }
    public void findMe()
    {

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.v("DOSTALEM", "jestcos");
        if(requestCode == POBIERZ_PLIK_MAPY) {
            if (resultCode == RESULT_OK) {
                String filename = data.getStringExtra("filename");
                Log.v("DOSTALEM", filename);
                setMap(filename);
            }
        }
    }
        @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_myLoc) {
            findMe();
            return true;
        }
        else if(id == R.id.action_map_list) {

            Intent intent = new Intent(this, MapSelectActivity.class);
            intent.putExtra(SEND_LAT, curLoc != null ? curLoc.getLatitude()+"" : "not-set");
            intent.putExtra(SEND_LON, curLoc != null ? curLoc.getLongitude()+"" : "not-set");
            startActivityForResult(intent,POBIERZ_PLIK_MAPY);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mServiceMessenger = new Messenger(service);
        //textStatus.setText("Attached.");
        try {
            Message msg = Message.obtain(null, ReLocation.MSG_REGISTER_CLIENT);
            msg.replyTo = mMessenger;
            mServiceMessenger.send(msg);
        }
        catch (RemoteException e) {
            // In this case the service has crashed before we could even do anything with it
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
        mServiceMessenger = null;
    }
    private void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mServiceMessenger != null) {
                try {
                    Message msg = Message.obtain(null, ReLocation.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
            gpsStopUiSet();
            //textStatus.setText("Unbinding.");
        }
    }

    private void gpsStopUiSet() {
        btn_gps.setSelected(false);
        gps_status.setText("GPS off");
        gps_status_2.setText("standby");
    }
    private void automaticBind() {
        if (ReLocation.isRunning()) {
            doBindService();
        }
    }
    private void doBindService() {
        bindService(new Intent(this, ReLocation.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        btn_gps.setSelected(true);
        //textStatus.setText("Binding.");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, mValuesAccelerometer, 0, 3);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, mValuesMagneticField, 0, 3);
                break;
        }

        boolean success = SensorManager.getRotationMatrix(mMatrixR, mMatrixI,
                mValuesAccelerometer,
                mValuesMagneticField);


        if (success) {
            SensorManager.getOrientation(mMatrixR, mMatrixValues);
            mAzimuthRadians.putValue(mMatrixValues[0]);
            mAzimuth = Math.toDegrees(mAzimuthRadians.getAverage());
            heading = (float) mAzimuth;
            redrawAllShit();
        }

        /*
        if(Math.abs(heading-event.values[0])>2) {
            heading = event.values[0];
        }
        */
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private class Crd {
        public double x;
        public double y;

        public Crd(double x, double y)
        {
            this.x = x;
            this.y = y;
        }
    }
    private class IncomingMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // Log.d(LOGTAG,"IncomingHandler:handleMessage");
            switch (msg.what) {
                case ReLocation.MSG_GPS_LOCATION:
                    String str = msg.getData().getString("gps");
                    gps_status.setText(str);
                    break;
                case ReLocation.MSG_GPS_LOCATION_OBJECT:
                    gps_flag = true;
                    curLoc = msg.getData().getParcelable("loc");
                    gps_status.setText("N:"+curLoc.getLatitude() + " | E:" + curLoc.getLongitude());
                    gps_status_2.setText("ALT:"+(int)curLoc.getAltitude() + " | ACC:"+ (int)curLoc.getAccuracy() );

                    redrawAllShit();
                    break;
                case ReLocation.MSG_GPS_SATELLITE:
                    String sat = msg.getData().getString("sat");
                    gps_status_3.setText("SAT:" + sat);
                    break;
                case ReLocation.MSG_SET_STRING_VALUE:

                    //textStrValue.setText("Str Message: " + str1);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
