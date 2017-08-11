package com.example.jon.joystick;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.math.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends AppCompatActivity {
    TextView angle_right, angle_left, strength_right;
    boolean connected = false;
    String str=null;
    String name;
    Integer x_str, y_str;
    byte[] send_data = new byte[1024];
    byte[] receiveData = new byte[1024];
    DatagramSocket client_socket;
    InetAddress IPAddress;
    Button bt1, bt2;
    EditText serverIp, nameText;
    Integer angleInt, angleInt2;
    Integer strengthInt;
    //Thread receiveThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        angle_left = (TextView)(findViewById(R.id.textView_angle_left));
        angle_right = (TextView)(findViewById(R.id.textView_angle_right));
        strength_right = (TextView)(findViewById(R.id.textView_strength_right));
        bt1 = (Button) findViewById(R.id.button);
        bt2 = (Button) findViewById(R.id.button2);
        bt2.setEnabled(false);
        serverIp = (EditText)findViewById(R.id.editText);
        nameText = (EditText)findViewById(R.id.editText2);





        JoystickView joystick2 = (JoystickView) findViewById(R.id.joystickView2);


        joystick2.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                angleInt2 = ((Integer)angle);


                angle_left.setText(angleInt2.toString());

                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try  {
                            send("Turret "+(angleInt2).toString());

                        } catch (Exception
                                e) {
                            e.printStackTrace();

                        }
                    }
                });
                try {
                    thread.start();
                } catch (Exception ex) {
                    ex.printStackTrace();

                }

            }
        });


        JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                angleInt = ((Integer)angle);

                strengthInt = ((Integer)strength);
                angle_right.setText(angleInt.toString());
                strength_right.setText(strengthInt.toString());
                x_str = (int)(strengthInt/10 * Math.cos(Math.toRadians(angleInt)));
                y_str = (int)(strengthInt/(-10) * Math.sin(Math.toRadians(angleInt)));

                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try  {
                            send("Body " + x_str.toString() + " " + y_str.toString()+ " " + (angleInt).toString());

                        } catch (Exception
                                e) {
                            e.printStackTrace();

                        }
                    }
                });
                try {
                    thread.start();
                } catch (Exception ex) {
                    ex.printStackTrace();

                }

            }
        });
        bt1.setOnClickListener(new View.OnClickListener(){    //connect button
            public void onClick(View v) {

                name = nameText.getText().toString();
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try  {
                            client_connect(serverIp.getText().toString());

                        } catch (Exception e) {
                            e.printStackTrace();
                         //   editText2.setText("bad address");


                        }
                    }
                });

                try {
                    thread.start();
                    Thread.currentThread().sleep(1000);

                } catch (Exception e) {
                    e.printStackTrace();

                }
                Thread receiveThread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        while(client_socket.isClosed() == false) {
                            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                            try{
                                do {
                                    receiveData = new byte[1024];
                                    client_socket.receive(receivePacket);
                                    final String temp = new String(receivePacket.getData()).substring(0,20);
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            strength_right.setText(temp);
                                        }
                                    });

                                    if (new String(receivePacket.getData()).substring(0,4).equals("reco")) {
                                        final String newIp = getIp(new String(receivePacket.getData()).substring(5, 25));
                                        IPAddress = InetAddress.getByName(newIp);
                                        MainActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                strength_right.setText(newIp);
                                            }
                                        });
                                    }

                                } while ( !(new String(receivePacket.getData()).substring(0,4).equals("exit")));
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        bt1.setEnabled(true);
                                        bt2.setEnabled(false);
                                        serverIp.setEnabled(true);
                                        nameText.setEnabled(true);
                                    }
                                });
                                client_socket.close();
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                            //if ((new String(receivePacket.getData())) == "exit") {
                            //}
                        }
                    }
                });
                if (connected) {
                    bt2.setEnabled(true);
                    bt1.setEnabled(false);
                    receiveThread.start();
                    serverIp.setEnabled(false);
                    nameText.setEnabled(false);
                }





            }

        });
        bt2.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try  {
                            send("FIRE");

                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                    }
                });
                try {
                    thread.start();

                } catch (Exception e) {
                    e.printStackTrace();

                }


            }

        });

    }

    public void onStop() {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    send("exit");

                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        });
        try {
            thread.start();
            Thread.currentThread().sleep(500);

        } catch (Exception e) {
            e.printStackTrace();

        }
        client_socket.close();
        super.onStop();

    }


    public void onDestroy() {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    send("exit");

                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        });
        try {
            thread.start();

        } catch (Exception e) {
            e.printStackTrace();

        }
        client_socket.close();
        super.onDestroy();

    }

    public void client_connect(String ipA) throws IOException {

        client_socket = new DatagramSocket(4000);
        IPAddress =  InetAddress.getByName(ipA);
        String temp = name + " connection successful";
        send_data = temp.getBytes();
        DatagramPacket send_packet = new DatagramPacket(send_data,temp.length(), IPAddress, 4000);
        client_socket.send(send_packet);
        client_socket.setSoTimeout(800);
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            client_socket.receive(receivePacket);
            connected = true;
        } catch (SocketTimeoutException e) {
            connected = false;
            client_socket.close();
        }


    }
    public String getIp(String badIp) {
        int count = 0;
        int endstring = 0;
        for (int i = 0; i< 16; i++) {
            if (badIp.charAt(i) == '.')
                count++;
            if (count == 4) {
                endstring = i;
                break;
            }
        }
        return badIp.substring(0,endstring);

    }

    public String send(String toSend) throws IOException {

        send_data = toSend.getBytes();
        DatagramPacket send_packet = new DatagramPacket(send_data,toSend.length(), IPAddress, 4000);
        client_socket.send(send_packet);

        byte[] receiveData = new byte[1024];
        //     DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        //   client_socket.receive(receivePacket);
        //    return new String(receivePacket.getData());
        return ("");

    }


}
