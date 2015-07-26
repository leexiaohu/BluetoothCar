package com.louis.bluetoothcar;

import java.util.concurrent.ScheduledExecutorService;

import com.louis.bluetoothcar.utils.BytesUtils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private static final String TAG = "MainActivity";
    private static final boolean D = true;
    
	public static final int REQUEST_CONNECT_DEVICE=1;
	private static final int REQUEST_ENABLE_BT = 2;
		
	private Button upButton=null;
	private Button downButton=null;
	private Button leftButton=null;
	private Button rightButton=null;
	private Button stopButton=null;
	
    private BluetoothAdapter mBluetoothAdapter=null;
    private BluetoothService mBluetoothService=null;
    private String mConnectedDeviceName;
    private SharedPreferences reference;
    @SuppressWarnings("unused")
	private ScheduledExecutorService mExecuter=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(D) Log.e(TAG, "+++ ON CREATE +++");
		
		getActionBar().setBackgroundDrawable(getWallpaper());
		setContentView(R.layout.activity_main);
		
		mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter==null){
			Toast.makeText(this, "�ֻ���֧������", Toast.LENGTH_LONG).show();
			return;
		}
		reference=PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
		setControlButtonListener();
		
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if(D) Log.e(TAG, "++ ON START ++");
		if(!mBluetoothAdapter.isEnabled()){
			Intent intent=new Intent();
			intent.setAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, REQUEST_ENABLE_BT);
		}else if(mBluetoothService==null){
			setupService();
		}
	}	

	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mBluetoothService != null) mBluetoothService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
	}
	
	
	/**
	 * ���ÿ��ư�ť������
	 */
	private void setControlButtonListener(){
		upButton=(Button) findViewById(R.id.button_up);
		upButton.setOnTouchListener(new mTouchUpListener());
		downButton=(Button) findViewById(R.id.button_down);
		downButton.setOnTouchListener(new mTouchDownListener());
		
		leftButton=(Button) findViewById(R.id.button_left);
		leftButton.setOnTouchListener(new mTouchLeftListener());
		
		rightButton=(Button) findViewById(R.id.button_right);
		rightButton.setOnTouchListener(new mTouchRightListener());
		
		stopButton=(Button) findViewById(R.id.button_stop);
		stopButton.setOnTouchListener(new mTouchStopListener());
	}
	class mTouchUpListener implements OnTouchListener{
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			Log.d(TAG, "upLstener touch");
			
			String up=reference.getString("pref_key_up_commond", Constant.UP);
			sendMessage(up);
			
			return false;
		}
		
	}
	class mTouchDownListener implements OnTouchListener{
			
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			Log.d(TAG, "downListener touch");
			String down=reference.getString("pref_key_down_commond", Constant.DOWN);
			sendMessage(down);
			
			return false;
		}
	}
	class mTouchLeftListener implements OnTouchListener{
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			Log.d(TAG, "leftListener touch");
			String left=reference.getString("pref_key_left_commond", Constant.LEFT);
			sendMessage(left);
			
			return false;
		}
	}
	class mTouchRightListener implements OnTouchListener{
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			Log.d(TAG, "rightListener touch");
			String right=reference.getString("pref_key_right_commond", Constant.RIGHT);
			sendMessage(right);
			
			return false;
		}
	}
	class mTouchStopListener implements OnTouchListener{
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			Log.d(TAG, "stopListener touch");
			String stop=reference.getString("pref_key_stop_commond", Constant.STOP);
			sendMessage(stop);
			
			return false;
		}
	}
	
	Handler mHandler =new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what){
			case Constant.MESSAGE_STATE_CHANGE:
				switch(msg.arg1){
				case Constant.STATE_NONE:
					setTitle(R.string.title_not_connected);
					break;
				case Constant.STATE_CONNECTING:
					setTitle(R.string.title_connecting);
					break;
				case Constant.STATE_CONNECTED:
					
					setTitle(getString(R.string.title_connected_to,mConnectedDeviceName));
				}
				break;
			case Constant.MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                //����յ�����Ϣ
                Toast.makeText(getApplicationContext(), "�յ�����Ϣ�� "+readMessage,Toast.LENGTH_SHORT).show();
				break;
			case Constant.MESSAGE_WRITE:
//				byte[] writeBuf=(byte[]) msg.obj;				
//				String writeMessage=new String(writeBuf);
				//�����������Ϣ
//				Toast.makeText(getApplicationContext(), writeMessage,Toast.LENGTH_SHORT).show();
				break;
			case Constant.MESSAGE_DEVICE_NAME:
				mConnectedDeviceName=msg.getData().getString(Constant.DEVICE_NAME);
				Toast.makeText(getApplicationContext(), "�����ӵ� "+ mConnectedDeviceName, Toast.LENGTH_LONG).show();
				break;
			case Constant.MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(Constant.TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
			}
		}
		
	};
	
	
	/**
	 * ������Ϣ
	 * @param message
	 */
	
	public synchronized void sendMessage(final String message){
		if(mBluetoothService.getState()!=Constant.STATE_CONNECTED){
			Toast.makeText(this, "����δ����", Toast.LENGTH_LONG).show();
			return;
		}
		if(message.length()>0){			
			// TODO Auto-generated method stub
//			byte[] buffer=message.getBytes();
			byte[] buffer=BytesUtils.hexStringToByte(message);
			if(buffer!=null)
				mBluetoothService.write(buffer);		
		}
	}
	/**
	 * �����豸
	 * @param intent
	 */ 
	public void connectDevice(Intent intent){
		Log.d(TAG, "connectDevice()");
		String address=intent.getExtras().getString(DevicesActivity.EXTRA_DEVICE_ADDRESS);
		BluetoothDevice device=mBluetoothAdapter.getRemoteDevice(address);
		mBluetoothService.connect(device);
		
	}
	/**
	 * ����service
	 */
	public void setupService(){
		
		mBluetoothService=new BluetoothService(MainActivity.this, mHandler);
		mBluetoothService.init();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		Intent intent=null;
		switch(item.getItemId()){
		case R.id.secure_connect_scan:
			intent=new Intent();
			intent.setClass(this, DevicesActivity.class);
			startActivityForResult(intent,REQUEST_CONNECT_DEVICE);
			return true;
		case R.id.action_discoverable:
			ensureDiscoverable();
			return true;
		case R.id.action_settings:
			intent=new Intent();
			intent.setClass(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		case R.id.action_exit:
			finish();
			return true;
		}
		return false;
	}
	
	
	/**
	 * �����豸�ɱ�����
	 */
	public void ensureDiscoverable(){
		
		Log.v(TAG, "++ensureDiscoverable()++");
		
		if(mBluetoothAdapter.getScanMode()!=BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
			Intent intent=new Intent();
			intent.setAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(intent);
		}
		Toast.makeText(this, "������300ms�ڿɱ������豸������", Toast.LENGTH_LONG).show();
	}
	@Override
	public void setTitle(CharSequence title) {
		// TODO Auto-generated method stub
		final ActionBar actionBar = getActionBar();
        if(actionBar!=null){
        	actionBar.setSubtitle(title);
        }
	}

	@Override
	public void setTitle(int titleId) {
		// TODO Auto-generated method stub
		 final ActionBar actionBar = getActionBar();
	        if(actionBar!=null){
	        	actionBar.setSubtitle(titleId);
	        }
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(D) Log.d(TAG, "onActivityResult " + resultCode);	
		switch(requestCode){
		case REQUEST_CONNECT_DEVICE:
			if(resultCode==Activity.RESULT_OK){
				connectDevice(data);
			}
			else{
				Log.d(TAG, "ȡ��ɨ��");
				Toast.makeText(this, "ȡ��ɨ��", Toast.LENGTH_SHORT).show();
			}
			break;
		case REQUEST_ENABLE_BT:
			if (resultCode == Activity.RESULT_OK) {
                setupService();
            } else {                
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, "��������ʧ��", Toast.LENGTH_SHORT).show();
            }
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
