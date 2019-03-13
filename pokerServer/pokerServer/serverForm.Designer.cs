namespace pokerServer {
    partial class serverForm {
        /// <summary>
        /// 必需的设计器变量。
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// 清理所有正在使用的资源。
        /// </summary>
        /// <param name="disposing">如果应释放托管资源，为 true；否则为 false。</param>
        protected override void Dispose(bool disposing) {
            if (disposing && (components != null)) {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows 窗体设计器生成的代码

        /// <summary>
        /// 设计器支持所需的方法 - 不要
        /// 使用代码编辑器修改此方法的内容。
        /// </summary>
        private void InitializeComponent() {
            this.panel1 = new System.Windows.Forms.Panel();
            this.button1 = new System.Windows.Forms.Button();
            this.bt_send = new System.Windows.Forms.Button();
            this.bt_connnect = new System.Windows.Forms.Button();
            this.lb_port = new System.Windows.Forms.Label();
            this.tb_port = new System.Windows.Forms.TextBox();
            this.lb_Ip = new System.Windows.Forms.Label();
            this.tb_ip = new System.Windows.Forms.TextBox();
            this.txt_msg = new System.Windows.Forms.TextBox();
            this.textBox1 = new System.Windows.Forms.TextBox();
            this.panel1.SuspendLayout();
            this.SuspendLayout();
            // 
            // panel1
            // 
            this.panel1.Controls.Add(this.button1);
            this.panel1.Controls.Add(this.bt_send);
            this.panel1.Controls.Add(this.bt_connnect);
            this.panel1.Controls.Add(this.lb_port);
            this.panel1.Controls.Add(this.tb_port);
            this.panel1.Controls.Add(this.lb_Ip);
            this.panel1.Controls.Add(this.tb_ip);
            this.panel1.Location = new System.Drawing.Point(28, 16);
            this.panel1.Margin = new System.Windows.Forms.Padding(4);
            this.panel1.Name = "panel1";
            this.panel1.Size = new System.Drawing.Size(406, 93);
            this.panel1.TabIndex = 0;
            // 
            // button1
            // 
            this.button1.Location = new System.Drawing.Point(46, 53);
            this.button1.Name = "button1";
            this.button1.Size = new System.Drawing.Size(117, 30);
            this.button1.TabIndex = 5;
            this.button1.Text = "云服务器监听";
            this.button1.UseVisualStyleBackColor = true;
            this.button1.Click += new System.EventHandler(this.button1_Click);
            // 
            // bt_send
            // 
            this.bt_send.Location = new System.Drawing.Point(316, 55);
            this.bt_send.Margin = new System.Windows.Forms.Padding(4);
            this.bt_send.Name = "bt_send";
            this.bt_send.Size = new System.Drawing.Size(77, 26);
            this.bt_send.TabIndex = 3;
            this.bt_send.Text = "发送";
            this.bt_send.UseVisualStyleBackColor = true;
            this.bt_send.Click += new System.EventHandler(this.bt_send_Click);
            // 
            // bt_connnect
            // 
            this.bt_connnect.Location = new System.Drawing.Point(183, 54);
            this.bt_connnect.Margin = new System.Windows.Forms.Padding(4);
            this.bt_connnect.Name = "bt_connnect";
            this.bt_connnect.Size = new System.Drawing.Size(113, 29);
            this.bt_connnect.TabIndex = 4;
            this.bt_connnect.Text = "开始监听";
            this.bt_connnect.UseVisualStyleBackColor = true;
            this.bt_connnect.Click += new System.EventHandler(this.bt_connnect_Click);
            // 
            // lb_port
            // 
            this.lb_port.AutoSize = true;
            this.lb_port.Location = new System.Drawing.Point(207, 28);
            this.lb_port.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.lb_port.Name = "lb_port";
            this.lb_port.Size = new System.Drawing.Size(38, 17);
            this.lb_port.TabIndex = 3;
            this.lb_port.Text = "Port:";
            // 
            // tb_port
            // 
            this.tb_port.Location = new System.Drawing.Point(261, 24);
            this.tb_port.Margin = new System.Windows.Forms.Padding(4);
            this.tb_port.Name = "tb_port";
            this.tb_port.Size = new System.Drawing.Size(132, 22);
            this.tb_port.TabIndex = 2;
            this.tb_port.Text = "8000";
            // 
            // lb_Ip
            // 
            this.lb_Ip.AutoSize = true;
            this.lb_Ip.Location = new System.Drawing.Point(20, 28);
            this.lb_Ip.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.lb_Ip.Name = "lb_Ip";
            this.lb_Ip.Size = new System.Drawing.Size(24, 17);
            this.lb_Ip.TabIndex = 1;
            this.lb_Ip.Text = "IP:";
            // 
            // tb_ip
            // 
            this.tb_ip.Location = new System.Drawing.Point(59, 24);
            this.tb_ip.Margin = new System.Windows.Forms.Padding(4);
            this.tb_ip.Name = "tb_ip";
            this.tb_ip.Size = new System.Drawing.Size(132, 22);
            this.tb_ip.TabIndex = 0;
            this.tb_ip.Text = "10.82.197.132";
            // 
            // txt_msg
            // 
            this.txt_msg.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.txt_msg.Location = new System.Drawing.Point(442, 16);
            this.txt_msg.Margin = new System.Windows.Forms.Padding(4);
            this.txt_msg.Multiline = true;
            this.txt_msg.Name = "txt_msg";
            this.txt_msg.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.txt_msg.Size = new System.Drawing.Size(880, 93);
            this.txt_msg.TabIndex = 2;
            // 
            // textBox1
            // 
            this.textBox1.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.textBox1.Font = new System.Drawing.Font("Microsoft Sans Serif", 10.8F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.textBox1.Location = new System.Drawing.Point(-2, 116);
            this.textBox1.Multiline = true;
            this.textBox1.Name = "textBox1";
            this.textBox1.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.textBox1.Size = new System.Drawing.Size(1324, 635);
            this.textBox1.TabIndex = 3;
            // 
            // serverForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(8F, 16F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(1322, 751);
            this.Controls.Add(this.textBox1);
            this.Controls.Add(this.txt_msg);
            this.Controls.Add(this.panel1);
            this.Margin = new System.Windows.Forms.Padding(4);
            this.Name = "serverForm";
            this.Text = "SocketForm";
            this.Load += new System.EventHandler(this.Form1_Load);
            this.panel1.ResumeLayout(false);
            this.panel1.PerformLayout();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Panel panel1;
        private System.Windows.Forms.Button bt_connnect;
        private System.Windows.Forms.Label lb_port;
        private System.Windows.Forms.TextBox tb_port;
        private System.Windows.Forms.Label lb_Ip;
        private System.Windows.Forms.TextBox tb_ip;
        private System.Windows.Forms.TextBox txt_msg;
        private System.Windows.Forms.Button bt_send;
        private System.Windows.Forms.TextBox textBox1;
        private System.Windows.Forms.Button button1;
    }
}