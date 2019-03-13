using pokerServer.NetworkProcess;
using pokerServer.NetworkProcess.Entity;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Windows.Forms;

namespace pokerServer {
    public partial class serverForm : Form {

        //当前服务器
        public static serverForm server;
        //维护已经登录的用户（用户对应一个大厅）（线程安全）
        public ConcurrentDictionary<string, GameLobby> players = new ConcurrentDictionary<string, GameLobby>();
        public SortedSet<GameLobby> gameLobbies = new SortedSet<GameLobby>(new GameLobbySort());

        public serverForm() {
            InitializeComponent();
            server = this;
        }

        /// <summary>
        /// 等待客户端的连接 并且创建与之通信的Socket
        /// </summary>
        //static Socket socketSend;
        void Listen(object o) {
            try {
                Socket socketWatch = o as Socket;
                while (true) {
                    //等待接收客户端连接
                    Socket socketSend = socketWatch.Accept();

                    //获取客户端的IP和端口
                    IPEndPoint ipEndClient = (IPEndPoint)socketSend.RemoteEndPoint;
                    //输出客户端的IP和端口
                    Console.Write("Connect with {0} at port {1}", ipEndClient.Address, ipEndClient.Port);

                    ShowMsg(socketSend.RemoteEndPoint.ToString() + ":" + "连接成功!");

                    //创建一个服务器处理流程
                    ServerDeal serverdeal = new ServerDeal();
                    serverdeal.socket = socketSend;

                    //开启一个新线程，执行接收消息方法
                    Thread r_thread = new Thread(Received);
                    r_thread.IsBackground = true;
                    r_thread.Start(serverdeal);
                }
            }
            catch (Exception ee) {
                ShowMsg(ee.ToString());
            }
        }

        /// <summary>
        /// 服务器端不停的接收客户端发来的消息
        /// </summary>
        /// <param name="o"></param>
        void Received(object o) {
            ServerDeal serverDeal = o as ServerDeal;
            Socket curSocket = serverDeal.socket;
            //设置接受时间的超时时间（客户端会定时发心跳包）
            //curSocket.ReceiveTimeout = Constants.HEART_BEAT_TIME;
            try {
                while (true) {
                    //客户端连接服务器成功后，服务器接收客户端发送的消息
                    byte[] buffer = new byte[1024 * 1024 * 3];
                    //实际接收到的有效字节数
                    if (IsSocketConnected(curSocket, serverDeal)) {
                        int len = curSocket.Receive(buffer);
                        //如果当前已断开
                        if (len <= 0 || !IsSocketConnected(curSocket, serverDeal)) {
                            clientBreakUp(serverDeal, curSocket);
                            break;
                        }

                        //获取服务器传来的消息
                        serverDeal.msg = Encoding.UTF8.GetString(buffer, 0, len);

                        //屏幕上显示（测试)
                        ShowMsg(curSocket.RemoteEndPoint + ": receive " + serverDeal.msg);

                        //服务器处理该进程
                        serverDeal.processUpdate();
                    } else {
                        clientBreakUp(serverDeal, curSocket);
                        break;
                    }
                }
            }
            catch (Exception ee) {
                ShowMsg(ee.ToString());
                clientBreakUp(serverDeal, curSocket);
            }
        }

        static bool IsSocketConnected(Socket s, ServerDeal serverDeal) {
            return !((s.Poll(1000, SelectMode.SelectRead) && (s.Available == 0)) || !s.Connected);
        }

        //断线的操作
        private void clientBreakUp(ServerDeal serverDeal, Socket curSocket) {
            //调用客户端断开
            serverDeal.clientBreak(true);
            ShowMsg(curSocket.RemoteEndPoint + ": 当前已断开");
            curSocket.Close();
            serverDeal = null;
        }

        /// <summary>
        /// 服务器向客户端发送消息
        /// </summary>
        /// <param name="str"></param>
        public static void Send(string str, Socket socketSend) {
            byte[] buffer = Encoding.UTF8.GetBytes(str + "\n");
            try {
                socketSend.Send(buffer);
            } catch (Exception e) {
                server.ShowMsg(e.ToString());
            }
        }

        public void ShowMsg(string msg) {
            textBox1.AppendText(msg + "\r\n");
        }

        private void Form1_Load(object sender, EventArgs e) {
            Control.CheckForIllegalCrossThreadCalls = false;
        }

        private void bt_send_Click(object sender, EventArgs e) {
            //Send(txt_msg.Text.Trim());
        }


        private IPAddress ip;
        private IPEndPoint point;
        private void button1_Click(object sender, EventArgs e) {
            //IPAddress ip = IPAddress.Any;
            ip = IPAddress.Parse("172.16.0.14");
            //创建对象端口
            point = new IPEndPoint(ip, Convert.ToInt32("8000"));

            bindSocket();
        }

        private void bindSocket() {
            try {
                //点击开始监听时 在服务端创建一个负责监听IP和端口号的Socket
                Socket socketWatch = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);

                socketWatch.Bind(point);//绑定端口号
                ShowMsg("监听成功!");
                socketWatch.Listen(10);//设置监听

                //创建监听线程
                Thread thread = new Thread(Listen);
                thread.IsBackground = true;
                thread.Start(socketWatch);
            } catch (Exception ee) {
                MessageBox.Show("没有绑定成功！" + ee.ToString());
            }
        }

        private void bt_connnect_Click(object sender, EventArgs e) {

            //IPAddress ip = IPAddress.Any;
            ip = IPAddress.Parse(tb_ip.Text);
            //创建对象端口
            point = new IPEndPoint(ip, Convert.ToInt32(tb_port.Text));

            bindSocket();
        }
    }
}