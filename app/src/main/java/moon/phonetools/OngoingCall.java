package moon.phonetools;

import android.telecom.Call;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Converted from Kotlin
 * Source: https://github.com/arekolek/simple-phone
 */

public final class OngoingCall {
    private static BehaviorSubject<Integer> state = BehaviorSubject.create();
    private static android.telecom.Call.Callback callback = new Call.Callback() {
        public void onStateChanged(Call call, int state) {
            OngoingCall.INSTANCE.getState().onNext(state);
        }
    };
    private static Call call;
    public static OngoingCall INSTANCE = new OngoingCall();

    public final BehaviorSubject<Integer> getState() {
        return state;
    }

    public final Call getCall() {
        return call;
    }

    public final void setCall(Call value) {
        Call c = call;
        if (call != null) {
            c.unregisterCallback(callback);
        }

        if (value != null) {
            value.registerCallback(callback);
            state.onNext(value.getState());
        }

        call = value;
    }

    public final void answer() {
        Call c = call;
        if (call == null) {
            throw new NullPointerException("Call must not be null");
        }

        c.answer(0);
    }

    public final void hangup() {
        Call c = call;
        if (call == null) {
            throw new NullPointerException("Call must not be null");
        }

        c.disconnect();
    }
}
