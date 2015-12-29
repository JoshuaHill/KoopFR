package de.hdm.faceCapture;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;

/**
 * 
 * This is mostly taken from http://bytedeco.org/ see also
 * https://github.com/bytedeco/javacv/blob/master/samples/OpenCVFaceRecognizer.
 * java
 *
 */

public class FaceRecog {

    public String trainingDir = "media/";
    // private FaceRecognizer faceRecognizer = opencv_face.createFisherFaceRecognizer();
    // private FaceRecognizer faceRecognizer = opencv_face.createEigenFaceRecognizer();
    private FaceRecognizer faceRecognizer = createLBPHFaceRecognizer();
    private File[] directories = new File[0];
    

    // Training
    @SuppressWarnings("deprecation")
    public void initFaceRec() {
        
        directories = new File(trainingDir).listFiles();
        System.out.println("Media directories: " + directories.length);

        FilenameFilter imgFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
            }
        };

        MatVector images;
        Mat labels;
        IntBuffer labelsBuf;

        faceRecognizer.clear();
        for (int dirCounter = 0; dirCounter < directories.length; dirCounter++) {
            File dir = directories[dirCounter];
            if (dir.isDirectory()) {
                File[] imageFiles = dir.listFiles(imgFilter);
                images = new MatVector(imageFiles.length);
                labels = new Mat(imageFiles.length, 1, CV_32SC1);
                labelsBuf = labels.getIntBuffer();

                for (int imageCounter = 0; imageCounter < imageFiles.length; imageCounter++) {
                    File image = imageFiles[imageCounter];
                    Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);

                    images.put(imageCounter, img);
                    labelsBuf.put(imageCounter, dirCounter);
                }
                System.out.println("updating face recognizer with " + dir.getName());
                faceRecognizer.update(images, labels);
            }

        }
        // faceRecognizer.train(images, labels);
    }

    // Recognition using saved picture
    public String startRecognition(String path) {
        int[] prediction = new int[1];
        double[] confidence = new double[1];
        // int predictedLabel =
        faceRecognizer.predict(imread(path, CV_LOAD_IMAGE_GRAYSCALE), prediction, confidence);
        // System.out.println("Predict " + directories[prediction[0]].getName() + " with confidence " + confidence[0]);
        // return directories[predictedLabel].getName();
        return directories[prediction[0]].getName();
    }

}
