package my.subject.myHelloIOIO;

import java.io.IOException;
//import ioio.lib.api.IOIO;
//import ioio.lib.api.Uart;
//import ioio.lib.api.Uart.Parity;
//import ioio.lib.api.Uart.StopBits;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.exception.ConnectionLostException;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;
//import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.util.Log;
import java.io.OutputStream;

import android.app.Activity;
import android.app.ProgressDialog;

//public class AsyncEvents extends AsyncTask<Integer, Integer, Void> {
public class AsyncEvents extends AsyncTask<String, Integer, Void> implements OnCancelListener {
	private static final boolean SERDEBUG = false;
	private static final String TAG = "AsyncEvents";
	private static final int OPT_SPD_MEDIUM_VAL = 2;
	
	private DigitalOutput led;
	private OutputStream out;
	private OutputStream scon;
	private ToggleButton tBtn;
	private Button btn;
	private int sendSpeed;
	private boolean isInCalibrationMode;
	private MIDIHellschreiber hell;
	private static boolean isIdle;

//	Context context;
	Activity act = null;
	ProgressDialog dialog = null;
	
	public AsyncEvents(Activity activity, OutputStream output, int spd, boolean flag) {
		act = activity;
		out = output;
		setSendSpeed(spd);
		setInCalibrationMode(flag);
		hell = new MIDIHellschreiber();
		hell.gsReset();
		setIdle(true);
		tBtn = (ToggleButton)act.findViewById(R.id.toggleButton1);
		btn = (Button)act.findViewById(R.id.button1);
	}

	public AsyncEvents(Activity activity, OutputStream output) {
		act = activity;
		out = output;
		setSendSpeed(OPT_SPD_MEDIUM_VAL);
		setInCalibrationMode(false);
		hell = new MIDIHellschreiber();
		hell.gsReset();
		setIdle(true);
		tBtn = (ToggleButton)act.findViewById(R.id.toggleButton1);
		btn = (Button)act.findViewById(R.id.button1);
		led = null;
	}

	public AsyncEvents(Activity activity, OutputStream output, OutputStream console, DigitalOutput dout) {
		act = activity;
		out = output;
		scon = console;
		setSendSpeed(OPT_SPD_MEDIUM_VAL);
		setInCalibrationMode(false);
		hell = new MIDIHellschreiber();
		hell.gsReset();
		setIdle(true);
		tBtn = (ToggleButton)act.findViewById(R.id.toggleButton1);
		btn = (Button)act.findViewById(R.id.button1);
		led = dout;
	}

	public void setInCalibrationMode(boolean status) {
		this.isInCalibrationMode = status;
	}

	public boolean isInCalibrationMode() {
		return isInCalibrationMode;
	}

	
	public static void setIdle(boolean isIdle) {
		AsyncEvents.isIdle = isIdle;
	}

	public static boolean isIdle() {
		return isIdle;
	}

	public void setSendSpeed(int sendSpeed) {
		this.sendSpeed = sendSpeed;
	}

	public int getSendSpeed() {
		return sendSpeed;
	}
	
