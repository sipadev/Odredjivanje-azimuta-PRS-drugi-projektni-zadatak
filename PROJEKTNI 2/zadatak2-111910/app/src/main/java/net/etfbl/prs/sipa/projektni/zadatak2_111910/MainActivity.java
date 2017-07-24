package net.etfbl.prs.sipa.projektni.zadatak2_111910;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;


/****************************************************************************
 *
 * Copyright (c) 2016 Elektrotehnički fakultet
 * Patre 5, Banja Luka
 *
 * All Rights Reserved
 *
 * \file <MainActivit.java>
 * \brief
 * <Klasa izracunava vrednost azimuta, dok je pozadina prikaz onoga što snima kamera>
 *
 * Created on <DATE(27.04.2017)>
 *
 * @Author <Milorad Šipovac>
 *
\notes
 * <DATE(28.04.2017)> <Nije radilo na verziji 6, pa sam morao dodavati neke permisije>
 *
\history
 *
 **********************************************************************/

public class MainActivity extends Activity implements SensorEventListener, SurfaceHolder.Callback {

    //Za azimut
    float[] mGravity;
    float[] mGeomagnetic;
    int azimut;
    //Ispis azimuta
    TextView textView;
    //Senzori
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Sensor magnet;
    //Za prikaz kamere
    private SurfaceView SurView;
    private SurfaceHolder camHolder;
    private long lastUpdate = 0;
    public static Camera camera = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnet = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        //Ako nema kamere ili ne radi,ispisi poruku
        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            SurView = (SurfaceView) findViewById(R.id.surfaceView);
            camHolder = SurView.getHolder();
            camHolder.addCallback(this);
            camHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        } else {
            Toast.makeText(MainActivity.this, R.string.greskaKamere, Toast.LENGTH_SHORT).show();
        }
        //Ako nema neki od senzora,treba ispisati poruku
        if (magnet == null || mSensor == null) {
            Toast.makeText(MainActivity.this, R.string.senzor, Toast.LENGTH_SHORT).show();
            textView.setText(R.string.senzor);
        }

    }

    //zbog ustede energije
    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnet, SensorManager.SENSOR_DELAY_UI);
    }
    //zbog ustede energije
    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mSensor);
        mSensorManager.unregisterListener(this, magnet);

    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values;

        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values;

        }


        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 400) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;
                if (SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)) {

                    float[] outR = new float[9];
                    float orientation[] = new float[3];

                    SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X,SensorManager.AXIS_Z, outR);

                    azimut = (int) (Math.toDegrees(SensorManager.getOrientation(outR, orientation)[0]) + 360) % 360;
                    textView.setText("" + azimut);

                }

            }
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //Zbog permisija
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 50);
        else

            try {
                //Kada se kreira surface, otvaramo kameru
                camera = Camera.open();
                camera.setDisplayOrientation(90);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), R.string.greskaUTokuRadaKamere, Toast.LENGTH_SHORT).show();
            }
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        try {
            Camera.Parameters camParams = camera.getParameters();
            Camera.Size size = camParams.getSupportedPreviewSizes().get(0);
            camParams.setPreviewSize(size.width, size.height);
            camera.setParameters(camParams);

            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.greskaUTokuRadaKamere, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }
}
