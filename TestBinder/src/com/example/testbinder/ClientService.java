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

public class ClientService extends Service {
	private static final String TAG = "ClientService";
	
	Messenger sendToClientMessenger = null;
	/**
	 * the service receives here the messages from a remote process
	 * @author ado
	 *
	 */
	private class IncomingHandler extends Handler{

		public void handleMessage(Message msg){
			switch(msg.what){
			case MsgDefine.REGISTER_MESSENGER:
				sendToClientMessenger = (Messenger) msg.obj;
				Log.i(TAG, "messenger addr is " + sendToClientMessenger.toString());
				break;
			case MsgDefine.SEND_STR_TO_SERVER:
				Bundle data = msg.getData();
				String title = data.getString("TITLE");
				int pid = Process.myPid();
				String s = title + "\nservice pid: "+pid;
				s = s + ((Rect)msg.obj).left;
				Log.i(TAG, s);
				try {
					if (null != sendToClientMessenger){
						Message msgToClient = Message.obtain();
						msgToClient.what = MsgDefine.SEND_STR_TO_CLIENT;
						msgToClient.arg1 = 12;
						msgToClient.arg2 = 8;
//						msgToClient.obj = new StringObj(s);
						msgToClient.getData().putString("str", "ocean sky");
						msgToClient.getData().putParcelable("string", new StringObj(s));
						sendToClientMessenger.send(msgToClient);
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			break;
			}
		}
	}

	private final Messenger messenger = new Messenger(new IncomingHandler());
	
	//the IBinder returned here is used by the messenger to communicate with the associated handler
	@Override
	public IBinder onBind(Intent arg0) {
		IBinder ibinder = messenger.getBinder(); 
		Log.i(TAG, "binder addr is "+ibinder.toString());
		return ibinder;
	}

}
