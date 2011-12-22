package my.subject.myHelloIOIO;

import java.io.IOException;
//import ioio.lib.api.IOIO;
//import ioio.lib.api.Uart;
//import ioio.lib.api.Uart.Parity;
//import ioio.lib.api.Uart.StopBits;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.exception.ConnectionLostException;
import android.os.AsyncTask;
//import android.widget.Button;
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
//	private static final int OPT_DEFAULT_ENTRY = 1;
	private static final int TUNE4CALIB = 1024;		// default value of master tune (+/- 0 cent)
	public final int FONT5x5 = 0;
	public final int FONT7x5 = 1;
	
	private DigitalOutput led, rts;
	private OutputStream out;
	private OutputStream scon;
	private ToggleButton tBtn;
//	private Button btn;
	private int fontNumber, sendSpeed, disp, tune;
	private boolean isInCalibrationMode, isDoubleWidth, isFullBreakIn;
	private GSMIDIHellschreiber sc55;
	private static boolean isIdle;

//	Context context;
	private Activity act = null;
	private ProgressDialog dialog = null;
	
	public AsyncEvents(MyHelloIOIOActivity activity, OutputStream output, OutputStream console, DigitalOutput dout1, DigitalOutput dout2, int pos) {
		act = activity;
		out = output;
		scon = console;
		sendSpeed = activity.getResources().getIntArray(R.array.speed_values)[pos];
		disp = activity.getResources().getIntArray(R.array.displacement_values)[pos];
		tune = activity.getResources().getIntArray(R.array.tune_values)[pos];
		if ( activity.isSmallFontEnabled() )
			fontNumber = FONT5x5;
		else
			fontNumber = FONT7x5;
		
		isDoubleWidth = activity.isDoubleWidthModeEnabled();
		isFullBreakIn = activity.isFullBrealInModeEnabled();
		isInCalibrationMode = false;
		sc55 = new GSMIDIHellschreiber();
		sc55.initSoundModule();
		setIdle(true);
		tBtn = (ToggleButton)act.findViewById(R.id.toggleButton1);
//		btn = (Button)act.findViewById(R.id.button1);
		led = dout1;
		rts = dout2;
	}

	public void enableCalibrationMode(boolean stat) {
		this.isInCalibrationMode = stat;
	}
//
//	public boolean isInCalibrationMode() {
//		return isInCalibrationMode;
//	}
//
//
	public static void setIdle(boolean isIdle) {
		AsyncEvents.isIdle = isIdle;
	}

	public static boolean isIdle() {
		return isIdle;
	}

//	public void storeActivity(Activity actv) {
//		this.act = actv;
//	}
	
