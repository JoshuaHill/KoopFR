/*
 * Created on 01.01.2016
 *
 */
package de.hdm.faceCapture;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;

public class AddFaceDialog extends JDialog {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JLabel imageLabel = new JLabel();
    private JRadioButton alsoSaveAsProfile = new JRadioButton("Also as Profile Picture", false);
    private JRadioButton SaveAsProfile = new JRadioButton("Only as Profile Picture");
    private JRadioButton justTheFace = new JRadioButton("Just the Face");
    private FacePicture candidate;
    static JFileChooser pictureDirChooser = initPictureDirChooser();

    private class saveFacesButtonActionListener implements ActionListener {
        private MatOfRect rects;

        public saveFacesButtonActionListener(MatOfRect rects) {
            this.rects = rects;
        }

        public void actionPerformed(ActionEvent event) {
            Rect[] detections = rects.toArray();
            for (Rect rect : detections) {
                FacePicture fp = new FacePicture(candidate);
                fp.drawRectangle(new Rect(rect.x, rect.y, rect.width, rect.height));
                fp.drawToLabel(imageLabel);
                repaint();

                if (pictureDirChooser.showDialog(AddFaceDialog.this,
                        "Select Faces Folder") == JFileChooser.APPROVE_OPTION) {
                    File selectedDir = pictureDirChooser.getSelectedFile();
                    if (selectedDir != null) {
                        if (alsoSaveAsProfile.isSelected() || SaveAsProfile.isSelected()) {
                            candidate.createProfileImage(rect).saveAsProfileImage(selectedDir);
                        }
                        if (alsoSaveAsProfile.isSelected() || justTheFace.isSelected()) {
                            candidate.isolateFace(rect).writeToPathname(createPictureFilePathName(selectedDir));
                        }
                        FaceRecog.retrain(selectedDir.getParentFile());
                    }
                }
            }
            setVisible(false);
            dispose();
        }
    }

    public AddFaceDialog(FacePicture[] facePictures) {
        if (pictureDirChooser.showDialog(AddFaceDialog.this, "Select Faces Folder") == JFileChooser.APPROVE_OPTION) {
            File selectedDir = pictureDirChooser.getSelectedFile();
            if (selectedDir != null) {
                for (FacePicture fp : facePictures) {
                    Rect[] rects = fp.detectFaces().toArray();
                    if (rects.length == 1) {
                        fp.isolateFace(rects[0]).writeToPathname(createPictureFilePathName(selectedDir));
                    }
                }
                FaceRecog.retrain(selectedDir.getParentFile());
            }
        }
    }

    public AddFaceDialog(FacePicture fp) {
        candidate = new FacePicture(fp);
        initGui();
    }

    public AddFaceDialog() {
        pictureDirChooser.showDialog(this, "Select Media Folder");
        if (pictureDirChooser.getSelectedFile() != null) {
            FaceRecog.retrain(pictureDirChooser.getSelectedFile());
        }
    }

    private void initGui() {
        this.setLayout(new BorderLayout());
        add(imageLabel, BorderLayout.CENTER);
        candidate.drawToLabel(imageLabel);
        JPanel controlsPanel = new JPanel();
        add(controlsPanel, BorderLayout.SOUTH);
        ButtonGroup bg = new ButtonGroup();
        bg.add(SaveAsProfile);
        bg.add(alsoSaveAsProfile);
        bg.add(justTheFace);

        if (candidate != null) {
            MatOfRect rects = candidate.detectFaces();

            if (!rects.empty()) {
                FacePicture fp = new FacePicture(candidate);
                fp.drawRectangles(rects);

                JButton saveFacesButton = new JButton("Save Faces");
                saveFacesButton.addActionListener(new saveFacesButtonActionListener(rects));
                controlsPanel.add(saveFacesButton);
                if (rects.rows() == 1) {
                    saveFacesButton.setText("Save Face");
                    alsoSaveAsProfile.setSelected(true);
                    controlsPanel.add(justTheFace);
                    controlsPanel.add(alsoSaveAsProfile);
                    controlsPanel.add(SaveAsProfile);
                }
                fp.drawToLabel(imageLabel);
            }
        }

        pack();
        setVisible(true);
    }

    private static JFileChooser initPictureDirChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setCurrentDirectory(new File(System.getProperty("user.home")).getAbsoluteFile());
        return chooser;
    }

    public static File getLastSelectedFile() {
        return pictureDirChooser.getSelectedFile();
    }

    private String createPictureFilePathName(File dir) {
        File file = new File(dir.getPath() + "/image-0.png");
        int counter = 1;
        while (file.exists()) {
            file = new File(dir.getPath() + "/image-" + counter + ".png");
            counter++;
        }
        return file.getPath();
    }
}
