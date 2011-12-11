package my.subject.myHelloIOIO;

import my.subject.myHelloIOIO.R;
//import ioio.lib.api.IOIO;
import ioio.lib.api.DigitalOutput;
//import ioio.lib.api.IOIO;
import ioio.lib.api.IOIO;
import ioio.lib.api.Uart;
//import ioio.lib.api.Uart.Parity;
//import ioio.lib.api.Uart.StopBits;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.AbstractIOIOActivity;
import android.app.AlertDialog;
import android.os.Bundle;
//import android.widget.CompoundButton;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;
//import android.content.Intent;
//import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
import android.util.Log;
//import java.io.IOException;
import java.io.IOException;
import java.io.OutputStream;
//import java.io.UnsupportedEncodingException;
//import java.util.Collections;
//import java.util.List;
//import java.util.ArrayList;

public class MyHelloIOIOActivity extends AbstractIOIOActivity implements OnClickListener, OnItemSelectedListener {
	private static final String TAG = "MyHelloIOIO" ;
//	private static final int OPT_SPD_FAST_VAL = 1;
//	private static final int OPT_SPD_MEDIUM_VAL = 2;
//	private static final int OPT_SPD_SLOW_VAL = 3;
	private static final int OPT_DEFAULT_ENTRY = 1;
	
	private boolean ioioInitialized;
	private String hellMessage;
//	private String sendSpeedTag;
	private boolean smsGWMode;
	private int itemAtPos = OPT_DEFAULT_ENTRY;
	private ToggleButton toggleButton;
	private Button sendButton, exitButton, aboutButton;
	private AsyncEvents async;
	private OutputStream out, scon;
	private DigitalOutput led;
	private Uart uart1, uart2;
//	private List<String> msgList = Collections.synchronizedList( new ArrayList<String>() );
	private EditText editText;
	private Spinner spinner;
	private CheckBox checkBox;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setIoioInitialized(true);		// set it to 'true' during development _without_ IOIO board
        enableSmsGWMode(false);
        setContentView(R.layout.main);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton1);
        toggleButton.setOnClickListener(this);
        sendButton = (Button) findViewById(R.id.button1);
        sendButton.setOnClickListener(this);
        exitButton = (Button) findViewById(R.id.button2);
        exitButton.setOnClickListener(this);
        aboutButton = (Button) findViewById(R.id.button3);
        aboutButton.setOnClickListener(this);
        editText = (EditText) findViewById(R.id.editText1);
        editText.setMaxLines(1);
        editText.setHint(getString(R.string.et1_hint));
        editText.setText("TEST");
        spinner = (Spinner) findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.speed_entries,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(OPT_DEFAULT_ENTRY);
        spinner.setOnItemSelectedListener(this);
        checkBox = (CheckBox) findViewById(R.id.checkBox1);
        checkBox.setOnClickListener(this);
    }

	public void setIoioInitialized(boolean stat) {
		this.ioioInitialized = stat;
	}

	public boolean isIoioInitialized() {
		return ioioInitialized;
	}
	
	public void enableSmsGWMode(boolean stat) {
		this.smsGWMode = stat;
	}
	
	public boolean isSmsGWEnabled() {
		return smsGWMode;
	}
	
	public void setItemAtPos(int val) {
		this.itemAtPos = val;
	}
	
	public int getItemAtPos() {
		return itemAtPos;
	}

	public void onClick(View v) {
		if ( isIoioInitialized() ) {
			switch(v.getId()) {
				case R.id.toggleButton1:
					Log.d(TAG, "ToggleButton was clicked");
//					async = new AsyncEvents(MyHelloIOIOActivity.this, out, OPT_SPD_SLOW_VAL, true);
					async = new AsyncEvents(MyHelloIOIOActivity.this, out, scon, led, getItemAtPos());
					if ( AsyncEvents.isIdle() ) {
						Log.d(TAG, "AsyncEvents WAS in idle status.");
						async.enableCalibrationMode(true);
						async.execute("In calibration mode");
						Log.d(TAG, "In calibration mode");
					}
					break;
				case R.id.button1:
					Log.d(TAG, "Button (SEND) was clicked");
					async = new AsyncEvents(MyHelloIOIOActivity.this, out, scon, led, getItemAtPos());
					hellMessage = editText.getText().toString();
					if ( AsyncEvents.isIdle() ) {
						Log.d(TAG, "AsyncEvents WAS in idle status.");
						if ( isSmsGWEnabled() && SMSReceiver.isSMSReceived() ) {
							Log.d(TAG, "SMS msg is added.");
							hellMessage += SMSReceiver.getSMSMsg();
							SMSReceiver.setSMSMsg("");
							SMSReceiver.setSMSReceived(false);
						}
						Log.d(TAG, "hell msg in onClick() = " + hellMessage);
						async.enableCalibrationMode(false);
//						async.setSendSpeed(getResources().getIntArray(R.array.speed_values)[getItemAtPos()]);
						Log.d(TAG, "SendSpeed = " + Integer.toString(async.getSendSpeed()));
						if ( hellMessage.length() > 0 )
							async.execute(hellMessage);
						hellMessage = "";
					}
					break;
				case R.id.checkBox1:
					if (checkBox.isChecked()) {
						enableSmsGWMode(true);
						Log.d(TAG, "SMSGW enabled.");
					} else {
						enableSmsGWMode(false);
						Log.d(TAG, "SMSGW disabled.");
					}
					break;
				case R.id.button3:
					AlertDialog.Builder dlg = new AlertDialog.Builder(this);
					dlg.setTitle(R.string.about_label);
					dlg.setMessage(R.string.about_text);
					dlg.setPositiveButton("OK", null);
					dlg.show();
					break;
				case R.id.button2:
					if (uart1 != null)
						uart1.close();
					if (uart2 != null)
						uart2.close();
					finish();
					break;
			} // end of switch
		} // end if if
	} // end of onClick()
	
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//    	super.onCreateOptionsMenu(menu);
//    	MenuInflater inflater = getMenuInflater();
//    	inflater.inflate(R.menu.menu, menu);
//    	return true;
//    }
    
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//    	switch (item.getItemId()) {
//    	case R.id.settings:
//    		startActivity(new Intent(this, Prefs.class));
//    		return true;
//    	case R.id.about:
//    		startActivity(new Intent(this, About.class));
//    		return true;
//    	}
//    	return false;
//    }
    
