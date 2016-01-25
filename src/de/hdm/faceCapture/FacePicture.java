/*
 * Created on 31.12.2015
 *
 */
package de.hdm.faceCapture;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.bytedeco.javacpp.opencv_core;
import org.opencv.core.CvType;
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

public class FacePicture {
    // private static String cascadePath =
    // "resources/cascades/lbpcascades/lbpcascade_frontalface.xml";
    private static String cascadePath = "resources/cascades/haarcascades/haarcascade_frontalface_alt.xml";
    private static CascadeClassifier faceDetector = new CascadeClassifier(cascadePath);
    private Mat picture;

    public FacePicture(Mat mat) {
        picture = mat.clone();
    }

    public FacePicture(FacePicture face) {
        this(face.picture);
    }

    public FacePicture() {
        this(new Mat());
    }

    // capture Image
    boolean capture(VideoCapture capture) {
        if (capture.isOpened()) {
            capture.read(picture);
            return !picture.empty();
        } else {
            return false;
        }
    }

    // Image Scaling
    void scaleImage(double factor) {
        scaleImage(new Size(picture.width()*factor, picture.height()*factor));
    }
    
    void scaleImage(Size size) {
        Mat snapshotScaled = new Mat();
        Imgproc.resize(picture, snapshotScaled, size);
        picture = snapshotScaled;
    }

    // Image cropping
    void cropImage(Rect rect) {
        cropImage(rect.x, rect.y, rect.width, rect.height);
    }

    void cropImage(int x, int y, int width, int height) {
        x = Math.max(x, 0);
        y = Math.max(y, 0);
        width = Math.min(width, picture.width() - x);
        height = Math.min(height, picture.height() - y);
        picture = new Mat(picture, new Rect(x, y, width, height));
    }

    // Image Noise Reduction via Blur
    void blurImage(Size size) {
        Mat snapshotBlurred = new Mat();
        Imgproc.blur(picture, snapshotBlurred, size);
        picture = snapshotBlurred;
    }

    // make it gray
    void grayImage() {
        Mat grayImage = new Mat();
        if (picture.channels()>1) {
            Imgproc.cvtColor(picture, grayImage, Imgproc.COLOR_BGR2GRAY);
            picture = grayImage;
        }
    }

    void drawToLabel(JLabel imageLabel) {
        imageLabel.setIcon(new ImageIcon(toBufferedImage(), "Captured video"));
        imageLabel.invalidate();
    }

