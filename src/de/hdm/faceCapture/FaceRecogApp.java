package de.hdm.faceCapture;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
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

    private JLabel imageLabel = new JLabel();
    private FacePicture webcamImage = new FacePicture();
    private JCheckBox check = new JCheckBox("Face Recognition enabled", false);
    private JFileChooser importFileChooser = null;

    private boolean running = false;
    private VideoCapture capture = null;

    private Map<String, Integer> nameFrequencies = new HashMap<String, Integer>();
    private String[] last100Names = new String[100];
    private int revolvingIndex = 0;

    // Main Methode
    public static void main(String[] args) {
        FaceRecogApp app = new FaceRecogApp("Face Recognizer");
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

    public FaceRecogApp(String windowName) {
        super(windowName);
    }

    // GUI: Initialisierung
    private void initGUI() {

        setLayout(new BorderLayout());
        // Einzelne Bestandteile einladen
        add(imageLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(createTakePictureButton());
        buttonPanel.add(createImportPictureButton());
        buttonPanel.add(check);

        add(buttonPanel, BorderLayout.SOUTH);

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
                // fp.writeToPathname(createPictureFilePathName(pictureDir));

                new AddFaceDialog(fp);
            }
        });
        return pictureButton;
    }

    /**
     * add an identified person's name to the data structures for computing
     * the most frequent name among the last 100 recognized persons
     * @param addedName
     */
    private void addName(String addedName) {
        String removedName = last100Names[revolvingIndex];
        
        if (removedName != "" && nameFrequencies.containsKey(removedName)) {
            nameFrequencies.put(removedName, nameFrequencies.get(removedName) - 1);
        }
        if (nameFrequencies.containsKey(addedName)) {
            nameFrequencies.put(addedName, nameFrequencies.get(addedName) + 1);
        } else {
            nameFrequencies.put(addedName, new Integer(1));
        }
        
        last100Names[revolvingIndex++] = addedName;
        revolvingIndex %= 100;
        
    }

    /**
     * get the most frequent recognized person
     * @return
     */
    private String mostFrequentName() {
        Integer maxValue = new Integer(0);
        String maxName = "";
        for (String name : nameFrequencies.keySet()) {
            if (nameFrequencies.get(name).compareTo(maxValue) > 0) {
                maxValue = nameFrequencies.get(name);
                maxName = name;
            }
        }
        return maxName;
    }

    // Image Processing Main Loop
    private void runMainLoop(String[] args) {
        // sneak in faceRec training ;)
        // FaceRecog.initFaceRec();
        FaceRecog.retrain();
        Arrays.fill(last100Names, "");
        
        capture = new VideoCapture(Integer.parseInt(System.getProperty("CaptureDevice", "0")));
        // capture.set(Videoio.CV_CAP_PROP_FRAME_WIDTH, 640);
        // capture.set(Videoio.CV_CAP_PROP_FRAME_HEIGHT, 480);
        if (webcamImage.capture(capture)) {
            webcamImage.drawToLabel(imageLabel);
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                center();
            }
        });

        MatOfRect faceDetections = null;
        FacePicture[] faces = null;
        String names[] = null;
        running = true;
        while (running) {
            if (webcamImage.capture(capture)) {
                faceDetections = webcamImage.detectFaces();
                webcamImage.drawRectangles(faceDetections);
                if (!faceDetections.empty() && check.isSelected()) {
                    faces = webcamImage.isolateFaces(faceDetections);
                    names = FaceRecog.recognizeFaces(faces);
                    addName(names[0]);   
                    webcamImage.putText(mostFrequentName());
                    //webcamImage.putTexts(names);        
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