//    private void sendHELLMessage() {
//		Log.d(TAG, sendSpeedTag);

//		async.setSendSpeed(OPT_SPD_MEDIUM_VAL);			// default speed
//		if (sendSpeedTag == "Fast" )
//			async.setSendSpeed(OPT_SPD_FAST_VAL);
//		if (sendSpeedTag == "Medium" )
//			async.setSendSpeed(OPT_SPD_MEDIUM_VAL);
//		if (sendSpeedTag == "Slow" )
//			async.setSendSpeed(OPT_SPD_SLOW_VAL);
		
//		async.setSendSpeed(getResources().getIntArray(R.array.speed_values)[getItemAtPos()]);
//		Log.d(TAG, "SendSpeed = " + Integer.toString(async.getSendSpeed()));
//		Log.d(TAG, "hell msg passed to SendHELLMessage() = " + hellMessage);
//		if ( hellMessage.length() > 0 )
//			async.execute(hellMessage);
//    }
    
    class IOIOThread extends AbstractIOIOActivity.IOIOThread {
    	private static final int BAUD_MIDI = 31250;
    	private static final int BAUD_SERCON = 9600;
    	private static final int PIN_RX = 13;
    	private static final int PIN_TX = 14;
//    	private static final int PIN_SERCON_RX = 40;
    	private static final int PIN_SERCON_TX = 39;
//    	private Uart uart_;
  
    	@Override
    	protected void setup() throws ConnectionLostException {
    		uart1 = ioio_.openUart(PIN_RX, PIN_TX, BAUD_MIDI, Uart.Parity.NONE, Uart.StopBits.ONE);
    		out = uart1.getOutputStream();
    		uart2 = ioio_.openUart(IOIO.INVALID_PIN, PIN_SERCON_TX, BAUD_SERCON, Uart.Parity.NONE, Uart.StopBits.ONE);
    		scon = uart2.getOutputStream();
    		serCon("UART modules were successfully configured.");
    		// NOTE: The on-board LED is wired so that LOW (false) turns it on and HIGH (true) turns it off.
    		led = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
    		serCon("DigitalOutput module was successfully configured.");
    		serCon("IOIO board initialization has completed.");
    		setIoioInitialized(true);
    	} // end of setup()
    	
    	@Override
    	protected void loop() throws ConnectionLostException {
    		
    		if ( SMSReceiver.isSMSReceived() )
//    			Log.d(TAG, "SMS msg was delivered.");
				serCon(TAG + ": SMS msg was delivered.");
    		
    		if ( isSmsGWEnabled() ) {
//    			Log.d(TAG, "SMSGW enabled.");			
    			if (SMSReceiver.isSMSReceived() && AsyncEvents.isIdle() ) {
    				hellMessage = SMSReceiver.getSMSMsg();
//    				sendHELLMessage();
    				async = new AsyncEvents(MyHelloIOIOActivity.this, out, scon, led, getItemAtPos());
//    				async.setSendSpeed(getResources().getIntArray(R.array.speed_values)[getItemAtPos()]);
//    				Log.d(TAG, "SendSpeed = " + Integer.toString(async.getSendSpeed()));
    				serCon(TAG + ": SendSpeed = " + Integer.toString(async.getSendSpeed()));
//    				Log.d(TAG, "hell msg = " + hellMessage);
    				serCon(TAG + ": hell msg = " + hellMessage);
    				if ( hellMessage.length() > 0 )
    					async.execute(hellMessage);
    				hellMessage = "";
    				SMSReceiver.setSMSMsg("");
    				SMSReceiver.setSMSReceived(false);
//        			Log.d(TAG, "SMS message has been sent.");			
        			serCon(TAG + ": SMS message has been sent.");			
    			}
    		}    	
    	} // end of loop()
    	
    } // end of inner-class IOIOThread definition

    private void serCon(String str) {
		if ( isIoioInitialized() && (scon != null) && (str != null) && (str.length() > 0) )
			try {
//				scon.write(str.getBytes("UTF-8"));
				scon.write(str.getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d(TAG, "IOException in serCon()");
			}
	}
   
    @Override
    protected AbstractIOIOActivity.IOIOThread createIOIOThread() {
    	return new IOIOThread();
    }

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		// TODO Auto-generated method stub
		//sendSpeedTag = (String) parent.getSelectedItem();
		setItemAtPos(pos);
		Log.d(TAG, "pos = " + Integer.toString(pos));
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
//		sendSpeedTag = "medium";
	}
        
}