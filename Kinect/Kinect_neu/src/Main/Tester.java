package Main;

import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import KinectPV2.*;
import Util.ImageUtils;
import processing.core.*;

public class Tester extends PApplet {

	private static KinectPV2 kinect;

	private static int[] depthZero;

	private static PImage depthToColorImg;

	private static JFrame imageFrame = new JFrame();
	private static JLabel imageLabel = new JLabel();

	private static Tester tester = new Tester();

	public static void main(String[] args) {
		
		// Setup
		kinect = new KinectPV2(tester);
		kinect.enableDepthImg(true);
		kinect.enableColorImg(true);
		kinect.activateRawDepth(true);
		kinect.activateRawColor(true);
		kinect.enablePointCloud(true);
		kinect.init();

		imageFrame.setLayout(new FlowLayout());
		imageFrame.setTitle("Image");
		imageFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		imageFrame.setVisible(true);
		imageFrame.add(imageLabel);
		imageFrame.setBounds(0, 10, 1100, 1000);
		
		// Code
		mapColorToDepth();
		System.exit(0);
		
	}
	
	public static Point getCoordsForColorPixel(int x, int y) {
		Point result;
		float[] mapDCT = kinect.getMapDepthToColor();
		
		
		int i = x;
		int j = y;
		
		
		
		
		return new Point(0,0);
	}

	public static BufferedImage mapColorToDepth() {
		try {
			float[] mapDCT = kinect.getMapDepthToColor(); // 0 -> returns X-Coord; 1 -> returns Y-Coord of Color Pixel in 1920x1080-Image for given DepthPixel
			int[] colorRaw = kinect.getRawColor(); // 1920 * 1080

			depthToColorImg = tester.createImage(512, 424, PImage.RGB); // Creates dtc-Image
			
			// Waits for the Sensor
			while (true) {
				mapDCT = kinect.getMapDepthToColor();
				int fails = 0;
				for (float f : mapDCT) {
					if (f == 0) {
						fails++;
					}
				}
				if (fails == 0) {
					break;
				}
			}


			
			int count = 0; // from 0 to 217088 ( 512 * 424 )
			for (int i = 0; i < 512; i++) {
				for (int j = 0; j < 424; j++) {

					float valX = mapDCT[count * 2 + 0]; // Each first value = X
					float valY = mapDCT[count * 2 + 1]; // Each second value = Y
					
					int valXDepth = (int) ((valX / 1920.0) * 512.0); // Gets valX for 512*424 Images ( like DepthImages )
					int valYDepth = (int) ((valY / 1080.0) * 424.0); // Gets valY for 512*424 Images ( like DepthImages )

					int valXColor = (int) (valX); // Gets valX for 1920*1080 Images ( like ColorImages )
					int valYColor = (int) (valY); // Gets valX for 1920*1080 Images ( like ColorImages )
					
					if (valXDepth >= 0 && valXDepth < 512 && valYDepth >= 0 && valYDepth < 424 && valXColor >= 0 && valXColor < 1920 && valYColor >= 0 && valYColor < 1080) { // Filters "valX = -Infinity"-Errors
						int colorPixel = colorRaw[valYColor * 1920 + valXColor];
						depthToColorImg.pixels[valYDepth * 512 + valXDepth] = colorPixel;
						
					}
					count++;
				}
			}
			depthToColorImg.updatePixels();
			
			BufferedImage dtc = ImageUtils.resize((BufferedImage) depthToColorImg.getImage(), 1920, 1080);
			BufferedImage color = (BufferedImage) kinect.getColorImage().getImage();
			
			ImageUtils.saveImage(dtc, "C://Users//vince//Desktop//dtc.png");
			ImageUtils.saveImage(color, "C://Users//vince//Desktop//color.png");

			return dtc;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void updateImage(BufferedImage i) {
		ImageIcon icon = new ImageIcon(i);
		imageLabel.setIcon(icon);
	}
}