	private void serCon(String str) {
		if ( (scon != null) && (str != null) && (str.length() > 0) )
			try {
				scon.write(str.getBytes("UTF-8"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d(TAG, "IOException in serCon()");
			}
	}

	@Override
	protected void onPreExecute() {
		setIdle(false);
		if ( !isInCalibrationMode() ) {
			btn.setEnabled(false);
			dialog = new ProgressDialog(act);
			dialog.setTitle(R.string.progress);
			dialog.setIndeterminate(false);
			dialog.setMax(100);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setCancelable(true);
			dialog.setOnCancelListener(this);
			try {
				dialog.show();
			} catch (Exception e) {
			}
		} else {
//			if ( tBtn.isChecked() ) {
//				Toast.makeText(act, "Start generating test tone...", Toast.LENGTH_SHORT).show();
//			} else {
//				Toast.makeText(act, "Stop generating test tone...", Toast.LENGTH_SHORT).show();
//			}
			try {
				doCalibrationTask();
			} catch (ConnectionLostException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		} // end of if
	} // end of onPreExecute()
	
	@Override
//	protected Void doInBackground(Integer... arg0) {
	protected Void doInBackground(String... arg0) {
		int len = arg0[0].length();
		Log.d(TAG, "doInBackground( " + arg0[0] + " )" );
		if ( !isInCalibrationMode() ) {
//		if (isInCalibrationMode)
//			try {
//				doCalibrationTask();
//			} catch (ConnectionLostException e) {
//				// TODO Auto-generated catch block
//				//e.printStackTrace();
//			}
//		else {
			Log.d(TAG, "in doInBackground(): send button is pressed.");
			if (SERDEBUG)
				serCon(TAG + " in doInBackground(): send button is pressed.");
			if (len > 0) {
				for (int i = 0; i < len; i++) {
					hell.doHellSchreiber(arg0[0].charAt(i));
					int pg = (100 * (i + 1)) / len;
					String str = Integer.toString(pg);
					Log.d(TAG, "progress = " + str + "%");
					publishProgress( pg );
				}
			}
		} // end of if
		return null;
	}  // end of doInBackground()
	
	private void doCalibrationTask() throws ConnectionLostException {
		hell.gsReset();
		if ( tBtn.isChecked() ) {
			if (led != null)
				led.write(false);	// turns on the on-board LED
			Log.d(TAG, "in doCalibrationTask(): toggleButton is checked.");
			Toast.makeText(act, "Start generating test tone...", Toast.LENGTH_SHORT).show();
			if (SERDEBUG)
				serCon(TAG + " in doCalibrationTask(): ToggleButton is checked.");
			hell.sendMIDImsg(0x90, 90, 127);	// note on
		} else {
			if (led != null)
				led.write(true);	// turns off the on-board LED
			Log.d(TAG, "toggleButton is NOT checked, in doCalibrationTask()");
			Toast.makeText(act, "Stop generating test tone...", Toast.LENGTH_SHORT).show();
			if (SERDEBUG)
				serCon(TAG + " in doCalibrationTask(): ToggleButton is NOT checked.");
			hell.sendMIDImsg(0x80, 90, 127);	// note off
		}
//		hell.delay(getSendSpeed() * 2000);
	}

	@Override
	protected void onProgressUpdate(Integer... arg1) {
		dialog.setProgress(arg1[0]);
	}
	
	@Override
	protected void onPostExecute(Void arg2) {
		try {
			dialog.dismiss();
		} catch (Exception e) {
			
		}
		setIdle(true);
		Log.d(TAG,"Return to Idle.");
		btn.setEnabled(true);
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		Log.d(TAG, "Dialog onCalcel");
		this.cancel(true);
	}
	
	@Override
	protected void onCancelled() {
		try {
			dialog.dismiss();
		} catch (Exception e) {
			
		}
		setIdle(true);
		Log.d(TAG,"Return to Idle.");
		btn.setEnabled(true);
		Log.d(TAG, "OnCancelled");
	}

	class MIDIHellschreiber {
		private static final String TAG = "MIDIHELL";
		
		protected void doHellSchreiber(char c) {
			// TODO Auto-generated method stub
			// debug code
			Log.d(TAG, "sending char:" + c);
			delay(500);
			if ( led != null ) {
				delay(100);
				try {
					led.write(true);
				} catch (ConnectionLostException e) {
					// TODO Auto-generated catch block
				}
				delay(100);
				try {
					led.write(false);
				} catch (ConnectionLostException e) {
					// TODO Auto-generated catch block
				}
			} // end of if
			// end of debug code
		}

		protected void sendMIDImsg(int stat, int data1, int data2) {
			serOut(stat);
			serOut(data1);
			serOut(data2);
		}
		
		protected void sendMIDImsg2(int stat, int data) {
			serOut(stat);
			serOut(data);
		}
		
		protected void vsendMIDImsg(int... args) {
			for (int i : args)
				serOut(i);
		}
		
		protected void changeMasterTune(int displacement) {
			
		}
		
		protected void gsReset() {	// for Roland SC-55/55mk2/(88)
			serOut(0xF0);	// exclusive status
			serOut(0x41);	// ID (=Roland)
			serOut(0x10);	// device ID
			serOut(0x42);	// model ID
			serOut(0x12);	// command ID (=DT1)
			serOut(0x40);	// address MSB
			serOut(0x00);	// address
			serOut(0x7F);	// address LSB
			serOut(0x00);	// GS reset
			serOut(0x41);
			serOut(0xF7);
			delay(100);
		}
		
		private void serOut(int i) {
			if ( out != null ) {
				try {
					out.write(i);
				} catch (IOException e) {
					Log.d(TAG, "IOException in serOut()");
				} catch (Exception e) {
					Log.d(TAG, "Something weird occcurred in serOut()");
				}
			}
		}
		
		private void delay(int wait) {
			try {
				Thread.sleep(wait);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				Log.d(TAG, "InterruptedException in delay()");
			}			
		}
	} // end of inner-class MIDIHellschreiber definition

}
