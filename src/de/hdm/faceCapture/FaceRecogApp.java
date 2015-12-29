package de.hdm.faceCapture;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

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

/**
 * Some elements of this code can be found at:
 * https://blog.openshift.com/day-12-opencv-face-detection-for-java-developers/
 * 
 *
 */

public class FaceRecogApp extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private String cascadePath = "resources/cascades/lbpcascades/lbpcascade_frontalface.xml";

    private JLabel imageLabel = new JLabel();
    private CascadeClassifier faceDetector = new CascadeClassifier(cascadePath);

    private FaceRecog faceR = new FaceRecog();

    private Mat lastDetectedFace = null;
    private File pictureDir = null;
    private JButton selectDirectory = new JButton("Choose image directory");
    private JCheckBox check = new JCheckBox("Face Recognition enabled", false);
    
    private boolean running = false;
    private VideoCapture capture = null;

    // Main Methode
    public static void main(String[] args) {
        FaceRecogApp app = new FaceRecogApp();
        app.initGUI();
        app.runMainLoop(args);
    }

    // Image Scaling
    private Mat scaleImage(Mat input, Size size) {
        Mat snapshotScaled = new Mat();
        Imgproc.resize(input, snapshotScaled, size);
        return snapshotScaled;
    }

    // Image Noise Reduction via Blur
    private Mat blurImage(Mat input, Size size) {
        Mat snapshotBlurred = new Mat();
        Imgproc.blur(input, snapshotBlurred, size);
        return snapshotBlurred;
    }

    // Image Noise Reduction via Non Local Means Denoising [Optionale
    // Optimierung]
    /*
     * To be implemented / Nice to have
     */

    // Image Normalization [Optionale Optimierung]
    /*
     * To be implemented / Nice to have
     */

    // GUI: Initialisierung
    private void initGUI() {

        setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
        // Einzelne Bestandteile einladen
        getContentPane().add(imageLabel);
        getContentPane().add(createNameInputButton());
        getContentPane().add(createTakePictureButton());
        getContentPane().add(check);

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                running = false;                
                capture.release();
                setVisible(false);
                System.exit(DISPOSE_ON_CLOSE);
                dispose();
            }

        });
        
        center();
    }
    
    private void center() {
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // GUI: Nameseingabe
    private JButton createNameInputButton() {
        selectDirectory.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setCurrentDirectory(pictureDir == null ? new File("media/") : pictureDir);
                int returnVal = chooser.showOpenDialog(null);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    setPictureDir(chooser.getSelectedFile());
                }
            }
        });
        return selectDirectory;
    }

    private void setPictureDir(File dir) {
        pictureDir = dir;
        selectDirectory.setText("Choose image directory (" + dir.getName() + ")");

    }

    // GUI: Take picture Button
    private JButton createTakePictureButton() {
        JButton pictureButton = new JButton("Take Picture");
        pictureButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (pictureDir == null) {
                    JOptionPane.showMessageDialog(null, "Please choose image directory before taking a picture");
                    return;
                }
                // count all files in picture dir and save a new face image
                String imageName = "image-" + pictureDir.listFiles().length + ".png";
                Imgcodecs.imwrite(pictureDir.getPath() + "/" + imageName, lastDetectedFace);
                // System status message
                System.out.println("Snapshot: " + imageName + " taken");
                // reinitialize FaceRecognition Training
                faceR.initFaceRec();
            }
        });
        return pictureButton;
    }

    // Image Processing Main Loop
    private void runMainLoop(String[] args) {
        // sneak in faceRec training ;)
        faceR.initFaceRec();
        running = true;

        ImageProcessor imageProcessor = new ImageProcessor();
        Mat webcamMatImage = new Mat();
        Image tempImage;
        capture = new VideoCapture(0);
        capture.set(Videoio.CV_CAP_PROP_FRAME_WIDTH, 640);
        capture.set(Videoio.CV_CAP_PROP_FRAME_HEIGHT, 480);

        if (capture.isOpened()) {
            while (running) {
                capture.read(webcamMatImage);
                if (!webcamMatImage.empty()) {
                    detectFacesInImage(webcamMatImage);
                    tempImage = imageProcessor.toBufferedImage(webcamMatImage);
                    ImageIcon imageIcon = new ImageIcon(tempImage, "Captured video");
                    imageLabel.setIcon(imageIcon);
                    center();
                } else {
                    System.out.println(" -- Frame not captured -- Break!");
                    break;
                }
            }
        } else {
            System.out.println("Couldn't open capture.");
        }

    }

    private Mat[] detectFacesInImage(Mat image) {
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(image, faceDetections, 1.1, 7, 0, new Size(50, 50), new Size());

        // Draw a bounding box around each face.
        Rect[] detections = faceDetections.toArray();
        Mat[] faces = new Mat[detections.length];

        for (int i = 0; i < detections.length; i++) {
            Rect rect = detections[i];
            // Display Rect around face
            Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0));

            // Crop, blur, resize and gray Image
            Mat cropImage = new Mat(image, rect);
            Mat blurImage = blurImage(cropImage, new Size(3.0, 3.0));
            Mat scaleImage = scaleImage(blurImage, new Size(75, 75));
            Mat grayImage = new Mat();
            Imgproc.cvtColor(scaleImage, grayImage, Imgproc.COLOR_BGR2GRAY);

            faces[i] = grayImage;
        }
        if (faces.length == 1) {
            lastDetectedFace = faces[0];
            if (check.isSelected()) {
                Imgcodecs.imwrite("media/temp.png", lastDetectedFace);
                String name = faceR.startRecognition("media/temp.png");
                if (name != null) {
                    Imgproc.putText(image, "Name:" + name, new Point(20, 50), 2, 1.2, new Scalar(0, 0, 255));
                }
            }
        }
        return faces;
    }

}
