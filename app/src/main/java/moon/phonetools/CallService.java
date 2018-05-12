package moon.phonetools;

import android.content.Intent;
import android.telecom.Call;
import android.telecom.InCallService;

/**
 * Converted from Kotlin
 * Source: https://github.com/arekolek/simple-phone
 */

public class CallService extends InCallService {
    @Override
    public void onCallAdded(Call call) {
        OngoingCall.INSTANCE.setCall(call);

        //If the call is coming from **** ****, handle it
        String number = call.getDetails().getHandle().getSchemeSpecificPart();
        if (number.contains("5555555555")) { //Note to anyone who reads this: This is an automated number and will hang up immediately if you call it.
            //Answer the call automatically
            OngoingCall.INSTANCE.answer();

            //Let the system know we really DO wanna send a tone;
            //it's not an accident
            OngoingCall.INSTANCE.getCall().postDialContinue(true);

            //Wait three seconds so we're not blasting the caller the moment we connect (or earlier...)
            try {
                Thread.sleep(3000);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            //Play the tone for half a second
            OngoingCall.INSTANCE.getCall().playDtmfTone('9');
            try {
                Thread.sleep(500);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            OngoingCall.INSTANCE.getCall().stopDtmfTone();

            //End the call
            OngoingCall.INSTANCE.hangup();
        }
        //If it's not the gate, proceed with dialer app
        else startActivity(new Intent(getBaseContext(), CallActivity.class)
                .setData(call.getDetails().getHandle())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public void onCallRemoved(Call call) {
        OngoingCall.INSTANCE.setCall(null);
    }
}