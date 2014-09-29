package com.example.testbinder;

import android.os.Bundle;
import android.os.Handler;
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
	
	//we use it to communicate with the remote service
	private Messenger messenger;
	private Messenger messengerServerToClient = new Messenger(new Handler(){
		public void handleMessage(Message msg){
			switch(msg.what){
			case MsgDefine.SEND_STR_TO_CLIENT:
				msg.getData().setClassLoader(StringObj.class.getClassLoader());
				String str = msg.getData().getString("str");
				StringObj strObj = (StringObj) msg.getData().getParcelable("string");
				Toast.makeText(MainActivity.this, strObj.str + "  " + str, Toast.LENGTH_SHORT).show();
//				Toast.makeText(MainActivity.this, "strObj.str " + msg.arg1 + " " + msg.arg2, Toast.LENGTH_SHORT).show();
				break;
			}
		}
	});
	
	private boolean isBound;
	
	private IMultiplier multiplierService;
	private boolean isAidlbound;

	private Button sendButton;
	private EditText editText;
	
	private Button multiplyButton;
	
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
			}
		});
        
        bindService();
        
        bindAidlService();
    }
    
    public void onDestroy(){
    	
    	if(isBound)
    		this.unbindService(myConnection);
    	if(isAidlbound)
    		this.unbindService(myAidlConnection);
    	super.onDestroy();
    	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    
    private void bindAidlService(){
    	Intent intent = new Intent();
    	intent.setClassName(this.getPackageName(), "com.example.testbinder.ClientAidl");
    	boolean b = this.bindService(intent, myAidlConnection, BIND_AUTO_CREATE);
    	Log.d(TAG, "bound? "+b);
    	
    }
    
    private void doMultiply(){
    	if(isAidlbound){
    		
    		try {
				multiplierService.multiply(5, 7);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    	}
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
    
    
    private void bindService(){
    	Intent intent = new Intent("com.example.testbinder.ClientService");
    	this.bindService(intent, myConnection, BIND_AUTO_CREATE);
    }
    
    private void registerMessengerToServer(){
    	Message msg = Message.obtain();
    	msg.what = MsgDefine.REGISTER_MESSENGER;
    	msg.obj = messengerServerToClient;
    	Log.i(TAG, "messenger addr is " + messengerServerToClient.toString());
    	try {
			messenger.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    }
    
    private void sendMessage(){
    	if(isBound){
    		Message newMessage = Message.obtain();
    		
    		Bundle data = new Bundle();
    		int pid = Process.myPid();
    		data.putString("TITLE", editText.getText().toString()+"sender pid: "+pid);
    		
    		newMessage.setData(data);
    		newMessage.what = MsgDefine.SEND_STR_TO_SERVER;
    		Rect rect = new Rect();
    		rect.left = 1028;
    		newMessage.obj = rect;
    		
    		try {
				messenger.send(newMessage);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
    		
    	}
    }
    
    private ServiceConnection myConnection = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			Log.i(TAG, "binder addr is "+binder.toString());
			messenger = new Messenger(binder);
			isBound = true;
			registerMessengerToServer();
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			messenger = null;
			isBound = false;
		}
    };
}