//	public void setSendSpeed(int sendSpeed) {
//		this.sendSpeed = sendSpeed;
//	}
//
//	public int getSendSpeed() {
//		return this.sendSpeed;
//	}
//	
//	public void setDisplacement(int dp) {
//		this.disp = dp;
//	}
//	
//	public int getDisplacement() {
//		return disp;
//	}
//	
//	public void setTune(int val) {
//		this.tune = val;
//	}
//	
//	public int getTune() {
//		return tune;
//	}
//	
	private void serCon(String str) {
		if ( (scon != null) && (str != null) && (str.length() > 0) )
			try {
//				scon.write(str.getBytes("UTF-8"));
				scon.write(str.getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d(TAG, "IOException in serCon()");
			}
	}
	
	private void GPIOWrite(DigitalOutput dout, boolean stat) {
		if ( dout != null ) 
			try {
				dout.write(stat);
			} catch (ConnectionLostException e) {
			}
	}

	@Override
	protected void onPreExecute() {
		setIdle(false);
		if ( !isInCalibrationMode ) {
//			btn.setEnabled(false);
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
		if ( !isInCalibrationMode ) {
			Log.d(TAG, "in doInBackground(): send button is pressed.");
			Log.d(TAG, "Send speed = " + Integer.toString(sendSpeed));
			Log.d(TAG, "Font number = " + Integer.toString(fontNumber));
			Log.d(TAG, "Initial tune = " + Integer.toString(tune));
			Log.d(TAG, "Tone displacement = " + Integer.toString(disp));
			if (isDoubleWidth)
				Log.d(TAG, "Double-Width mode was enabled.");
			else
				Log.d(TAG, "Double-Width mode was disabled.");
			if (isFullBreakIn)
				Log.d(TAG, "Full-break-in mode was enabled.");
			else
				Log.d(TAG, "Full-break-in mode was disabled.");
			if (SERDEBUG)
				serCon(TAG + " in doInBackground(): send button is pressed.");
			GPIOWrite(rts, false); GPIOWrite(led, false); sc55.delay(100);		// assert RTS (turn transmitter on)
			if (len > 0) {
				sc55.setUserStr(arg0[0]);
				for (int i = 0; i < len; i++) {
//					hell.doHellSchreiber(arg0[0].charAt(i));
					sc55.sendCharacter(fontNumber, arg0[0].charAt(i), tune, disp, sendSpeed, isDoubleWidth);
					int pg = (100 * (i + 1)) / len;
					String str = Integer.toString(pg);
					Log.d(TAG, "progress = " + str + "%");
					publishProgress( pg );
				}
			}
			sc55.delay(100); GPIOWrite(rts, true); GPIOWrite(led, true);		// negate RTS (turn transmitter off) 
		} // end of if
		return null;
	}  // end of doInBackground()
	
	private void doCalibrationTask() throws ConnectionLostException {
//		sc55.initSoundModule();
		sc55.changeMasterTune(TUNE4CALIB);
		if ( tBtn.isChecked() ) {
			GPIOWrite(led, false);	// turns on the on-board LED
//			if (led != null)
//				led.write(false);	// turns on the on-board LED
			Log.d(TAG, "in doCalibrationTask(): toggleButton is checked.");
			Toast.makeText(act, "Start generating test tone...", Toast.LENGTH_SHORT).show();
			if (SERDEBUG)
				serCon(TAG + " in doCalibrationTask(): ToggleButton is checked.");
			sc55.sendMIDImsg(0x90, 90, 127);	// note on
		} else {
			GPIOWrite(led, true);	// turns on the on-board LED
//			if (led != null)
//				led.write(true);	// turns off the on-board LED
			Log.d(TAG, "toggleButton is NOT checked, in doCalibrationTask()");
			Toast.makeText(act, "Stop generating test tone...", Toast.LENGTH_SHORT).show();
			if (SERDEBUG)
				serCon(TAG + " in doCalibrationTask(): ToggleButton is NOT checked.");
			sc55.sendMIDImsg(0x80, 90, 127);	// note off
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
//		btn.setEnabled(true);
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
//		btn.setEnabled(true);
		Log.d(TAG, "OnCancelled");
	}

	class GSMIDIHellschreiber extends MIDIHellschreiber {
		private static final String TAG = "GSMIDIHELL";
		
//		protected void doHellSchreiber(char c) {
//			// TODO Auto-generated method stub
//			// debug code
//			Log.d(TAG, "sending char:" + c);
//			delay(500);
//			if ( led != null ) {
//				delay(100);
//				try {
//					led.write(true);
//				} catch (ConnectionLostException e) {
//					// TODO Auto-generated catch block
//				}
//				delay(100);
//				try {
//					led.write(false);
//				} catch (ConnectionLostException e) {
//					// TODO Auto-generated catch block
//				}
//			} // end of if
//			// end of debug code
//		}

		@Override
		protected void changeMasterTune(int tune) {
			int csum, b1 = 0x40, b2 = 0, b3 = 0, d1, d2, d3, d4;
			
			d1 = (tune >> 12) & 0x0F;
			d2 = (tune >> 8) & 0x0F;
			d3 = (tune >> 4) & 0x0F;
			d4 = tune & 0x0F;

			vsendMIDImsg(0xF0, 0x41, 0x10, 0x42, 0x12, b1, b2, b3, d1, d2, d3, d4);
			csum = 128 - ((b1 + b2 + b3 + d1 + d2 + d3 + d4) & 0x7F);
			sendMIDImsg2(csum, 0xF7);
			delay(50);					
		}
		
		@Override
		protected void initSoundModule() {	// for Roland SC-55/55mk2/(88)
			vsendMIDImsg(0xF0, 0x41, 0x10, 0x42, 0x12, 0x40, 0, 0x7F, 0 ,0x41, 0xF7);	// GS reset
			delay(100);
			sendMIDImsg(0xB0, 0, 8);		// "Sine-wave" @ SC-55mk2
			sendMIDImsg(0xB0, 0x20, 0);
			sendMIDImsg2(0xC0, 80);
//			sendMIDImsg(0xB0, 0, 0);		// "flute" @ SC-55mk2
//			sendMIDImsg(0xB0, 0x20, 0);
//			sendMIDImsg2(0xC0, 73);
			delay(100);
		}
		
		@Override
		protected void MIDIOut(int i) {
			Log.d("SC55", "--> 0x" + Integer.toHexString(i));
			if ( out != null ) {
				try {
					out.write(i);
				} catch (IOException e) {
					Log.d(TAG, "IOException in MIDIOut()");
				} catch (Exception e) {
					Log.d(TAG, "Something weird occcurred in MIDIOut()");
				}
			}
		}
		
		@Override
		public void delay(long wait) {
			try {
				Thread.sleep(wait);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				Log.d(TAG, "InterruptedException in delay()");
			}			
		}
		
		@Override
		public void doOptionalTaskPerEveryColumn(String msg, int fontSel, char ch, int curPos) {
			displayMessage(msg);
			displayDots(fontSel, ch, curPos);
		}
		
		protected void displayMessage(String msg) {
			int csum, b, dat, b1 = 0x10, b2 = 0, b3 = 0;
			String str;
			
			str = msg.concat("                                ").substring(0, 32);
			vsendMIDImsg(0xF0, 0x41, 0x10, 0x45, 0x12, b1, b2, b3);
			dat = 0;
			for (int i = 0; i <32; i++) {
				b = str.charAt(i);
				MIDIOut(b);
			    dat += b;  
			}
			csum = 128 - ((b1 + b2 + b3 + dat) & 0x7F);
			sendMIDImsg2(csum, 0xF7);
			delay(50);
		}
		
		protected void displayDots(int fontSel, char ch, int curPos) {
			int csum, dat, b = 0, b1 = 0x10, b2 = 0, b3 = 0;
			
			vsendMIDImsg(0xF0, 0x41, 0x10, 0x45, 0x12, b1, b2, b3);
			dat = 0;
			for (int i = 1; i <=64; i++) {
				if ( i < 8 )
					b =  getRowCharPattern(fontSel, ch)[i-1];
				if ( ( i == 8) || ( i > 9) )
					b = 0;
				if ( i == 9 )
					b = 0x20 >> (curPos+1);
				MIDIOut(b);
				dat += b;  
			}
			  
			csum = 128 - ((b1 + b2 + b3 + dat) & 0x7F);
			sendMIDImsg2(csum, 0xF7);
			delay(50);		
		}
		
		protected void doOptionalTaskPerEveryPixel(String str, int fontSel, int row, int pixelStat) {
			if (isFullBreakIn)
				if (pixelStat == 0) {
					GPIOWrite(rts, true);		// turn transmitter off while "white" pixels appear
					GPIOWrite(led, true);
					Log.d(TAG, "White pixel, turn transmitter off.");
				} else {
					GPIOWrite(rts, false);		// turn transmitter on only when needed				
					GPIOWrite(led, false);
					Log.d(TAG, "Black pixel, turn transmitter on.");
				}
		}

	} // end of inner-class GSMIDIHellschreiber definition

}
