package my.subject.myHelloIOIO;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Prefs extends PreferenceActivity {
	private static final String OPT_SMSGW_ENABLED = "gw";
	private static final boolean OPT_SMSGW_ENABLED_DEF = false;
	private static final String OPT_SPEED = "speed_list";
	private static final String OPT_SPEED_DEF = "fast";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}
	
	public static boolean getSMSGWMode(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_SMSGW_ENABLED, OPT_SMSGW_ENABLED_DEF);
	}
	
	public static String getSendSpeed(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(OPT_SPEED, OPT_SPEED_DEF);
	}

}
