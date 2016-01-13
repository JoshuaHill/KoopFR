/*
 * Created on 01.01.2016
 *
 */
package de.hdm.faceCapture;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;

public class AddFaceDialog extends JDialog {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JLabel imageLabel = new JLabel();
    JCheckBox saveAsProfile = new JCheckBox("Save as profile picture", false);
    private FacePicture candidate;
    private JFileChooser pictureDirChooser = null;

    private class saveFacesButtonActionListener implements ActionListener {
        private MatOfRect rects;

        public saveFacesButtonActionListener(MatOfRect rects) {
            this.rects = rects;
        }

        public void actionPerformed(ActionEvent event) {
            Rect[] detections = rects.toArray();
            for (int i = 0; i < detections.length; i++) {
                Rect rect = detections[i];
                FacePicture fp = new FacePicture(candidate);
                fp.drawRectangle(new Rect(rect.x, rect.y, rect.width, rect.height));
                fp.drawToLabel(imageLabel);
                repaint();

                File mediaDir = new File("faces/");
                if (!mediaDir.exists() || !mediaDir.isDirectory()) {
                    mediaDir = new File(System.getProperty("user.home"));
                }

                if (pictureDirChooser == null) {
                    pictureDirChooser = new JFileChooser();
                    pictureDirChooser.setAcceptAllFileFilterUsed(false);

                    pictureDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    pictureDirChooser.setCurrentDirectory(mediaDir);
                }
                int returnVal = pictureDirChooser.showOpenDialog(null);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File selectedDir = pictureDirChooser.getSelectedFile();
                    if (saveAsProfile.isSelected()) {
                        BufferedImage image = candidate.toBufferedImage();
                        try {
                            ImageIO.write(image, "jpg", new File(selectedDir.getPath() + "/profilePicture.jpg"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    String path = createPictureFilePathName(selectedDir);
                    candidate.isolateFace(rect).writeToPathname(path);
                    FaceRecog.retrain(selectedDir.getParent());
                }
            }
            setVisible(false);
            dispose();
        }
    }

    public AddFaceDialog() {
        this(new FacePicture());
    }

    public AddFaceDialog(FacePicture fp) {
        super();
        candidate = new FacePicture(fp);
        initGui();
    }

    private void initGui() {
        this.setLayout(new BorderLayout());
        add(imageLabel, BorderLayout.CENTER);
        candidate.drawToLabel(imageLabel);
        JPanel controlsPanel = new JPanel();
        add(controlsPanel, BorderLayout.SOUTH);
        
        if (candidate != null) {
            MatOfRect rects = candidate.detectFaces();

            if (!rects.empty()) {
                FacePicture fp = new FacePicture(candidate);
                fp.drawRectangles(rects);

                JButton saveFacesButton = new JButton("Save faces");
                saveFacesButton.addActionListener(new saveFacesButtonActionListener(rects));
                controlsPanel.add(saveFacesButton);
                if (rects.rows()==1) {
                    saveFacesButton.setText("Save face");
                    saveAsProfile.setSelected(true);
                    controlsPanel.add(saveAsProfile);
                }
                fp.drawToLabel(imageLabel);
            } else {
                JButton quitButton = new JButton("No faces detected. Close Window.");
                quitButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event){
                        setVisible(false);
                        dispose();
                    }
                });
                controlsPanel.add(quitButton);
            }
        }

        pack();
        setVisible(true);
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
