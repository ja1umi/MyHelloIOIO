package my.subject.myHelloIOIO;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
//import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
//import android.util.Log;
//import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {
	private static boolean smsReceived;
	private static String smsMsg;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		String str = "";
		Object messages[] = (Object[]) bundle.get("pdus");
		SmsMessage smsMessage[] = new SmsMessage[messages.length];
		for (int i = 0; i < messages.length; i++ ) {
			smsMessage[i] = SmsMessage.createFromPdu((byte[]) messages[i]);
			str += smsMessage[i].getMessageBody().toString();
			
			SimpleDateFormat  sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date  date = new Date();
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(smsMessage[i].getOriginatingAddress().toString(), null, "#Received " + sdf.format(date) + "#", null, null);
		}
////		Log.d("SMS", str);
		//Toast.makeText(context, "SMS: " + str, Toast.LENGTH_SHORT).show();
//		SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);	// Importing Context won't give me all it's constants (e.g. MODE_PRIVATE) and methods!
//		SharedPreferences.Editor e = pref.edit();
//		e.putString("SMSMsg", pref.getString("SMSMsg", "") + str);
////		e.putBoolean("IsSMSReceived", true);
		SMSReceiver.smsMsg += str;
		setSMSReceived(true); 
	}

	public static void setSMSReceived(boolean smsReceived) {
		SMSReceiver.smsReceived = smsReceived;
	}

	public static boolean isSMSReceived() {
		return smsReceived;
	}

	public static void setSMSMsg(String smsMsg) {
		SMSReceiver.smsMsg = smsMsg;
	}

	public static String getSMSMsg() {
		return smsMsg;
	}
}
