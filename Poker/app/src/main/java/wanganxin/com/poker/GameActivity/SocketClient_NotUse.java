package wanganxin.com.poker.GameActivity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import wanganxin.com.poker.GameLogic.ClientProcess.ClientReceiveDeal;
import wanganxin.com.poker.GameLogic.ClientProcess.ClientSendDeal;
import wanganxin.com.poker.R;

class SocketClient_NotUse extends Activity implements View.OnClickListener {

    private EditText etIp;
    private EditText etPort;
    private Button btnConn1;
    private Button btnConn2;
    private EditText tvContent;
    private EditText etSendContent;
    private Button btnSendMessage;

    private Socket clientSocket;
    private boolean isReceivingMsgReady;
    private BufferedReader mReader;
    private BufferedWriter mWriter;
    private StringBuffer sb= new StringBuffer();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.socket_client_layout);

        etIp=(EditText) findViewById(R.id. et_ip);
        etPort=(EditText) findViewById(R.id. et_port);
        btnConn1 =(Button) findViewById(R.id.btn_start1);
        btnConn2 =(Button) findViewById(R.id.btn_start2);
        etSendContent=(EditText) findViewById(R.id.editText2 );
        btnSendMessage=(Button) findViewById(R.id.btn_send );
        tvContent=(EditText) findViewById(R.id.textView3 );

        btnConn1.setOnClickListener( this);
        btnConn2.setOnClickListener( this);
        btnSendMessage.setOnClickListener( this);

        socketClient = this;
        //sendDeal = new ClientSendDeal(this);
        //receiveDeal = new ClientReceiveDeal(this);
    }

    private static SocketClient_NotUse socketClient = null;
    public static SocketClient_NotUse getInstance() {
        return socketClient;
    }

    //客户端发送流程
    public ClientSendDeal sendDeal;
    //客户端接收流程
    public ClientReceiveDeal receiveDeal;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start1:
                if(!isReceivingMsgReady){
                    initSocket(1);
                }
                break;
            case R.id.btn_start2:
                if(!isReceivingMsgReady){
                    initSocket(2);
                }
                break;
            case R.id.btn_send:
                sendDeal.processSendUpdate();
                //send();
                break;
        }
    }

    //向服务器段发送数据
    public void send(final String msg) {

        new AsyncTask<String, Integer, String>() {

            @Override
            protected String doInBackground(String... params) {
                sendMsg(msg);
                return null;
            }

        }.execute();

    }

    /**
     * 向服务器发送消息
     */
    protected void sendMsg(String msg) {
        try {
            //通过BufferedWriter对象向服务器写数据
            mWriter.write(msg + "\n");

            //一定要调用flush将缓存中的数据写到服务器
            mWriter.flush();

            String str= "\n"+ "我:" +msg+"   "+getTime(System.currentTimeMillis ())+"\n" ;

            handler.obtainMessage(WRITE, str).sendToTarget();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case READ:
                    //获取服务器传输的数据
                    receiveDeal.receiveMsg = (String)msg.obj;

                    //处理登录返回的结果
                    receiveDeal.processReceiveUpdate();

                    sb.append((String)msg.obj);
                    tvContent.setText(sb.toString());
                break;

                case WRITE:
                    sb.append((String)msg.obj);
                    tvContent.setText(sb.toString());
                    break;
            }
        };
    };

    final static int READ = 0;
    final static int WRITE = 1;

    //获取登录编号
    public int loginIndex = 1;

    private void initSocket(final int index) {

        new Thread( new Runnable() {

            @Override
            public void run() {
                loginIndex = index;
                //获取ip地址
                //String ip = etIp.getText().toString();
                String ip = null;
                try {
                    ip = InetAddress.getByName("free.idcfengye.com").getHostAddress();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                //获取端口号
                //int port = Integer.parseInt(etPort.getText().toString());
                int port = 15263;

                try {
                    //准备好接受消息
                    isReceivingMsgReady= true;

                    //在子线程中初始化Socket对象
                    clientSocket = new Socket(ip, port);

                    //根据clientSocket.getInputStream得到BufferedReader对象，从而从输入流中获取数据
                    mReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(),"utf-8" ));

                    //根据clientSocket.getOutputStream得到BufferedWriter对象，从而从输出流中获取数据
                    mWriter= new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(),"utf-8" ));

                    while(isReceivingMsgReady){

                        if (mReader.ready() == true){

                            handler.obtainMessage(READ, mReader.readLine()).sendToTarget();

                        }
                    }

                    mWriter.close();
                    mReader.close();
                    clientSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    /**
     * 得到自己定义的时间格式的样式
     * @param millTime
     * @return
     */
    private String getTime( long millTime) {
        Date d = new Date(millTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss" );
        System. out.println(sdf.format(d));
        return sdf.format(d);
    }
}
