using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.IO.Ports;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.Threading;

namespace WindowsFormsApp1
{
    public partial class Form1 : Form
    {
        private SerialPort serialPort = new SerialPort();

        public Form1()
        {
            InitializeComponent();
        }

        //시리얼 데이터 수신 이벤트 처리 함수
        private void SerialPort_DataReceived(Object sender, SerialDataReceivedEventArgs e)
        {
            String recvData = this.serialPort.ReadLine();
            Console.WriteLine(recvData);

            //LED 신호전달 문자열
            if (recvData.StartsWith("LED:"))
            {
                //스레드 생성 실행
                Invoke(new Action(() =>
                {
                    this.textBox1.AppendText(recvData+"\r\n");
                   
                }));
                Thread.Sleep(10);
            }
            //조도센서 전달 문자열
            if (recvData.StartsWith("SUN:"))
            {
                //스레드 생성 실행
                Invoke(new Action(() =>
                {
                    this.textBox2.Text = recvData.Replace("SUN:","");    
                }));
                Thread.Sleep(10);
            }
            //온도센서 전달 문자열
            if (recvData.StartsWith("TEMP:"))
            {
                //스레드 생성 실행
                Invoke(new Action(() =>
                {
                    this.textBox3.Text = recvData.Replace("TEMP:", "");
                }));
                Thread.Sleep(10);
            }
            //초음파센서 전달 문자열
            if (recvData.StartsWith("DIS:"))
            {
                //스레드 생성 실행
                Invoke(new Action(() =>
                {
                    this.textBox4.Text = recvData.Replace("DIS:", "");
                }));
                Thread.Sleep(10);
            }


        }

        private void button1_MouseClick(object sender, MouseEventArgs e)
        {
            Console.WriteLine("연결 Btn Clicked..");
            String port_number = null;

            try
            {
                if(this.comboBox1.SelectedIndex > -1)
                {
                    port_number = this.comboBox1.Items[this.comboBox1.SelectedIndex].ToString();
                    Console.WriteLine("Selected Port : " + port_number);

                    //SERIAL PORT 연결
                    this.serialPort.PortName = port_number;
                    this.serialPort.BaudRate = 9600;
                    this.serialPort.DataBits = 8;
                    this.serialPort.StopBits = System.IO.Ports.StopBits.One;
                    this.serialPort.Parity = System.IO.Ports.Parity.None;
                    this.serialPort.Open();
                    Console.WriteLine("CONNECTION SUCCESS... " + this.serialPort);
                    this.serialPort.DataReceived += new SerialDataReceivedEventHandler(SerialPort_DataReceived);
                }

            }
            catch(Exception ex)
            {
                Console.Write("CONNECTION ERROR : " + ex);
                this.serialPort.Close();
            }

        }

        private void btn_on_Click(object sender, EventArgs e)
        {
            Console.WriteLine("LED ON BTN CLICKED...");
            serialPort.Write("1");
        }

        private void btn_off_Click(object sender, EventArgs e)
        {
            Console.WriteLine("LED OFF BTN CLICKED...");
            serialPort.Write("0");
        }

        private void groupBox3_Enter(object sender, EventArgs e)
        {

        }
    }
}
