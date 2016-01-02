package de.hdm.faceCapture;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.opencv.core.Core;
import org.opencv.core.MatOfRect;
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


    private JLabel imageLabel = new JLabel();
    
    private FacePicture webcamImage = new FacePicture();
    
    private JCheckBox check = new JCheckBox("Face Recognition enabled", false);
    private JFileChooser importFileChooser = null;

    private boolean running = false;
    private VideoCapture capture = null;

    // Main Methode
    public static void main(String[] args) {
        FaceRecogApp app = new FaceRecogApp();
        app.initGUI();
        app.runMainLoop(args);
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
        add(imageLabel);
        imageLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(createTakePictureButton());
        add(createImportPictureButton());
        add(check);
        check.setAlignmentX(CENTER_ALIGNMENT);

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

    // GUI: Take picture Button
    private JButton createTakePictureButton() {
        JButton pictureButton = new JButton("Take Picture");
        pictureButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                new AddFaceDialog(webcamImage);
            }
        });
        pictureButton.setAlignmentX(CENTER_ALIGNMENT);
        return pictureButton;
    }

    private JButton createImportPictureButton() {
        JButton pictureButton = new JButton("Import Picture");
        pictureButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // select picture file to be imported
                if (importFileChooser == null) {
                    importFileChooser = new JFileChooser();
                    FileNameExtensionFilter filter = new FileNameExtensionFilter("Images", "jpg", "jpeg", "gif", "png");
                    importFileChooser.setAcceptAllFileFilterUsed(false);
                    importFileChooser.setFileFilter(filter);
                    importFileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + "/Pictures"));
                }
                int returnVal = importFileChooser.showOpenDialog(null);
                if (returnVal != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                FacePicture fp = new FacePicture();
                fp.importFrom(importFileChooser.getSelectedFile());

                // just for checks write modified picture to picture dir
                //fp.writeToPathname(createPictureFilePathName(pictureDir));

                new AddFaceDialog(fp);
            }
        });
        pictureButton.setAlignmentX(CENTER_ALIGNMENT);
        return pictureButton;
    }

    // Image Processing Main Loop
    private void runMainLoop(String[] args) {
        // sneak in faceRec training ;)
        FaceRecog.initFaceRec();
        running = true;

        capture = new VideoCapture(0);
        capture.set(Videoio.CV_CAP_PROP_FRAME_WIDTH, 640);
        capture.set(Videoio.CV_CAP_PROP_FRAME_HEIGHT, 480);
        webcamImage.capture(capture);
        webcamImage.drawToLabel(imageLabel);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                center();
            }
        });

        MatOfRect faceDetections = null;
        FacePicture[] faces = null;
        String names[] = null;
        while (running) {
            if (webcamImage.capture(capture)) {
                faceDetections = webcamImage.detectFaces();
                webcamImage.drawRectangles(faceDetections);
                if (!faceDetections.empty()) {
                    if (check.isSelected()) {
                        faces = webcamImage.isolateFaces(faceDetections);
                        names = FaceRecog.recognizeFaces(faces);
                        webcamImage.putTexts(names);
                    }
                }
                webcamImage.drawToLabel(imageLabel);
                repaint();
            } else {
                System.out.println(" -- Frame not captured -- Break!");
                break;
            }
        }
    }

}
