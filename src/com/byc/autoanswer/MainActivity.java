package com.byc.autoanswer;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.byc.autoanswer.R;
import com.byc.autoanswer.job.SpeechUtil;
import com.byc.autoanswer.util.ftp;
import com.byc.autoanswer.Config;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.widget.Toast; 
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView; 
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;; 

public class MainActivity extends Activity implements
CompoundButton.OnCheckedChangeListener,SeekBar.OnSeekBarChangeListener{

	private String TAG = "byc001";
	
    // 声明SeekBar 和 TextView对象 
    private SeekBar mSeekBar;
    private TextView tvSpeed; 
    public TextView tvRegState;
    public TextView tvRegWarm;
    public TextView tvHomePage;
    public Button btReg;
    private Button btStartWechat;
    private Button btAccessibility; 
    private Button btClose;
    private Button btNotification;
    public EditText etRegCode; 
    public TextView tvPlease;
    private SpeechUtil speaker ;
   
    //发音模式：
    private RadioGroup rgSelSoundMode; 
    private RadioButton rbFemaleSound;
    private RadioButton rbMaleSound;
    private RadioButton rbSpecialMaleSound;
    private RadioButton rbMotionMaleSound;
    private RadioButton rbChildrenSound;
    private RadioButton rbCloseSound;

    
    private BackgroundMusic mBackgroundMusic;
    private ftp mFtp;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//my codes
	    
	    //Log.d(TAG, "事件---->打开TTS");
		TAG=Config.TAG;
	    Config.getConfig(MainActivity.this);//初 始化配置类；
	    speaker=SpeechUtil.getSpeechUtil(MainActivity.this);
        //2。获取分辨率
        getResolution2();
		//3.控件初始化
		SetParams();
        //4.是否注册处理（显示版本信息(包括标题)）
		Config.bReg=getConfig().getREG();
		showVerInfo(Config.bReg);
		if(Config.bReg)//开始服务器验证：
			Sock.getSock(MainActivity.this).VarifyStart();

		//6。接收广播消息
        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.ACTION_QIANGHONGBAO_SERVICE_CONNECT);
        filter.addAction(Config.ACTION_QIANGHONGBAO_SERVICE_DISCONNECT);
        filter.addAction(Config.ACTION_NOTIFY_LISTENER_SERVICE_DISCONNECT);
        filter.addAction(Config.ACTION_NOTIFY_LISTENER_SERVICE_CONNECT);
        registerReceiver(qhbConnectReceiver, filter);
        //7.播放背景音乐：
        mBackgroundMusic=BackgroundMusic.getInstance(this);
        mBackgroundMusic.playBackgroundMusic( "bg_music.mp3", true);
        //7.7天置为试用版
        setAppToTestVersion();
		
	}
	private BroadcastReceiver qhbConnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            Log.d(TAG, "receive-->" + action);
            if(Config.ACTION_QIANGHONGBAO_SERVICE_CONNECT.equals(action)) {
            	speaker.speak("已连接"+MainActivity.this.getString(R.string.app_name)+"服务！");
            } else if(Config.ACTION_QIANGHONGBAO_SERVICE_DISCONNECT.equals(action)) {
            	speaker.speak("已中断"+MainActivity.this.getString(R.string.app_name)+"服务！");
            } else if(Config.ACTION_NOTIFY_LISTENER_SERVICE_CONNECT.equals(action)) {
            	speaker.speak("已连接"+MainActivity.this.getString(R.string.app_name)+"监听服务！");
            } else if(Config.ACTION_NOTIFY_LISTENER_SERVICE_DISCONNECT.equals(action)) {
            	speaker.speak("已中断"+MainActivity.this.getString(R.string.app_name)+"监听服务！");
            }
        }
    };

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent=new Intent();
			//Intent intent =new Intent(Intent.ACTION_VIEW,uri);
			intent.setAction("android.intent.action.VIEW");
			Uri content_url=Uri.parse(Config.homepage);
			intent.setData(content_url);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	//SeekBar接口：
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, 
            boolean fromUser) {// 在拖动中--即值在改变 
        // progress为当前数值的大小 
    	tvSpeed.setText("请设置拆包速度（毫秒）:当前拆包延迟：" + progress); 
    	
    } 
    
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {// 在拖动中会调用此方法 
    	//mSpeed.setText("xh正在调节"); 
    } 
    
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {// 停止拖动 
    	//mSpeed.setText("xh停止调节"); 
    	//保存当前值
    	//Config.getConfig(this).SetWechatOpenDelayTime(seekBar.getProgress());
    	speaker.speak("当前拆包延迟（毫秒）：" + seekBar.getProgress());
    } 
    public Config getConfig(){
    	return Config.getConfig(this);
    }
    public Sock getSock(){
    	return Sock.getSock(this);
    }
    public boolean OpenWechat(){
    	Intent intent = new Intent(); 
    	PackageManager packageManager = this.getPackageManager(); 
    	intent = packageManager.getLaunchIntentForPackage(Config.WECHAT_PACKAGENAME); 
    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP) ; 
    	this.startActivity(intent);
    	return true;
    }
  
    //配置参数：
    private void SetParams(){
    	//1。参数控件初始化+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	    mSeekBar=(SeekBar) findViewById(R.id.seekBar1);
	    tvSpeed = (TextView) findViewById(R.id.tvSpeed); 
	    tvRegState=(TextView) findViewById(R.id.tvRegState);
	    tvRegWarm=(TextView) findViewById(R.id.tvRegWarm);
	    tvHomePage=(TextView) findViewById(R.id.tvHomePage);
	    btReg=(Button)findViewById(R.id.btReg);
	    btStartWechat=(Button)findViewById(R.id.btStartWechat);
	    btAccessibility=(Button) findViewById(R.id.btAccessibility); 
	    btClose=(Button)findViewById(R.id.btClose);
	    etRegCode=(EditText) findViewById(R.id.etRegCode); 
	    tvPlease=(TextView) findViewById(R.id.tvPlease); 
	    
	 
	    //发音模式：
	    rgSelSoundMode = (RadioGroup)this.findViewById(R.id.rgSelSoundMode);
	    rbFemaleSound=(RadioButton)findViewById(R.id.rbFemaleSound);
	    rbMaleSound=(RadioButton)findViewById(R.id.rbMaleSound);
	    rbSpecialMaleSound=(RadioButton)findViewById(R.id.rbSpecialMaleSound);
	    rbMotionMaleSound=(RadioButton)findViewById(R.id.rbMotionMaleSound);
	    rbChildrenSound=(RadioButton)findViewById(R.id.rbChildrenSound);
	    rbCloseSound=(RadioButton)findViewById(R.id.rbCloseSound);   	
    	//2。++++++++++++++++++++++++读入参数+++++++++++++++++++++++++++++++++++++++++++++
	
    	//延时秒数：
    	//Config.iDelayTime=getConfig().getWechatOpenDelayTime();
    	mSeekBar.setProgress(Config.iDelayTime);
    	//发音模式：
    	if(Config.bSpeaking==Config.KEY_NOT_SPEAKING){
    		rbCloseSound.setChecked(true);//自动返回
    	}else if(Config.speaker.equals(Config.KEY_SPEAKER_FEMALE)){
    		rbFemaleSound.setChecked(true);
    	}else if(Config.speaker.equals(Config.KEY_SPEAKER_MALE)){
    		rbMaleSound.setChecked(true);
    	}else if(Config.speaker.equals(Config.KEY_SPEAKER_SPECIAL_MALE)){
    		rbSpecialMaleSound.setChecked(true);
    	}else if(Config.speaker.equals(Config.KEY_SPEAKER_EMOTION_MALE)){
    		rbMotionMaleSound.setChecked(true);
    	}else if(Config.speaker.equals(Config.KEY_SPEAKER_CHILDREN)){
    		rbChildrenSound.setChecked(true);
    	}
    	speaker.setSpeaker(Config.speaker);
    	speaker.setSpeaking(Config.bSpeaking);
    	//拆包延时：
    	Config.iDelayTime=getConfig().getWechatOpenDelayTime();
    	mSeekBar.setProgress(Config.iDelayTime);
    	//++++++++++++++++++++++++++++绑定参数+++++++++++++++++++++++++++++++++++++++++++++++++
		//1。关闭程序按钮
    	btStartWechat.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				mBackgroundMusic.stopBackgroundMusic();
				//Log.d(TAG, "事件---->打开微信");
				OpenWechat();
				Config.JobState=Config.STATE_NONE;
				speaker.speak("启动微信，祝你玩的愉快！");
			}
		});//btn.setOnClickListener(
		//2。打开辅助服务按钮
    	btNotification.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//1判断服务是否打开：
				mBackgroundMusic.stopBackgroundMusic();
				String say="";
				if(!QiangHongBaoService.isRunning()) {
					//打开系统设置中辅助功能
					Log.d(TAG, "事件---->打开系统设置中辅助功能");
					Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS); 
					startActivity(intent);
					say="找到秒抢专家，然后开启秒抢服务。";
				}else{
					say="秒抢服务已开启！如需重新开启，请重启软件。";
				}
				Toast.makeText(MainActivity.this,say, Toast.LENGTH_LONG).show();
				speaker.speak(say);
			}
		});//startBtn.setOnClickListener(
	
		//3。SeekBar处理
        mSeekBar.setOnSeekBarChangeListener(this); 
		btClose.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				System.exit(0);
			}
		});//btn.setOnClickListener(
        //5。注册流程：
		btReg.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				mBackgroundMusic.stopBackgroundMusic();
				String regCode=etRegCode.getText().toString();
				if(regCode.length()!=12){
					Toast.makeText(MainActivity.this, "授权码输入错误！", Toast.LENGTH_LONG).show();
					speaker.speak("授权码输入错误！");
					return;
				}
				getSock().RegStart(regCode);
				//Log.d(TAG, "事件---->测试");
				//System.exit(0);
			}
		});//btReg.setOnClickListener(
   	 //发音 模式
    	rgSelSoundMode.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
                // TODO Auto-generated method stub
                //获取变更后的选中项的ID
               int radioButtonId = arg0.getCheckedRadioButtonId();
               //根据ID获取RadioButton的实例
                RadioButton rb = (RadioButton)MainActivity.this.findViewById(radioButtonId);
                //更新文本内容，以符合选中项
                String sChecked=rb.getText().toString();
                String say="";
               if(sChecked.equals("关闭语音提示")){
            	   Config.bSpeaking=Config.KEY_NOT_SPEAKING;
               		say="当前设置：关闭语音提示。";
               }
               if(sChecked.equals("女声")){
            	   Config.bSpeaking=Config.KEY_SPEAKING;
            	   Config.speaker=Config.KEY_SPEAKER_FEMALE;
               		say="当前设置：女声提示。";
               }
               if(sChecked.equals("男声")){
            	   Config.bSpeaking=Config.KEY_SPEAKING;
            	   Config.speaker=Config.KEY_SPEAKER_MALE;
               		say="当前设置：男声提示。";
               }
               if(sChecked.equals("特别男声")){
            	   Config.bSpeaking=Config.KEY_SPEAKING;
            	   Config.speaker=Config.KEY_SPEAKER_SPECIAL_MALE;
               		say="当前设置：特别男声提示。";
               }
               if(sChecked.equals("情感男声")){
            	   Config.bSpeaking=Config.KEY_SPEAKING;
            	   Config.speaker=Config.KEY_SPEAKER_EMOTION_MALE;
               		say="当前设置：情感男声提示。";
               }
               if(sChecked.equals("情感儿童声")){
            	   Config.bSpeaking=Config.KEY_SPEAKING;
            	   Config.speaker=Config.KEY_SPEAKER_CHILDREN;
               		say="当前设置：情感儿童声提示。";
               }
        	   speaker.setSpeaking(Config.bSpeaking);
        	   speaker.setSpeaker(Config.speaker);
          		getConfig().setWhetherSpeaking(Config.bSpeaking);
          		getConfig().setSpeaker(Config.speaker);
              	speaker.speak(say);
              	Toast.makeText(MainActivity.this,say, Toast.LENGTH_LONG).show();
           }
        });
		//3。SeekBar处理
        mSeekBar.setOnSeekBarChangeListener(this);
        swTotalSW.setOnCheckedChangeListener(this);
        swNoPayFor.setOnCheckedChangeListener(this);
        swShowMoney.setOnCheckedChangeListener(this);
        swRobTail.setOnCheckedChangeListener(this);
	    swGuardAccount.setOnCheckedChangeListener(this);
    }
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
    	String sShow="";
        switch (compoundButton.getId()){
            case R.id.swTotalSW:
                if(compoundButton.isChecked())sShow="打开秒抢总开关 ";
                else  sShow="关闭秒抢总开关";
                Config.bSwitch=compoundButton.isChecked();
                getConfig().setSwitchTotal(Config.bSwitch);
                break;
            case R.id.swNoPayFor:
                if(compoundButton.isChecked())sShow="打开过滤赔付包功能";
                else sShow="关闭过滤赔付包功能";
                Config.bNoPayFor=compoundButton.isChecked();
                getConfig().setSwitchNoPayFor(Config.bNoPayFor);
                break;
            case R.id.swShowMoney:
                if(compoundButton.isChecked())sShow="打开显示金额功能";
                else sShow="关闭显示金额功能";
                Config.bShowMoney=compoundButton.isChecked();
                getConfig().setSwitchShowMoney(Config.bShowMoney);
                break;
            case R.id.swRobTail:
            	if(Config.bReg){
            		if(compoundButton.isChecked())sShow="打开扫尾功能";
            		else sShow="关闭扫尾功能";
            	}else{
                    if(compoundButton.isChecked())sShow="只有授权后才能使用扫尾功能";
                    compoundButton.setChecked(false);
                    
            	}
            	Config.bRobTail=compoundButton.isChecked();
            	getConfig().setRobTail(Config.bRobTail);
                break;
            case R.id.swGuardAccount:
                if(compoundButton.isChecked()){
                   	if(Config.bReg)
                   		sShow="打开防封号功能";
                   	else{
                   		compoundButton.setChecked(false);
                   		sShow="必须授权后才能打开防封号开关";
                   	}
                   }
                   else sShow="关闭防封号功能";
            	Config.bGuardAccount=compoundButton.isChecked();
            	getConfig().setGuardAccount(Config.bGuardAccount);
                   break;

        }
        Toast.makeText(this,sShow,Toast.LENGTH_SHORT).show();
        speaker.speak(sShow);
    }
    @SuppressWarnings("deprecation")
	private void getResolution2(){
        WindowManager windowManager = getWindowManager();    
        Display display = windowManager.getDefaultDisplay();    
        Config.screenWidth= display.getWidth();    
        Config.screenHeight= display.getHeight();  
        Config.currentapiVersion=android.os.Build.VERSION.SDK_INT;
    }
    private void getResolution(){
  	  DisplayMetrics dm = new DisplayMetrics();  
        getWindowManager().getDefaultDisplay().getMetrics(dm);   
        Config.screenWidth=(int)(dm.widthPixels*dm.density/2);   
        Config.screenHeight=(int)(dm.heightPixels*dm.density/2);  
        Config.currentapiVersion=android.os.Build.VERSION.SDK_INT;
  }
    //设置软件标题：
   public void setMyTitle(){
        if(Config.version.equals("")){
      	  try {
      		  PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
      		  Config.version =info.versionName;
      	  } catch (PackageManager.NameNotFoundException e) {
      		  e.printStackTrace();
            
      	  }
        }
        if(Config.bReg){
      	  setTitle(getString(R.string.app_name)+" v" + Config.version+"（正式版）");
        }else{
      	  setTitle(getString(R.string.app_name)+" v" + Config.version+"（试用版）");
        }
    }
   /**显示版本信息*/
   public void showVerInfo(boolean bReg){
       if(bReg){
       	Config.bReg=true;
       	getConfig().setREG(true);
       	tvRegState.setText("授权状态：已授权");
       	tvRegWarm.setText("升级技术售后联系"+Config.contact);
       	etRegCode.setVisibility(View.INVISIBLE);
       	tvPlease.setVisibility(View.INVISIBLE);
       	btReg.setVisibility(View.INVISIBLE);
       	speaker.speak("欢迎使用微信排雷专家！您是正版用户！" );
       	tvRegWarm.setVisibility(View.VISIBLE);
       	
       }else{
       	Config.bReg=false;
       	getConfig().setREG(false);
       	tvRegState.setText("授权状态：未授权");
       	tvRegWarm.setText(Config.warning+"技术及授权联系"+Config.contact);
       	etRegCode.setVisibility(View.VISIBLE);
       	tvPlease.setVisibility(View.VISIBLE);
       	btReg.setVisibility(View.VISIBLE);
       	speaker.speak("欢迎使用微信秒抢专家！您是试用版用户！" );
       	tvRegWarm.setVisibility(View.VISIBLE);
       	
       }
       String html = "<font color=\"blue\">官方网站下载地址(点击链接打开)：</font><br>";
       html+= "<a target=\"_blank\" href=\""+Config.homepage+"\"><font color=\"#FF0000\"><big><b>"+Config.homepage+"</b></big></font></a>";
       //html+= "<a target=\"_blank\" href=\"http://119.23.68.205/android/android.htm\"><font color=\"#0000FF\"><big><i>http://119.23.68.205/android/android.htm</i></big></font></a>";
       tvHomePage.setTextColor(Color.BLUE);
       tvHomePage.setBackgroundColor(Color.WHITE);//
       //tvHomePage.setTextSize(20);
       tvHomePage.setText(Html.fromHtml(html));
       tvHomePage.setMovementMethod(LinkMovementMethod.getInstance());
       setMyTitle();//设置软件 标题
       updateMeWarning(Config.version,Config.new_version);//软件更新提醒
   }
   /**  软件更新提醒*/
   private void updateMeWarning(String version,String new_version){
	   try{
		   float f1=Float.parseFloat(version);
		   float f2=Float.parseFloat(new_version);
	   if(f2>f1){
		   showUpdateDialog();
	   }
	   } catch (Exception e) {  
           e.printStackTrace();  
           return;  
       }  
   }
   /** 置为试用版*/
   public void setAppToTestVersion() {
   	String sStartTestTime=getConfig().getStartTestTime();//取自动置为试用版的开始时间；
   	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",Locale.US);//yyyy-MM-dd_HH-mm-ss
   	String currentDate =sdf.format(new Date());//取当前时间；
   	int timeInterval=getConfig().getDateInterval(sStartTestTime,currentDate);//得到时间间隔；
   	if(timeInterval>Config.TestTimeInterval){//7天后置为试用版：
   		showVerInfo(false);
   		ftp.getFtp().DownloadStart();//下载服务器信息;
   	}
   }
   private   void   showUpdateDialog(){ 
       /* @setIcon 设置对话框图标 
        * @setTitle 设置对话框标题 
        * @setMessage 设置对话框消息提示 
        * setXXX方法返回Dialog对象，因此可以链式设置属性 
        */ 
       final AlertDialog.Builder normalDialog=new  AlertDialog.Builder(MainActivity.this); 
       normalDialog.setIcon(R.drawable.ic_launcher); 
       normalDialog.setTitle(  "升级提醒"  );
       normalDialog.setMessage("有新版软件，是否现在升级？"); 
       normalDialog.setPositiveButton("确定",new DialogInterface.OnClickListener(){
           @Override 
           public void onClick(DialogInterface dialog,int which){ 
               //...To-do
    		   Uri uri = Uri.parse(Config.download);    
    		   Intent it = new Intent(Intent.ACTION_VIEW, uri);    
    		   startActivity(it);  
           }
       }); 
       normalDialog.setNegativeButton("关闭",new DialogInterface.OnClickListener(){ 
           @Override 
           public void onClick(DialogInterface dialog,   int   which){ 
           //...To-do 
           } 
       }); 
       // 显示 
       normalDialog.show(); 
   } 

}
