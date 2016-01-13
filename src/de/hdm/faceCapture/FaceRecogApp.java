package de.hdm.faceCapture;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.opencv.core.Core;
import org.opencv.core.MatOfRect;
import org.opencv.videoio.VideoCapture;

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

    private static String trainingDir = "faces/";
    private JLabel imageLabel = new JLabel();
    private FacePicture webcamImage = new FacePicture();
    private JCheckBox displayNames = new JCheckBox("Names", false);
    private JCheckBox movingPics = new JCheckBox("Moving Pics");
    private JFileChooser importFileChooser = null;

    private Map<String, MovingPicture> movingPictures = new HashMap<String, MovingPicture>();

    private boolean running = false;
    private VideoCapture capture = null;
    private int captureDevice;

    // Main Methode
    public static void main(String[] args) {
        if (args.length > 0) {
            trainingDir = args[0];
        }
        FaceRecogApp app = new FaceRecogApp("Face Recognizer");
        app.initGUI();
        app.start();
    }

    public FaceRecogApp(String windowName) {
        super(windowName);
        // capture device may be provided as a system property (e.g.,
        // -DCaptureDevive=1)
        setCaptureDevice(Integer.parseInt(System.getProperty("CaptureDevice", "0")));
    }

    // GUI: Initialisierung
    private void initGUI() {

        setLayout(new BorderLayout());
        add(imageLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(createTakePictureButton());
        buttonPanel.add(createImportPictureButton());
        buttonPanel.add(displayNames);
        buttonPanel.add(movingPics);
        buttonPanel.add(createDeviceRadioButtons());

        add(buttonPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                stop();
                capture.release();
                setVisible(false);
                System.exit(DISPOSE_ON_CLOSE);
                dispose();
            }

        });
        // initialize face recognition with pictures on training dir
        FaceRecog.retrain(trainingDir);

        // capture.set(Videoio.CV_CAP_PROP_FRAME_WIDTH, 640);
        // capture.set(Videoio.CV_CAP_PROP_FRAME_HEIGHT, 480);
        if (webcamImage.capture(capture)) {
            webcamImage.drawToLabel(imageLabel);
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                pack();
                setLocationRelativeTo(null);
                setVisible(true);
            }
        });
    }

    // GUI: Take picture Button
    private JButton createTakePictureButton() {
        JButton pictureButton = new JButton("Take Picture");
        pictureButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                stop();
                FacePicture fp = new FacePicture(webcamImage);
                new AddFaceDialog(fp);
                start();
            }
        });
        return pictureButton;
    }

    private JButton createImportPictureButton() {
        JButton pictureButton = new JButton("Import Picture");
        pictureButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                stop();
                // select picture file to be imported
                if (importFileChooser == null) {
                    importFileChooser = new JFileChooser();
                    FileNameExtensionFilter filter = new FileNameExtensionFilter("Images", "jpg", "jpeg", "gif", "png");
                    importFileChooser.setAcceptAllFileFilterUsed(false);
                    importFileChooser.setFileFilter(filter);
                    importFileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + "/Pictures"));
                }
                int returnVal = importFileChooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {

                    FacePicture fp = new FacePicture();
                    fp.importFrom(importFileChooser.getSelectedFile());

                    // just for checks write modified picture to picture dir
                    // fp.writeToPathname(createPictureFilePathName(pictureDir));

                    new AddFaceDialog(fp);
                }
                start();
            }
        });
        pictureButton.setTransferHandler(new PictureFileTransferHandler());
        return pictureButton;
    }

    private JPanel createDeviceRadioButtons() {
        JPanel buttonPanel = new JPanel();
        ButtonGroup bg = new ButtonGroup();
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                switch (event.getActionCommand()) {
                case "Camera 0":
                    setCaptureDevice(0);
                    break;
                case "Camera 1":
                    setCaptureDevice(1);
                    break;
                case "Camera 2":
                    setCaptureDevice(2);
                    break;
                default:
                    System.out.println("unknown capture device");
                }
            }
        };
        String deviceName = "";
        JRadioButton rb = null;
        for (int i = 0; i < 2; i++) {
            deviceName = "Camera " + i;
            rb = new JRadioButton(deviceName);
            rb.setActionCommand(deviceName);
            rb.addActionListener(al);
            bg.add(rb);
            if (captureDevice == i) {
                rb.setSelected(true);
            }
            buttonPanel.add(rb);
        }
        return buttonPanel;
    }

    private void setCaptureDevice(int cd) {
        stop();
        if (capture != null && capture instanceof VideoCapture) {
            capture.release();
        }
        captureDevice = cd;
        capture = new VideoCapture(captureDevice);
        start();
    }

    private void stop() {
        running = false;
    }

    private void start() {
        new FacRecogThread().start();
    }

    private class FacRecogThread extends Thread {
        private MatOfRect faceDetections = null;
        private FacePicture[] faces = null;
        private Prediction[] predictions = new Prediction[0];

        public void run() {
            running = true;
            while (running) {
                try {
                    if (webcamImage.capture(capture)) {
                        sleep(100);
                        faceDetections = webcamImage.detectFaces();
                        webcamImage.drawRectangles(faceDetections);
                        if (!faceDetections.empty() && displayNames.isSelected() || movingPics.isSelected()) {
                            faces = webcamImage.isolateFaces(faceDetections);
                            predictions = FaceRecog.recognizeFaces(faces);
                            if (displayNames.isSelected()) {
                                webcamImage.displayNames(predictions, faceDetections);
                            }
                            if (movingPics.isSelected()) {
                                for (Prediction pred : predictions) {
                                    if (movingPictures.containsKey(pred.name)) {
                                        movingPictures.get(pred.name).reset();
                                    } else {
                                        movingPictures.put(pred.name, new MovingPicture(pred.name));
                                    }
                                }
                            }
                        }
                        webcamImage.drawToLabel(imageLabel);
                        repaint();
                    } else {
                        System.out.println(" -- Frame not captured -- Break!");
                        break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
