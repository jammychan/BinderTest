package com.example.testbinder;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.Process;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private static final String TAG = "MainActivity";
	
	private Button multiplyButton;
	private Button sendButton;
	private EditText editText;

	private boolean isAidlbound;
	private IMultiplier multiplierService;

	private boolean isMessengerBound;
	private Messenger messengerClientToServer;	//we use it to communicate with the remote service
	private Messenger messengerServerToClient = new Messenger(new Handler(){
		public void handleMessage(Message msg){
			switch(msg.what){
			case MsgDefine.SEND_STR_TO_CLIENT:
				msg.getData().setClassLoader(StringObj.class.getClassLoader());
				StringObj strObj = (StringObj) msg.getData().getParcelable(MsgDefine.PARCEL_STRING_KEY);
				Toast.makeText(MainActivity.this, strObj.str, Toast.LENGTH_SHORT).show();
				break;
			}
		}
	});
	
	private boolean isSameProcessBound;
	private Messenger messengerSameProcessToServer;
	private Messenger messengerSameProcessServerToClient = new Messenger(new Handler(){
		public void handleMessage(Message msg){
		}
	});
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        editText = (EditText)this.findViewById(R.id.titleText);
        sendButton = (Button)this.findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				sendMessage();
			}
        });
        
        multiplyButton = (Button)this.findViewById(R.id.buttonMultiply);
        multiplyButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				doMultiply();
			}
        });
        
        findViewById(R.id.register).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				sendSystemParcelClassToDiffProcessService();
			}
		});
        
        bindServiceUseMessenger();
        bindServiceUseAidl();
        bindServerSameProcess();
    }
    
    
    public void onDestroy(){
    	if(isMessengerBound)
    		this.unbindService(myConnection);
    	if(isAidlbound)
    		this.unbindService(myAidlConnection);
    	if(isSameProcessBound)
    		this.unbindService(mySameProcessConnection);
    	super.onDestroy();
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    
    private void bindServiceUseAidl(){
    	Intent intent = new Intent();
    	intent.setClassName(this.getPackageName(), "com.example.testbinder.ServiceUseAidl");
    	boolean b = this.bindService(intent, myAidlConnection, BIND_AUTO_CREATE);
    	Log.d(TAG, "bound? "+b);
    }
    
    private ServiceConnection myAidlConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			multiplierService = IMultiplier.Stub.asInterface(service);
			isAidlbound = true;
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			multiplierService = null;
			isAidlbound = false;
		}
    };
    
    
    private void bindServerSameProcess(){
    	Intent intent = new Intent();
    	intent.setClassName(getPackageName(), "com.example.testbinder.ServiceSameProcess");
    	boolean isBound = bindService(intent, mySameProcessConnection, BIND_AUTO_CREATE);
    	Log.d(TAG, "Same Process bound? " + isBound);
    }
    
    private ServiceConnection mySameProcessConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			messengerSameProcessToServer = new Messenger(binder);
			isSameProcessBound = true;
			registerMessengerToSameProcessService();
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			messengerSameProcessToServer = null;
			isSameProcessBound = false;
		}
    };
    
    
    private void bindServiceUseMessenger(){
    	Intent intent = new Intent("com.example.testbinder.ServiceUseMessenger");
    	this.bindService(intent, myConnection, BIND_AUTO_CREATE);
    }
    
    private ServiceConnection myConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			Log.i(TAG, "binder addr is "+binder.toString());
			messengerClientToServer = new Messenger(binder);
			isMessengerBound = true;
			registerMessengerToServer();
		}
		
		@Override
		public void onServiceDisconnected(ComponentName className) {
			messengerClientToServer = null;
			isMessengerBound = false;
		}
    };
    
    
    private void registerMessengerToServer(){
    	Message msg = Message.obtain();
    	msg.what = MsgDefine.REGISTER_MESSENGER;
    	msg.obj = messengerServerToClient;
    	Log.i(TAG, "messenger addr is " + messengerServerToClient.toString());
    	try {
			messengerClientToServer.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    }
    
    
    private void registerMessengerToSameProcessService(){
    	Message msg = Message.obtain();
    	msg.what = MsgDefine.REGISTER_MESSENGER_SAME_PROCESS;
    	msg.obj = messengerSameProcessServerToClient;
    	Log.i(TAG, "SameProcess messenger addr is " + messengerSameProcessServerToClient.toString());
    	try {
    		messengerSameProcessToServer.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    }
    
    
    private void sendMessage(){
    	if(isMessengerBound){
    		Bundle data = new Bundle();
    		int pid = Process.myPid();
    		data.putString("TITLE", editText.getText().toString()+"sender pid: "+pid);

    		Message newMessage = Message.obtain();
    		newMessage.setData(data);
    		newMessage.what = MsgDefine.SEND_STR_TO_SERVER;
    		
    		try {
				messengerClientToServer.send(newMessage);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
    	}
    }
    

    private void sendSystemParcelClassToDiffProcessService(){
    	if (isMessengerBound){
    		Message msg = Message.obtain();
    		msg.what = MsgDefine.SEND_SYSTEM_PARCEL_CLASS_TO_ANOTHER_PROCESS;
    		msg.obj = new Rect(1, 2, 3, 4);	//public final class Rect implements Parcelable...

    		// msg.obj = "1234"; 				
    		// fail, not parcel object 
    		// should use msg.getData().putString("key", "1234");
    		
    		// msg.obj = new StringObj("1234");
    		// fail, not system parcel objcet
    		// should use msg.getData().putParcelable("key", new StringObj("1234"));
    		try {
				messengerClientToServer.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
    	}
    }

    
    private void doMultiply(){
    	if(isAidlbound){
    		try {
				multiplierService.multiply(5, 7);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
    	}
    }
}
