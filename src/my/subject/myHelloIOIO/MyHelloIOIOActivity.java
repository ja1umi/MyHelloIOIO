package my.subject.myHelloIOIO;

import my.subject.myHelloIOIO.R;
//import ioio.lib.api.IOIO;
import ioio.lib.api.DigitalOutput;
//import ioio.lib.api.IOIO;
import ioio.lib.api.Uart;
import ioio.lib.api.Uart.Parity;
import ioio.lib.api.Uart.StopBits;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.AbstractIOIOActivity;
import android.os.Bundle;
//import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;
import android.content.Intent;
//import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.util.Log;
//import java.io.IOException;
import java.io.IOException;
import java.io.OutputStream;
//import java.io.UnsupportedEncodingException;
//import java.util.Collections;
//import java.util.List;
//import java.util.ArrayList;

public class MyHelloIOIOActivity extends AbstractIOIOActivity implements OnClickListener {
	private static final String TAG = "MyHelloIOIO" ;
	private static final int OPT_SPD_FAST_VAL = 1;
	private static final int OPT_SPD_MEDIUM_VAL = 2;
	private static final int OPT_SPD_SLOW_VAL = 3;
	
	private boolean ioioInitialized;
	private String hellMessage;
//	private int sendSpeed;
	private ToggleButton toggleButton;
	private AsyncEvents async;
	private OutputStream out, scon;
	private DigitalOutput led;
//	private List<String> msgList = Collections.synchronizedList( new ArrayList<String>() );
	private EditText editText;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setIoioInitialized(true);		// set it to 'true' during development _without_ IOIO board
        setContentView(R.layout.main);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton1);
        toggleButton.setOnClickListener(this);
        View sendButton = findViewById(R.id.button1);
        sendButton.setOnClickListener(this);
        View exitButton = findViewById(R.id.button2);
        exitButton.setOnClickListener(this);
        editText = (EditText) findViewById(R.id.editText1);
        editText.setMaxLines(1);
        editText.setHint(getString(R.string.et1_hint));
        editText.setText("TEST"); 
    }

	public void setIoioInitialized(boolean stat) {
		this.ioioInitialized = stat;
	}

	public boolean isIoioInitialized() {
		return ioioInitialized;
	}

	public void onClick(View v) {
		if ( isIoioInitialized() ) {
			switch(v.getId()) {
				case R.id.toggleButton1:
					Log.d(TAG, "ToggleButton was clicked");
//					async = new AsyncEvents(MyHelloIOIOActivity.this, out, OPT_SPD_SLOW_VAL, true);
					async = new AsyncEvents(MyHelloIOIOActivity.this, out, scon, led);
					if ( AsyncEvents.isIdle() ) {
						Log.d(TAG, "AsyncEvents WAS in idle status.");
						async.setInCalibrationMode(true);
						async.execute("In calibration mode");
						Log.d(TAG, "In calibration mode");
					}
					break;
				case R.id.button1:
					Log.d(TAG, "Button (SEND) was clicked");
					async = new AsyncEvents(MyHelloIOIOActivity.this, out, scon, led);
					hellMessage = editText.getText().toString();
					if ( AsyncEvents.isIdle() ) {
						Log.d(TAG, "AsyncEvents WAS in idle status.");
						if ( Prefs.getSMSGWMode(this) && SMSReceiver.isSMSReceived() ) {
							Log.d(TAG, "SMS msg is added.");
							hellMessage += SMSReceiver.getSMSMsg();
							SMSReceiver.setSMSMsg("");
							SMSReceiver.setSMSReceived(false);
						}
						Log.d(TAG, "hell msg in onClick() = " + hellMessage);
						async.setInCalibrationMode(false);
						sendHELLMessage();
						hellMessage = "";
					}
					break;
				case R.id.button2:
					finish();
					break;
			} // end of switch
		} // end if if
	} // end of onClick()

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.settings:
    		startActivity(new Intent(this, Prefs.class));
    		return true;
    	case R.id.about:
    		startActivity(new Intent(this, About.class));
    		return true;
    	}
    	return false;
    }
    
    private void sendHELLMessage() {
		Log.d(TAG, Prefs.getSendSpeed(this));

		if (Prefs.getSendSpeed(this) == "fast" )
			async.setSendSpeed(OPT_SPD_FAST_VAL);
		if (Prefs.getSendSpeed(this) == "medium" )
			async.setSendSpeed(OPT_SPD_MEDIUM_VAL);
		if (Prefs.getSendSpeed(this) == "slow" )
			async.setSendSpeed(OPT_SPD_SLOW_VAL);

		Log.d(TAG, "hell msg passed to SendHELLMessage() = " + hellMessage);
		if ( hellMessage.length() > 0 )
			async.execute(hellMessage);
    }
    
    class IOIOThread extends AbstractIOIOActivity.IOIOThread {
    	private static final int BAUD_MIDI = 31250;
    	private static final int BAUD_SERCON = 9600;
    	private static final int PIN_RX = 32;
    	private static final int PIN_TX = 31;
    	private static final int PIN_SERCON_RX = 40;
    	private static final int PIN_SERCON_TX = 39;
    	private Uart uart_;
  
    	@Override
    	protected void setup() throws ConnectionLostException {
    		uart_ = ioio_.openUart(PIN_RX, PIN_TX, BAUD_MIDI, Parity.NONE, StopBits.ONE);
    		out = uart_.getOutputStream();
    		uart_ = ioio_.openUart(PIN_SERCON_RX, PIN_SERCON_TX, BAUD_SERCON, Parity.NONE, StopBits.ONE);
    		scon = uart_.getOutputStream();
    		serCon("UART modules were successfully configured.");
    		// NOTE: The on-board LED is wired so that LOW (false) turns it on and HIGH (true) turns it off.
    		led = ioio_.openDigitalOutput(0, true);
    		serCon("DigitalOutput module was successfully configured.");
    		serCon("IOIO board initialization has completed.");
    		setIoioInitialized(true);
    	} // end of setup()
    	
    	@Override
    	protected void loop() throws ConnectionLostException {
    		
    		if ( SMSReceiver.isSMSReceived() )
    			Log.d(TAG, "SMS msg was delivered.");
    		
    		if ( Prefs.getSMSGWMode(MyHelloIOIOActivity.this) ) {
    			Log.d(TAG, "SMSGW enabled.");			
    			if (SMSReceiver.isSMSReceived() && AsyncEvents.isIdle() ) {
    				hellMessage = SMSReceiver.getSMSMsg();
    				sendHELLMessage();
    				hellMessage = "";
    				SMSReceiver.setSMSMsg("");
    				SMSReceiver.setSMSReceived(false);
        			Log.d(TAG, "SMS message is sent through hell.");			
    			}
    		}    	
    	} // end of loop()
    	
    	private void serCon(String str) {
    		if ( isIoioInitialized() && (scon != null) && (str != null) && (str.length() > 0) )
				try {
					scon.write(str.getBytes("UTF-8"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "IOException in serCon()");
				}
    	}

    } // end of inner-class IOIOThread definition
    
    @Override
    protected AbstractIOIOActivity.IOIOThread createIOIOThread() {
    	return new IOIOThread();
    }
        
}