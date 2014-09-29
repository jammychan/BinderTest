package com.example.testbinder;

import android.app.Service;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import android.os.Process;

public class ServiceUseMessenger extends Service {
	private static final String TAG = "ServiceUseMessenger";
	
	Messenger sendToClientMessenger = null;
	/**
	 * the service receives here the messages from a remote process
	 * @author ado
	 */
	private final Messenger messenger = new Messenger(new IncomingHandler());
	private class IncomingHandler extends Handler{
		public void handleMessage(Message msg){
			switch(msg.what){
			case MsgDefine.REGISTER_MESSENGER:
				sendToClientMessenger = (Messenger) msg.obj;
				Log.i(TAG, "another process messenger addr is " + sendToClientMessenger.toString());
				break;
			case MsgDefine.SEND_STR_TO_SERVER:
				Bundle data = msg.getData();
				String title = data.getString("TITLE");
				int pid = Process.myPid();
				String s = title + "\nservice pid: "+pid;
				Log.i(TAG, s);
				try {
					if (null != sendToClientMessenger){
						Message msgToClient = Message.obtain();
						msgToClient.what = MsgDefine.SEND_STR_TO_CLIENT;
						msgToClient.getData().putParcelable(MsgDefine.PARCEL_STRING_KEY, new StringObj(s));
						sendToClientMessenger.send(msgToClient);
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			break;
			case MsgDefine.SEND_SYSTEM_PARCEL_CLASS_TO_ANOTHER_PROCESS:
				Rect rect = (Rect) msg.obj;
				String str = "" + rect.left + " " + rect.right + " " + rect.top + " " + rect.bottom;
				Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
				break;
			}
		}
	}


	//the IBinder returned here is used by the messenger to communicate with the associated handler
	@Override
	public IBinder onBind(Intent arg0) {
		IBinder ibinder = messenger.getBinder(); 
		Log.i(TAG, "another process binder addr is "+ibinder.toString());
		return ibinder;
	}
}
