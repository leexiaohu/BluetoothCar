package com.louis.bluetoothcar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class BluetoothService extends Service {
	// Debugging
    private static final String TAG = "BluetoothChatService";
    private static final boolean D = true;
    
    //蓝牙串口服务UUID   
	private static final UUID SerialPortServiceClass_UUID =UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	//UUID
    private static final UUID uuid=UUID
    		.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    
    //Member filed
    
	private Handler mHandler=null;
	private BluetoothAdapter mAdapter=null;
	private int state;
	private ClientThread clientThread=null;
	private MessageThread messageThread=null;
	public BluetoothService(Context context,Handler mHandler) {		
		this.mHandler = mHandler;
		this.mAdapter = BluetoothAdapter.getDefaultAdapter();
		this.state = Constant.STATE_NONE;
	}

	public synchronized void init(){
		if (D) Log.d(TAG, "start");
		if(clientThread!=null){
			clientThread.cancle();
			clientThread=null;
		}
		if(messageThread!=null){
			messageThread.cancle();
			messageThread=null;
		}
		setState(Constant.STATE_NONE);
	}
	public synchronized int getState() {
		return state;
	}

	public synchronized void setState(int state) {
		this.state = state;
		//状态改变时发送给通知Handler
		mHandler.obtainMessage(Constant.MESSAGE_STATE_CHANGE, 
						state, -1).sendToTarget();
	}

	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public void stop() {
		// TODO Auto-generated method stub
		 if (D) Log.d(TAG, "stop");
		 BluetoothService.this.init();
	}
	
	//客户端进程
	private class ClientThread  extends Thread{
		private BluetoothSocket mSocket;
		private BluetoothDevice mDevice;
		public  ClientThread(BluetoothDevice device){
			mDevice=device;
			try {
				mSocket=mDevice.createRfcommSocketToServiceRecord(SerialPortServiceClass_UUID);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public void cancle() {
			// TODO Auto-generated method stub
			try {
				mSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public void run(){
			setName("客户端进程");
			mAdapter.cancelDiscovery();
			try {
				mSocket.connect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					mSocket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				connecteFailed();
				return;
			}
			synchronized (BluetoothService.this) {
	            	clientThread = null;
	        }
			connected(mSocket,mDevice);
		}
	}
	private class MessageThread extends Thread{
		private BluetoothSocket mSocket;
		private InputStream inputstream;
		private OutputStream outputstream;
		public MessageThread(BluetoothSocket socket){
			mSocket=socket;
			InputStream tmpIn=null;
			OutputStream tmpOut=null;
			try {
				tmpIn=socket.getInputStream();
				tmpOut=socket.getOutputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			inputstream=tmpIn;
			outputstream=tmpOut;
			
		}
		public void run(){
			byte[] buffer=new byte[1024];
			int bytes;
			while(true){
				try {
					bytes=inputstream.read(buffer);
					mHandler.obtainMessage(Constant.MESSAGE_READ, bytes, -1, buffer)
	                	.sendToTarget();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					connectionLost();
					e.printStackTrace();
					break;
				}
			}
		}
		public void write(byte[] buffer){
			try {
				outputstream.write(buffer);
                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(Constant.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
		}
		
		public void cancle() {
			// TODO Auto-generated method stub
			try {
				mSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				 Log.e(TAG, "close() of connect socket failed", e);
			}
		}
		
	}
	public synchronized void write(byte[] out){
		MessageThread m;
		synchronized (this) {
            if (state != Constant.STATE_CONNECTED) return;
            m = messageThread;
        }
		if(m!=null){
//			System.out.println(new String(out));
			m.write(out);
		}
	}
	//创建连接
	public synchronized void connect(BluetoothDevice device) {
		// TODO Auto-generated method stub
		if(state==Constant.STATE_CONNECTING){
			if(clientThread!=null){
				clientThread.cancle();
				clientThread=null;
			}
		}
		if(messageThread!=null){
			messageThread.cancle();
			messageThread=null;
		}
		clientThread=new ClientThread(device);
		clientThread.start();
		setState(Constant.STATE_CONNECTING);
	}
	//连接后
	private synchronized void connected(BluetoothSocket socket,BluetoothDevice device){
		if(clientThread!=null){
			clientThread.cancle();
			clientThread=null;
		}
		if(messageThread!=null){
			messageThread.cancle();
			messageThread=null;
		}
		messageThread=new MessageThread(socket);
		messageThread.start();
		
		Message msg=mHandler.obtainMessage(Constant.MESSAGE_DEVICE_NAME);
		Bundle bundle=new Bundle();
		bundle.putString(Constant.DEVICE_NAME,device.getName());
		msg.setData(bundle);
		msg.sendToTarget();
		setState(Constant.STATE_CONNECTED);
		
	}
	
	//连接蓝牙设备连接失败
	private void connecteFailed(){
		Message msg = mHandler.obtainMessage(Constant.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constant.TOAST, "无法连接到设备");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        BluetoothService.this.init();
	}
	//与设备失去连接
	private void connectionLost(){
		Message msg = mHandler.obtainMessage(Constant.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constant.TOAST, "设备连接丢失");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        BluetoothService.this.init();
	}

}
