package com.example.testbinder;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class ServiceSameProcess extends Service {

	protected static final String TAG = "ServiceSameProcess";
	Messenger sendToClientMessenger = null;
	
	private final Messenger messenger = new Messenger(new Handler(){
		public void handleMessage(Message msg){
			switch(msg.what){
			case MsgDefine.REGISTER_MESSENGER_SAME_PROCESS:
				sendToClientMessenger = (Messenger) msg.obj;
				Log.i(TAG, "SameProcess Messenger addr is " + sendToClientMessenger.toString());
				break;
			default:
				break;
			}
		}
	});
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		IBinder binder = messenger.getBinder();
		Log.i(TAG, "same process binder addr is  " + binder.toString());
		return binder;
	}
}
