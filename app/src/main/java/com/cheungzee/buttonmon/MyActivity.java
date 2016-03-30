package com.cheungzee.buttonmon;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

public class MyActivity extends AppCompatActivity implements SensorEventListener {
    public final static String EXTRA_MESSAGE = "com.cheungzee.buttonmon.MESSAGE";
    private static final int msgKey1 = 1;
    private TextView mTime;
    private TextView mTime1;
    private TextView mTime2;
    public Integer count = 0;
    public Integer count1 = 0;
    public Integer count2 = 0;
    public double x, y, z = 0;
    public double x1, y1, z1 = 0;
    public double vx, vy, vz = 0;
    public double vx1, vy1, vz1 = 0;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    public double lastTimestamp = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        mTime = (TextView) findViewById(R.id.textView);
        mTime1 = (TextView) findViewById(R.id.textView4);
        mTime2 = (TextView) findViewById(R.id.textView6);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        new TimeThread().start();
    }
    public void onSensorChanged(SensorEvent event){
        // In this example, alpha is calculated as t / (t + dT),
        // where t is the low-pass filter's time-constant and
        // dT is the event delivery rate.

        //Log.v("INFO", "In onSensorChanged");

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float[] eventValues = new float[3];
            eventValues = event.values.clone ();
            if(lastTimestamp == 0) {
                lastTimestamp = event.timestamp;
                return;
            }
            double dt = (event.timestamp  - lastTimestamp) / 1000000000.0f;
            lastTimestamp = event.timestamp;


            double theSum = eventValues[0] * eventValues[0] + eventValues[1] * eventValues[1]
                    + eventValues[2] * eventValues[2];
            double value =  Math.sqrt(theSum);

            String tableText =  "X:" + String.format("%+3.5f", eventValues[0]) + ";\nY:"
                        + String.format("%+3.5f", eventValues[1]) +
                    ";\nZ:" + String.format("%+3.5f", eventValues[2]) + "\nSIZE:" + value;

            //calc
            double filter = 1.0f; // high pass;
            double vDistance, vDistance1  = 0.0;
            double vSpeed, vSpeed1 = 0.0;

            if(Math.abs(value) >= filter ) {
                x1 = x1 + vx1*dt + 1/2 * dt * eventValues[0] * dt;
                y1 = y1 + vy1*dt + 1/2 * dt * eventValues[1] * dt;
                z1 = z1 + vz1*dt + 1/2 * dt * eventValues[2] * dt;
                vx1 = vx1 + dt * eventValues[0];
                vy1 = vy1 + dt * eventValues[1];
                vz1 = vz1 + dt * eventValues[2];
                vDistance1 = Math.sqrt(x1 * x1 + y1 * y1 + z1 * z1);
                vSpeed1 = Math.sqrt(vx1 * vx1 + vy1 * vy1 + vz1 * vz1);
                //set Text;

                String tableText1 =  "vx1:" + String.format("%+3.5f", vx1) +
                        ";\nvy1:" + String.format("%+3.5f", vy1) +
                        ";\nvz1:" + String.format("%+3.5f", vz1) +
                        "\n去噪后速度:" + vSpeed1 +
                        "\nX1:" + String.format("%+3.5f", x1) +
                        ";\nY1:" + String.format("%+3.5f", y1) +
                        ";\nZ1:" + String.format("%+3.5f", z1) +
                        "\n去噪后位移:" + vDistance1;
                mTime2.setText(tableText1);
            }
            x = x + vx*dt + 1/2 * dt * eventValues[0] * dt;
            y = y + vy*dt + 1/2 * dt * eventValues[1] * dt;
            z = z + vz*dt + 1/2 * dt * eventValues[2] * dt;

            vx = vx + dt * eventValues[0];
            vy = vy + dt * eventValues[1];
            vz = vz + dt * eventValues[2];
            vDistance = Math.sqrt(x * x + y * y + z * z);
            vSpeed = Math.sqrt(vx * vx + vy * vy + vz * vz);
            //set Text;
            String tableText2 =  "vx:" + String.format("%+3.5f", vx)
                    + ";\nvy:" + String.format("%+3.5f", vy) +
                    ";\nvz:" + String.format("%+3.5f", vz) +
                    "\n带噪速度:" + vSpeed +
                    "\nX:" + x + "\nY:" + y +
                    "\nZ:" + z + "\n带噪位移:" + vDistance;

            mTime1.setText(tableText2);
            mTime.setText(tableText);
        }
    }

    public class TimeThread extends Thread {

        @Override
        public void run () {
            do {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = msgKey1;
                    mHandler.sendMessage(msg);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while(true);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage (Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case msgKey1:
                    long sysTime = System.currentTimeMillis();
                    CharSequence sysTimeStr = DateFormat.format("hh:mm:ss", sysTime);

                    count += 1;
                    count1 += 1;
                    count2 += 1;
                    //mTime.setText(count.toString());
                    //mTime1.setText(count1.toString());
                    //mTime2.setText(count2.toString());
                    //Log.v("INFO", "in count  ~");

                    break;

                default:
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void sendMessage(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void reset(View view) {
        count = 0;
        count1 = 10000;
        count2 = 100000000;
        x=0f; y=0f; z=0f;
        x1=0f; y1=0f; z1=0f;
        vx=0;vy=0;vz=0;
        vx1=0;vy1=0;vz1=0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register this class as a listener for the orientation and
        // accelerometer sensors

        //使用 SENSOR_DELAY_GAME 目测比较准确
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}



