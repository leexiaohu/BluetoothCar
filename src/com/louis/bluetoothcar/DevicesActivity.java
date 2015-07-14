package com.louis.bluetoothcar;


import java.util.Set;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DevicesActivity extends Activity {
	
	// Debugging
    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    //成员
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter <String> mNewDevicesArrayAdapter;
    private ListView pairedListView,newDevicesListView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_devices);
		
		setResult(Activity.RESULT_CANCELED);
		
		mBtAdapter=BluetoothAdapter.getDefaultAdapter();
		//扫描设备
		doDiscovery();
		
		//初始化适配器
		mPairedDevicesArrayAdapter=new ArrayAdapter<String>(this, R.layout.device);
		mNewDevicesArrayAdapter=new ArrayAdapter<String>(this, R.layout.device);
		
		//设置适配器
		pairedListView=(ListView) findViewById(R.id.paired_devices);
		pairedListView.setAdapter(mPairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);
		
		newDevicesListView=(ListView) findViewById(R.id.new_devices);
		newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(mDeviceClickListener);
		
		//当发现新设备时注册广播		
		IntentFilter filter=new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);
		//结束扫描时注册一个广播
		filter=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);
		
		//获取本地蓝牙适配器
		
		Set<BluetoothDevice> pairedDevices=mBtAdapter.getBondedDevices();
		if(pairedDevices.size()>0){
			for(BluetoothDevice device:pairedDevices){
				mPairedDevicesArrayAdapter.add(device.getName()+"    "+device.getAddress());
			}
		}else{
			 String noDevices = getResources().getText(R.string.none_paired_device).toString();
			 mPairedDevicesArrayAdapter.add(noDevices);
		}
	}

	OnItemClickListener mDeviceClickListener =new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			mBtAdapter.cancelDiscovery();
			String info=((TextView) view).getText().toString();
			String address=info.substring(info.length()-17);
			
			Intent intent=new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
			setResult(Activity.RESULT_OK,intent);
			finish();
		}	
	};
	
	/**
	 * 开始扫描周围蓝牙设备
	 */
	private void doDiscovery() {
		// TODO Auto-generated method stub
		if(D) Log.d(TAG, "+++doDiscovery()+++");
		setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.scanning);
		if(mBtAdapter.isDiscovering()){
			mBtAdapter.cancelDiscovery();
		}
		
		mBtAdapter.startDiscovery();
	}
	/**
	 * 广播接收器
	 */
	BroadcastReceiver mReceiver =new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action=intent.getAction();
			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if(device.getBondState()!=BluetoothDevice.BOND_BONDED){
					mNewDevicesArrayAdapter.add(device.getName()+"    "+device.getAddress());
				}
			}else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
				setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {               	
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
			}
		}
		
	};
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.d(TAG, "++onDestroy()++");
		super.onDestroy();
		if(mBtAdapter!=null){
			mBtAdapter.cancelDiscovery();
		}
		this.unregisterReceiver(mReceiver);
	}

	

}
