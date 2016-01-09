/*
 * Created on 31.12.2015
 *
 */
package de.hdm.faceCapture;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
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
    private void scaleImage(Size size) {
        Mat snapshotScaled = new Mat();
        Imgproc.resize(picture, snapshotScaled, size);
        picture = snapshotScaled;
    }

    // Image cropping
    private void cropImage(Rect rect) {
        picture = new Mat(picture, rect);
    }

    // Image Noise Reduction via Blur
    private void blurImage(Size size) {
        Mat snapshotBlurred = new Mat();
        Imgproc.blur(picture, snapshotBlurred, size);
        picture = snapshotBlurred;
    }

    // make it gray
    private void grayImage() {
        Mat grayImage = new Mat();
        Imgproc.cvtColor(picture, grayImage, Imgproc.COLOR_BGR2GRAY);
        picture = grayImage;
    }

    void drawToLabel(JLabel imageLabel) {
        imageLabel.setIcon(new ImageIcon(ImageProcessor.toBufferedImage(picture), "Captured video"));
        imageLabel.invalidate();
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
            putText(preds[i].getName() + " " + (int) preds[i].getConfidence(),
                    new Point(rects[i].x, rects[i].y + rects[i].height + 25));
        }
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

    void importFrom(File pictureFile) {

        // read file into Mat
        try {
            BufferedImage image = ImageIO.read(pictureFile);
            byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            picture = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
            picture.put(0, 0, data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // reject small pictures
/*        if (picture.width() < 500 || picture.height() < 500) {
            JOptionPane.showMessageDialog(null, "picture is too small");
            return;
        }
*/
        // scale file to fit appropriate size (500x500)
        double scale = Math.max(500.0 / picture.width(), 500.0 / picture.height());
        scaleImage(new Size(Math.max(500, scale * picture.width()), Math.max(500, scale * picture.height())));

        // cut file to a size of 500/500
/*        int xDiff = (picture.width() - 500) / 2;
        int yDiff = (picture.height() - 500) / 2;
        cropImage(new Rect(xDiff, yDiff, 500, 500));
*/ 
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
        fp.blurImage(new Size(3.0, 3.0));
        fp.scaleImage(new Size(75, 75));
        fp.grayImage();
        return fp;
    }
}
