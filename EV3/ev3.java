import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.remote.nxt.BTConnector;
import lejos.remote.nxt.NXTConnection;

public class ev3 {

	int angle;
	int distance = -1;
	boolean stop = true;

	String BTMessage = "Nix";
	String WlanMessage = "Nix";

	boolean BTConnected = true; //false
	boolean WlanConnected = false;

	long speed = Long.MAX_VALUE;

	ServerSocket server;
	Socket client;

	public ev3() {

		try {

			NXTRegulatedMotor mA = Motor.A;
			NXTRegulatedMotor mB = Motor.B;
			NXTRegulatedMotor mC = Motor.C;
			NXTRegulatedMotor mD = Motor.D;

			mA.setSpeed(speed);
			mB.setSpeed(speed);
			mC.setSpeed(speed);
			mD.setSpeed(speed);

			bluetoothThread bT = new bluetoothThread("BT");
			wlanThread wT = new wlanThread("WLAN", 1337);
			bT.start();
			wT.start();
			while (true) {
				
				if (WlanConnected && BTConnected) {
					
					//Get Messages
					WlanMessage = wT.Command;
					BTMessage = bT.Command;
					
					//Check if BT tells the robot to stop or drive
					if (BTMessage.toLowerCase().equals("go")) {
						stop = false;
					} else if (BTMessage.toLowerCase().equals("stop")) {
						stop = true;
					} else if (!BTMessage.equals("Nix")) {
						System.out.println(BTMessage);
					}
						
					
					//Get Data out of the Wlan-Message
					if (WlanMessage.contains("#")) {
						int i = WlanMessage.indexOf('#');
						WlanMessage = WlanMessage.replace("#", "");
						angle = Integer.valueOf(WlanMessage.substring(0, i));
						distance = Integer.valueOf(WlanMessage.substring(i, WlanMessage.length()));
						System.out.println("Angle: " + angle + "   Distance: " + distance);
					} else {
						System.out.println(WlanMessage);
					}
					
					if (angle < 90) {
						mB.rotateTo(angle * 4, true);
						mC.rotateTo(-angle * 4, false);
					}
					
					if (distance > 80) {
						mA.backward();
						mD.backward();
					} else {
						mA.stop();
						mD.stop();
					}
					

				} else {
					System.out.println("BT-State: " + BTConnected + ", Wlan-State; " + WlanConnected);
				}
				
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class bluetoothThread extends Thread {

		String name;
		String Command = "Nix";

		bluetoothThread(String name) {
			this.name = name;
		}

		@Override
		public void run() {

			System.out.println("Thread " + name + " gestartet");

			BTConnector connector = new BTConnector();

			System.out.println("Auf B-Signal warten");

			NXTConnection conn = connector.waitForConnection(0, NXTConnection.RAW);
			InputStream is = conn.openInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is), 1);

			String message = "";

			BTConnected = true;
			
			System.out.println("B verbunden");

			while (true) {

				message = "";
				try {
					message = br.readLine();
					if (message != null && !message.equals("Nix")) {
						this.Command = message;
					}
				} catch (IOException e) {
					e.printStackTrace(System.out);
				}

			}
		}
	}

	public class wlanThread extends Thread {

		String name;
		String Command = "Nix";
		int port;

		wlanThread(String name, int port) {
			this.name = name;
			this.port = port;
		}

		@Override
		public void run() {

			try {
				server = new ServerSocket(port);

				System.out.println("Auf W-Signal warten");

				client = server.accept();

				DataInputStream stream = new DataInputStream(client.getInputStream());

				String message = "";

				WlanConnected = true;
				
				System.out.println("W verbunden");

				while (true) {

					message = "";
					message = stream.readUTF();
					if (message != null && !message.equals("Nix")) {
						this.Command = message;
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		new ev3();
	}

}