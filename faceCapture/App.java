package faceCapture;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;


public class App {
	
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); 
	}
	
	private String windowName;
	
	private JLabel imageLabel;
	private CascadeClassifier faceDetector;
	
	public fileReader fr = new fileReader();
	public FaceRecog faceR = new FaceRecog();

	
	
	public Mat snapshot;
	public String pictureName;	
	
	
	// Auto Increment Image Counter
	public int imageCounter;
	
	public int personId;
	
	// Image Cropping Variables
	public int borderx1;
	public int borderx2;
	public int bordery1;
	public int bordery2;
	
	boolean train = true;
	
	// Main Methode
	public static void main(String[] args) {
		App app = new App();
		app.initGUI();
		app.loadCascade();
		app.runMainLoop(args);
	}
	
	// Metods for Image prep
	// Image Cropping 
	private Mat cropImage(Mat input) {
		Rect roi = new Rect(borderx1, bordery1, borderx2, bordery2);
		//new Rect(borderx1 + 20, bordery1 + 35, borderx2 - 30, bordery2 - 15);
		Mat snapshotCropped = new Mat(input, roi);
		return snapshotCropped;
	}
	
	// Image Scaling 
	private Mat scaleImage(Mat input) {
		Mat snapshotScaled = new Mat();
		Imgproc.resize(input, snapshotScaled, new Size(75, 75));
		return snapshotScaled;
	}
	
	// Image Noise Reduction via Blur
	private Mat blurImage(Mat input) {
		Mat snapshotBlurred = new Mat();
		Imgproc.blur(input, snapshotBlurred, new Size(3.0, 3.0));
		return snapshotBlurred;
	}
	
	// Image Noise Reduction via Non Local Means Denoising [Optionale Optimierung]
	/*
	 * To be implemented / Nice to have
	 */
	
	// Image Normalization [Optionale Optimierung]
	/*
	 * To be implemented / Nice to have
	 */
	
	
	

	// FaceDetection Cascade Classifier laden und faceDetector erstellen
	private void loadCascade() {
		String cascadePath = "cascades/lbpcascades/lbpcascade_frontalface.xml";
	    faceDetector = new CascadeClassifier(cascadePath);
	}

	// GUI: Initialisierung
	private void initGUI() {
		JFrame frame = createJFrame(windowName);
		frame.setPreferredSize(new Dimension(640, 640));
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	// GUI: Grundgerüst
	private JFrame createJFrame(String windowName) {
		JFrame frame = new JFrame(windowName);
		frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.PAGE_AXIS));	
		// Einzelne Bestandteile einladen
		setupImage(frame);
		setupNameInput(frame);
		setupButton(frame);
		setupChangeMode(frame);

		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		return frame;
	}
	
	// GUI: Webcam Output
	private void setupImage(JFrame frame) {
		imageLabel = new JLabel();
		imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		imageLabel.setAlignmentY(Component.TOP_ALIGNMENT);
		frame.add(imageLabel);
	}
	
	// GUI: Nameseingabe
	private void setupNameInput(JFrame frame) {
		JPanel contentPanel = new JPanel();
		JLabel instructionText = new JLabel();
		instructionText.setText("Vor- und Nachname eintragen und mit Enter bestätigen");
		instructionText.setAlignmentX(Component.CENTER_ALIGNMENT);
		final JTextField nameField = new JTextField();
		
		nameField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				imageCounter = 0;
				String nameInput = nameField.getText();
				personId = fr.getImageId() + 1;
				pictureName = nameInput;
				fr.addEntry(personId + "-" + nameInput);
				System.out.println(personId + "-" + nameInput + "added to list");
			}
		});
		
		nameField.setAlignmentX(Component.CENTER_ALIGNMENT);
		frame.getContentPane().add(instructionText);
		frame.getContentPane().add(nameField);
		frame.add(contentPanel);
	}
	
	// GUI: Change mode
	private void setupChangeMode(JFrame frame) {
		JCheckBox check = new JCheckBox("Face Recognition enabled", false);

		check.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (train == true) {
					train = false;
					System.out.println("Mode changed to recognition");			
				} else {
					train = true;
					System.out.println("Mode changed to training");
				}
			}
		});
		
		frame.add(check);
	}
	
	// GUI: Take picture Button
	private void setupButton(JFrame frame) {
		JButton pictureButton = new JButton("Take Picture");
		// Action Listener dem Button hinzufügen
		pictureButton.addActionListener(new ActionListener() {
			// Wenn Button gedrückt wird, wird das Originalbild geladen
			 public void actionPerformed(ActionEvent event) {
				imageCounter++;
				// Crop Image
				Mat cropImage = cropImage(snapshot);
				// Blur Image
				Mat blurImage = blurImage(cropImage);
				// Resize Image
				Mat scaleImage = scaleImage(blurImage);
				// Cvt to Grayscale
				Mat grayImage = new Mat();
				Imgproc.cvtColor(scaleImage, grayImage, Imgproc.COLOR_BGR2GRAY);
				
				// Border Control status message 
				System.out.println(borderx1 + ", " + bordery1 + ", " + borderx2 + ", " + bordery2);
				
				// String anpassen für korrekten Dateinamen
				pictureName.toLowerCase();
				
				String[] namen = pictureName.split(" ");
				String vorname = namen[0];
				String nachname = namen[1];
				
				// save picture
				Imgcodecs.imwrite("media/test/" + personId + "-" + vorname + "_" + nachname + "_" + imageCounter + ".png", grayImage);
				
				// System status message
				System.out.println("Snapshot: " + personId + vorname + "_" + nachname + "_" + imageCounter + ".png" + "taken");
				
				// reinitialize FaceRecognition Training
				faceR.initFaceRec();
			}
		});
		// Setzt den Button in die Mitte
		pictureButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		pictureButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		// Fügt den Button der frame hinzu
		frame.add(pictureButton);
	}
	
	
	// Image Processing Main Loop
	private void runMainLoop(String[] args) {
		// sneak in faceRec training ;)
		faceR.initFaceRec();
		ImageProcessor imageProcessor = new ImageProcessor();
		Mat webcamMatImage = new Mat();  
		Image tempImage;  
		VideoCapture capture = new VideoCapture(0);
		capture.set(Videoio.CV_CAP_PROP_FRAME_WIDTH,640);
		capture.set(Videoio.CV_CAP_PROP_FRAME_HEIGHT,480);

		if( capture.isOpened()){  
			while (true){  
				capture.read(webcamMatImage);  
				if( !webcamMatImage.empty() ){  
					snapshot = webcamMatImage; 
					detectAndDrawFace(webcamMatImage);
					tempImage = imageProcessor.toBufferedImage(webcamMatImage);
					ImageIcon imageIcon = new ImageIcon(tempImage, "Captured video");
					imageLabel.setIcon(imageIcon);
				}  
				else{  
					System.out.println(" -- Frame not captured -- Break!"); 
					break;  
				}
				try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
			}  
		}
		else{
			System.out.println("Couldn't open capture.");
		}
		
	}

	// Face Detection
	private void detectAndDrawFace(Mat image) {
		// filereader
		fileReader tempFr = new fileReader();
		int result = 0;
	    MatOfRect faceDetections = new MatOfRect();
	    faceDetector.detectMultiScale(image, faceDetections); //, 1.1, 7,0,new Size(200,200),new Size());
	    // Draw a bounding box around each face.
	    for (Rect rect : faceDetections.toArray()) {
	    	// Display Rect around face
	    	if(train == false) {
	    		Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
	    		
	    		// Crop Image
				Mat cropImage = cropImage(snapshot);
				// Blur Image
				Mat blurImage = blurImage(cropImage);
				// Resize Image
				Mat scaleImage = scaleImage(blurImage);
				// Cvt to Grayscale
				Mat grayImage = new Mat();
				Imgproc.cvtColor(scaleImage, grayImage, Imgproc.COLOR_BGR2GRAY);
				
				Imgcodecs.imwrite("media/temp.png", grayImage);
				
				result = faceR.startRecognition("media/temp.png");
				System.out.println(result);
				if(result != 0) {
					String personName = tempFr.getName(result);
					Imgproc.putText(image, "Name:" + personName, new Point(20, 50), 2, 1.2, new Scalar(0, 0, 255));
				}
	    	}
	        borderx1 = rect.x;
	        borderx2 = rect.width;
	        bordery1 = rect.y;
	        bordery2 = rect.height;
	        // Display entered Name
	        if(train == true) {
	        	if(pictureName != null) {
	        		Imgproc.putText(image, "Name:" + pictureName, new Point(20, 50), 2, 1.2, new Scalar(255, 0, 0));
	        	}
	        }
	    }
	}

}
