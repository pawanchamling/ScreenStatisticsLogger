package np.com.pawanchamling.screenstatisticslogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import np.com.pawanchamling.screenstatisticslogger.service.UpdateService;
import np.com.pawanchamling.screenstatisticslogger.service.recordScreenStatusService;

/**
 * Created by Pawan Chamling on 2016-09-17.
 */
// https://thinkandroid.wordpress.com/2010/01/24/handling-screen-off-and-screen-on-intents/
public class ScreenReceiver extends BroadcastReceiver {

    private boolean screenOff;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            //Log.d("ScreenReceiver", "Screen OFF");
            screenOff = true;
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            //Log.d("ScreenReceiver", "Screen ON");
            screenOff = false;
        }


        //Log.d("ScreenReceiver", "Sending an Intent");
        Intent intent1 = new Intent(context, recordScreenStatusService.class);
        intent1.putExtra("screen_state", screenOff);
        context.startService(intent1);
    }

}