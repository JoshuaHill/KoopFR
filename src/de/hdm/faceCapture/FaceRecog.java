package de.hdm.faceCapture;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

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

    private static String trainingDir = "media/";
    private static File[] directories = new File[0];
    private static FaceRecognizer faceRecognizer =
//    org.bytedeco.javacpp.opencv_face.createFisherFaceRecognizer();
//    org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer();
    org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer();
    private static FilenameFilter imgFilter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            name = name.toLowerCase();
            return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
        }
    };

    // Training
    public static void retrain() {
        directories = new File(trainingDir).listFiles();
        System.out.println("(Re)train using " + directories.length + " media directories");

        // collect and count all image files in all media directories
        File[][] files = new File[directories.length][];
        int fileCount = 0;
        for (int dirCounter = 0; dirCounter < directories.length; dirCounter++) {
            File dir = directories[dirCounter];
            if (dir.isDirectory()) {
                files[dirCounter] = dir.listFiles(imgFilter);
                fileCount += files[dirCounter].length;
            } else {
                files[dirCounter] = new File[0];
            }
        }

        // train the recognizer with all files found
        MatVector images = new MatVector(fileCount);
        Mat labels = new Mat(fileCount, 1, CV_32SC1);
        IntBuffer labelsBuf = labels.getIntBuffer();

        Mat img = null;
        for (int dirCounter = 0; dirCounter < files.length; dirCounter++) {
            for (File imageFile : files[dirCounter]) {
                fileCount--;
                img = imread(imageFile.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
                images.put(fileCount, img);
                labelsBuf.put(fileCount, dirCounter);
            }
        }
        faceRecognizer.train(images, labels);
    }

    // Recognition using saved picture
    public static String recognizeFace(FacePicture face) {
        if (face != null) {
            String tmpFilePath = trainingDir + "temp.png";
            face.writeToPathname(tmpFilePath);

            int[] prediction = new int[1];
            double[] confidence = new double[1];

            faceRecognizer.predict(imread(tmpFilePath, CV_LOAD_IMAGE_GRAYSCALE), prediction, confidence);
            // System.out.println("Predict " +
            // directories[prediction[0]].getName() + " with confidence " +
            // confidence[0]);
            return directories[prediction[0]].getName();
        } else {
            return null;
        }
    }

    public static String[] recognizeFaces(FacePicture[] faces) {
        String[] names = new String[faces.length];
        for (int i = 0; i < faces.length; i++) {
            names[i] = recognizeFace(faces[i]);
        }
        return names;
    }

}
