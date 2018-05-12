package moon.phonetools;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.telecom.TelecomManager;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;

import static android.Manifest.permission.CALL_PHONE;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

/**
 * Converted from Kotlin
 * Source: https://github.com/arekolek/simple-phone
 */

public class DialerActivity extends AppCompatActivity {
    static final int REQUEST_PERMISSION = 0;

    private EditText phoneNumberInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialer);

        phoneNumberInput = findViewById(R.id.phoneNumberInput);

        if (getIntent().getData() != null) {
            phoneNumberInput.setText(getIntent().getData().getSchemeSpecificPart());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        offerReplacingDefaultDialer();

        phoneNumberInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                makeCall();
                return true;
            }
        });
    }

    private void makeCall() {
        if (PermissionChecker.checkSelfPermission(this, CALL_PHONE) == PERMISSION_GRANTED) {
            if (phoneNumberInput != null) {
                Uri uri = Uri.parse("tel:" + phoneNumberInput.getText().toString());
                startActivity(new Intent(Intent.ACTION_CALL, uri));
            }
        } else {
            requestPermissions(new String[]{CALL_PHONE}, REQUEST_PERMISSION);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            //If the permission was granted, make call
            for (int result : grantResults) {
                if (result == PERMISSION_GRANTED) makeCall();
            }
        }
    }

    private void offerReplacingDefaultDialer() {
        TelecomManager t = getSystemService(TelecomManager.class);
        if (t != null && (!t.getDefaultDialerPackage().equals(getPackageName()))) {
            Intent intent = (new Intent("android.telecom.action.CHANGE_DEFAULT_DIALER")).putExtra("android.telecom.extra.CHANGE_DEFAULT_DIALER_PACKAGE_NAME", getPackageName());
            startActivity(intent);
        }
    }
}
