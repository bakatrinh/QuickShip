package dev_t.cs161.quickship;


import android.bluetooth.BluetoothAdapter;
import android.os.Build;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.widget.Button;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Rule
    public ActivityTestRule<quickShipActivityMain> mainActivity = new ActivityTestRule(quickShipActivityMain.class);

    @Test
    public void checkBluetoothAdapterExists_test() {
        boolean btAdapterExists = mainActivity.getActivity().btAdapterExists();
        assertEquals(true,btAdapterExists);

        //Can't use espresso to select yes or no to bluetooth permission prompts :'(
        //Need to use UIAnimator
        //Toast messages crash instrumented tests.
        /*
        if(btAdapterExists){
            boolean isBTon = mainActivity.getActivity().isBTon();
            if(isBTon == false){
                onView(withText("OK")).perform(click());
                mainActivity.getActivity().enableBluetooth();
                //allowPermissionsIfNeeded();
                isBTon = mainActivity.getActivity().isBTon();
                assertEquals(true,isBTon);
            }
        }*/
    }

    //UIAnimator select yes on bluetooth prompt.
    private static void allowPermissionsIfNeeded() {
        //if (Build.VERSION.SDK_INT >= 23) {
            UiDevice device = UiDevice.getInstance(getInstrumentation());
            UiObject allowPermissions = device.findObject(new UiSelector().text("YES"));
            if (allowPermissions.exists()) {
                try {
                    allowPermissions.click();
                } catch (UiObjectNotFoundException e) {
                    //Timber.e(e, "There is no permissions dialog to interact with ");
                }
            }
        //}
    }
}