    void importFrom(File pictureFile) {

        // read file into Mat
        try {
            toMat(ImageIO.read(pictureFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // scale file to fit appropriate size (500x500)
        double scale = Math.max(500.0 / picture.width(), 500.0 / picture.height());
        scaleImage(new Size(Math.max(500, scale * picture.width()), Math.max(500, scale * picture.height())));
    }

    // see:
    // http://answers.opencv.org/question/46638/java-how-capture-webcam-and-show-it-in-a-jpanel-like-imshow/
    // Mat() to BufferedImage
    BufferedImage toBufferedImage() {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (picture.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }

        BufferedImage image = new BufferedImage(picture.width(), picture.height(), type);
        WritableRaster raster = image.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        byte[] data = dataBuffer.getData();

        picture.get(0, 0, data);

        return image;
    }

    void toMat(BufferedImage image) {
        WritableRaster raster = image.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        byte[] data = dataBuffer.getData();

        picture = new Mat(image.getHeight(), image.getWidth(),
                image.getType() == BufferedImage.TYPE_BYTE_GRAY ? CvType.CV_8U : CvType.CV_8UC3);
        picture.put(0, 0, data);
    }

    void putText(String text) {
        Imgproc.putText(picture, text, new Point(20, 50), 2, 1, new Scalar(0, 0, 255));
    }

    void putText(String text, Point pos) {
        Imgproc.putText(picture, text, pos, 2, 0.5, new Scalar(0, 0, 255));
    }

    void putTexts(String[] texts) {
        int verticalPos = 50;
        for (String text : texts) {
            Imgproc.putText(picture, text, new Point(20, verticalPos), 2, 1, new Scalar(0, 0, 255));
            verticalPos += 25;
        }
    }

    void displayNames(Prediction preds[], MatOfRect detections) {
        Rect[] rects = detections.toArray();
        for (int i = 0; i < preds.length; i++) {
            if (preds[i] != null) {
                putText(preds[i].getName() + " " + (int) preds[i].getConfidence(),
                        new Point(rects[i].x, rects[i].y + rects[i].height + 25));
            }
        }
    }

    FacePicture createProfileImage(Rect rect) {
        int deltaWidth = rect.width / 4;
        int deltaHeight = rect.height / 3;
        FacePicture fp = new FacePicture(this);
        fp.cropImage(rect.x - deltaWidth, rect.y - deltaHeight, rect.width + 2 * deltaWidth,
                rect.height + 2 * deltaHeight);
        return fp;
    }

    void saveAsProfileImage(File directory) {

        BufferedImage bimage = toBufferedImage();
        // scale image to 300 pixels in height
        Image img = bimage.getScaledInstance(-1, 300, BufferedImage.SCALE_DEFAULT);
        // Create an empty buffered image with appropriate size and type
        bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), bimage.getType());
        // Draw the scaled image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        try {
            ImageIO.write(bimage, "jpg", new File(directory.getPath() + "/profilePicture.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void showLookAlikePicture(Prediction pred, Rect rect) {
        JFrame lookAlikeWindow = new JFrame();

        JButton heading = new JButton("<html><body><h1>OMG: You look just like " + pred.getName() + "!</h1></body></html>");
        lookAlikeWindow.add(heading, BorderLayout.PAGE_START);

        JLabel snapShotLabel = new JLabel();
        FacePicture snapShot = createProfileImage(rect);
        snapShot.scaleImage(2.0);
        snapShot.drawToLabel(snapShotLabel);
        lookAlikeWindow.add(snapShotLabel, BorderLayout.LINE_END);

        File profileFile = pred.getProfilePictureFile();
        if (profileFile != null) {
            JLabel profileLabel = new JLabel();
            Image scaledImage = new ImageIcon(profileFile.getPath()).getImage().getScaledInstance(-1,
                    snapShotLabel.getIcon().getIconHeight(), Image.SCALE_DEFAULT);
            profileLabel.setIcon(new ImageIcon(scaledImage));
            lookAlikeWindow.add(profileLabel, BorderLayout.LINE_START);
        }

        lookAlikeWindow.pack();
        lookAlikeWindow.setLocationRelativeTo(null);
        lookAlikeWindow.setVisible(true);
    }

    void writeToPathname(String path) {
        Imgcodecs.imwrite(path, picture);
        // System.out.println("Snapshot: " + path + " taken");
    }

    // see: https://github.com/bytedeco/javacpp/issues/38
    opencv_core.Mat convertToJavaCVMat() {
        return new opencv_core.Mat() {
            {
                address = picture.getNativeObjAddr();
            }
        };

    }

    MatOfRect detectFaces() {
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(picture, faceDetections, 1.1, 7, 0, new Size(50, 70), new Size(0, 0));
        return faceDetections;
    }

    void drawRectangles(MatOfRect rects) {
        for (Rect rect : rects.toArray()) {
            drawRectangle(rect);
        }
    }

    void drawRectangle(Rect rect) {
        Imgproc.rectangle(picture, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                new Scalar(0, 255, 0));
    }

    FacePicture[] isolateFaces(MatOfRect faceDetections) {

        Rect[] detections = faceDetections.toArray();
        FacePicture[] faces = new FacePicture[detections.length];

        for (int i = 0; i < detections.length; i++) {
            Rect rect = detections[i];
            faces[i] = isolateFace(rect);
        }
        return faces;
    }

    FacePicture isolateFace(Rect rect) {
        // Crop, blur, resize and gray Image
        FacePicture fp = new FacePicture(picture);
        fp.cropImage(rect);
        if (rect.height > 150){
            double scaleFactor = 150.0 / rect.height;
            fp.scaleImage(new Size(rect.width*scaleFactor, rect.height*scaleFactor));
        }
        fp.blurImage(new Size(3.0, 3.0));
        fp.scaleImage(new Size(75, 75));
        fp.grayImage();
        return fp;
    }
}
