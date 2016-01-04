package de.hdm.faceCapture;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

import org.bytedeco.javacpp.opencv_core.CvType;
import org.opencv.core.Mat;

/**
 * compare: http://answers.opencv.org/question/46638/java-how-capture-webcam-and-show-it-in-a-jpanel-like-imshow/
 *
 */
public class ImageProcessor {

	public static BufferedImage toBufferedImage(Mat matrix){
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if ( matrix.channels() > 1 ) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		int bufferSize = matrix.channels()*matrix.cols()*matrix.rows();
		byte [] buffer = new byte[bufferSize];
		matrix.get(0,0,buffer); // get all the pixels
		BufferedImage image = new BufferedImage(matrix.cols(),matrix.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);  
		return image;
	}
	
	public static BufferedImage MatToBufferedImage(Mat matrix) {
        //Mat() to BufferedImage
        int type = 0;
        if (matrix.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else if (matrix.channels() == 3) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage image = new BufferedImage(matrix.width(), matrix.height(), type);
        WritableRaster raster = image.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        byte[] data = dataBuffer.getData();
        matrix.get(0, 0, data);

        return image;
    }
	
}
