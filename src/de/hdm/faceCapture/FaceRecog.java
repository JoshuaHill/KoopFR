package de.hdm.faceCapture;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;

import static org.bytedeco.javacpp.opencv_face.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
/**
 * 
 * This is mostly taken from http://bytedeco.org/
 * see also https://github.com/bytedeco/javacv/blob/master/samples/OpenCVFaceRecognizer.java9
 *
 */

public class FaceRecog {
	
	

	public String trainingDir = "media/test/";
	// private FaceRecognizer faceRecognizer = createFisherFaceRecognizer();
	// private FaceRecognizer faceRecognizer = createEigenFaceRecognizer();
    private FaceRecognizer faceRecognizer = createLBPHFaceRecognizer();

	
	
	// Training
	public void initFaceRec() {
		
        File root = new File(trainingDir);

        FilenameFilter imgFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
            }
        };

        File[] imageFiles = root.listFiles(imgFilter);

        MatVector images = new MatVector(imageFiles.length);

        Mat labels = new Mat(imageFiles.length, 1, CV_32SC1);
        
		@SuppressWarnings("deprecation")
		IntBuffer labelsBuf = labels.getIntBuffer();

        int counter = 0;

        for (File image : imageFiles) {
            Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);

            int label = Integer.parseInt(image.getName().split("\\-")[0]);

            images.put(counter, img);

            labelsBuf.put(counter, label);

            counter++;
        }
        faceRecognizer.train(images, labels);
    }
	
	
	// Recognition
	public int startRecognition(String path) {
		
		int predictedLabel = faceRecognizer.predict(imread(path, CV_LOAD_IMAGE_GRAYSCALE));
		// return predictedLabel;
		return predictedLabel;
	}
    
}
    
