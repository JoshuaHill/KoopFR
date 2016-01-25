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
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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
    private boolean isLookAlikeRequest = false;
    private JFileChooser importFileChooser = null;
    private FileNameExtensionFilter imageFileFilter = new FileNameExtensionFilter("Images", "jpg", "jpeg", "gif",
            "png");

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
        // initialize face recognition with pictures on training dir
        FaceRecog.retrain(trainingDir);
        // capture device may be provided as a system property (e.g.,
        // -DCaptureDevive=1)
        setCaptureDevice(Integer.parseInt(System.getProperty("CaptureDevice", "0")));
    }

    // GUI: Initialisierung
    private void initGUI() {
        
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu pictureMenu = new JMenu("File");
        menuBar.add(pictureMenu);
        pictureMenu.add(createImportPictureMenuItem());
        pictureMenu.add(createSelectMediaDirMenuItem());
        pictureMenu.add(createExitMenuItem());
        

        setLayout(new BorderLayout());
        add(imageLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(createTakePictureButton());
        buttonPanel.add(displayNames);
        buttonPanel.add(movingPics);
        movingPics.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (!movingPics.isSelected()) {
                    for (MovingPicture mp : movingPictures.values()) {
                        mp.terminate();
                    }
                }
            }
        });
        buttonPanel.add(createDeviceRadioButtons());
        
        buttonPanel.add(createLookAlikeButton());

        add(buttonPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                exit();
            }

        });

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
    
    private void exit() {
        stop();
        capture.release();
        setVisible(false);
        System.exit(DISPOSE_ON_CLOSE);
        dispose();
    }
    
    private JMenuItem createExitMenuItem() {
        JMenuItem menuItem = new JMenuItem("Exit");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event){
                exit();
            }
        });
        return menuItem;
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

    private JMenuItem createImportPictureMenuItem() {
        JMenuItem pictureMenuItem = new JMenuItem("Import Picture(s)");
        pictureMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                stop();
                // select picture file to be imported
                if (importFileChooser == null) {
                    importFileChooser = new JFileChooser();
                    importFileChooser.setAcceptAllFileFilterUsed(false);
                    importFileChooser.setFileFilter(imageFileFilter);
                    importFileChooser.setAccessory(new ImagePreview(importFileChooser));
                    importFileChooser.setMultiSelectionEnabled(true);
                    importFileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + "/Pictures"));
                }
                int returnVal = importFileChooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {

                    File[] selectedFiles = importFileChooser.getSelectedFiles();
                    FacePicture[] facePictures = new FacePicture[selectedFiles.length];
                    for (int i=0; i< selectedFiles.length; i++){
                        facePictures[i]=new FacePicture();
                        facePictures[i].importFrom(selectedFiles[i]);
                    } 

                    // just for checks write modified picture to picture dir
                    // fp.writeToPathname(System.getProperty("user.home") +
                    // "/Desktop/test.png");
                    if (facePictures.length==1){
                        new AddFaceDialog(facePictures[0]);
                    } else {
                        new AddFaceDialog(facePictures);
                    }
                }
                
                start();
            }
        });
        pictureMenuItem.setTransferHandler(new PictureFileTransferHandler());
        return pictureMenuItem;
    }
    
    private JMenuItem createSelectMediaDirMenuItem() {
        JMenuItem selectMenuItem = new JMenuItem("Select Media Folder");
        selectMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                stop();
                new AddFaceDialog();
                start();
            }
        });
        return selectMenuItem;
    }

    

    private JPanel createDeviceRadioButtons() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.LINE_AXIS));
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
    
    private JButton createLookAlikeButton() {
        JButton findLookAlike = new JButton("Find Look Alike");
        findLookAlike.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                isLookAlikeRequest = true;
            }
        });
        return findLookAlike;
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
                        if (!faceDetections.empty() && displayNames.isSelected() 
                                || movingPics.isSelected()
                                || isLookAlikeRequest) {
                            faces = webcamImage.isolateFaces(faceDetections);
                            predictions = FaceRecog.recognizeFaces(faces);
                            if (displayNames.isSelected()) {
                                webcamImage.displayNames(predictions, faceDetections);
                            }
                            if (isLookAlikeRequest && predictions.length==1) {
                                isLookAlikeRequest = false;
                                webcamImage.showLookAlikePicture(predictions[0], faceDetections.toArray()[0]);
                            }
                            if (movingPics.isSelected()) {
                                for (Prediction pred : predictions) {
                                    // recognition distance must not be greater
                                    // than 110
                                    if (pred == null || pred.getConfidence() > 110)
                                        continue;
                                    if (movingPictures.containsKey(pred.getName())) {
                                        movingPictures.get(pred.getName()).reset();
                                    } else {
                                        movingPictures.put(pred.getName(), new MovingPicture(pred.getName()));
                                    }
                                }
                            }
                        }
                        webcamImage.drawRectangles(faceDetections);
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
