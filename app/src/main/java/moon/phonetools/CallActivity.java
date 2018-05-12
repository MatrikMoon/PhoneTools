package moon.phonetools;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telecom.Call;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

/**
 * Converted from Kotlin
 * Source: https://github.com/arekolek/simple-phone
 * Sensor event listener: https://stackoverflow.com/questions/17967806/using-proximity-sensor-in-android
 */

public class CallActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mProximity;
    private static final int SENSOR_SENSITIVITY = 4;

    private CompositeDisposable disposables = new CompositeDisposable();
    private String number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        if (getIntent().getData() != null) {
            number = getIntent().getData().getSchemeSpecificPart();
        }

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] >= -SENSOR_SENSITIVITY && event.values[0] <= SENSOR_SENSITIVITY) {
                findViewById(R.id.root).setVisibility(View.GONE);
                params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                params.screenBrightness = 0;
                getWindow().setAttributes(params);
            } else {
                findViewById(R.id.root).setVisibility(View.VISIBLE);
                params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                params.screenBrightness = -1f;
                getWindow().setAttributes(params);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onStart() {
        super.onStart();

        findViewById(R.id.answer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OngoingCall.INSTANCE.answer();
            }
        });

        findViewById(R.id.hangup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OngoingCall.INSTANCE.hangup();
            }
        });

        findViewById(R.id.one).setOnTouchListener(new NumberTouch('1'));
        findViewById(R.id.two).setOnTouchListener(new NumberTouch('2'));
        findViewById(R.id.three).setOnTouchListener(new NumberTouch('3'));
        findViewById(R.id.four).setOnTouchListener(new NumberTouch('4'));
        findViewById(R.id.five).setOnTouchListener(new NumberTouch('5'));
        findViewById(R.id.six).setOnTouchListener(new NumberTouch('6'));
        findViewById(R.id.seven).setOnTouchListener(new NumberTouch('7'));
        findViewById(R.id.eight).setOnTouchListener(new NumberTouch('8'));
        findViewById(R.id.nine).setOnTouchListener(new NumberTouch('9'));
        findViewById(R.id.zero).setOnTouchListener(new NumberTouch('0'));
        findViewById(R.id.pound).setOnTouchListener(new NumberTouch('#'));
        findViewById(R.id.star).setOnTouchListener(new NumberTouch('*'));

        disposables.addAll(
                OngoingCall.INSTANCE.getState().subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        updateUi(integer);
                    }
                }),
                OngoingCall.INSTANCE.getState().filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer integer) {
                        return integer == Call.STATE_DISCONNECTED;
                    }
                }).delay(1, TimeUnit.SECONDS).firstElement().subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        finish();
                    }
                })
        );
    }

    private void updateUi(int state) {
        String infoText = stateToString(state) + "\n" + number;
        ((TextView)findViewById(R.id.callInfo)).setText(infoText);

        findViewById(R.id.answer).setVisibility((state == Call.STATE_RINGING) ? View.VISIBLE : View.GONE);

        findViewById(R.id.hangup).setVisibility((state == Call.STATE_DIALING ||
                state == Call.STATE_RINGING ||
                state == Call.STATE_ACTIVE) ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        disposables.clear();
    }

    String stateToString(int i) {
        if (i == Call.STATE_NEW) return "NEW";
        if (i == Call.STATE_RINGING) return "RINGING";
        if (i == Call.STATE_DIALING) return "DIALING";
        if (i == Call.STATE_ACTIVE) return "ACTIVE";
        if (i == Call.STATE_HOLDING) return "HOLDING";
        if (i == Call.STATE_DISCONNECTED) return "DISCONNECTED";
        if (i == Call.STATE_CONNECTING) return "CONNECTING";
        if (i == Call.STATE_DISCONNECTING) return "DISCONNECTING";
        if (i == Call.STATE_SELECT_PHONE_ACCOUNT) return "SELECT_PHONE_ACCOUNT";
        return "UNKNOWN";
    }

    //Helper class for number touch listener
    private class NumberTouch implements View.OnTouchListener {
        char num;
        NumberTouch(char num) {
            this.num = num;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            v.performClick(); //To satisfy android studio warning

            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    OngoingCall.INSTANCE.getCall().playDtmfTone(num);
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    OngoingCall.INSTANCE.getCall().stopDtmfTone();
                    break;
                }
            }
            return false;
        }
    }
}
