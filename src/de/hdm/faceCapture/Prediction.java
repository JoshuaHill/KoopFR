/*
 * Created on 08.01.2016
 *
 */
package de.hdm.faceCapture;

import java.io.File;

public class Prediction {
    private File folder;
    private double confidence;
    
    Prediction(File f, double c){
        folder = f;
        confidence = c;
    }
    
    File getFolder() {
        return folder;
    }
    
    File getProfilePictureFile() {
       File profilePictureFile = new File(folder.getAbsolutePath() + "/profilePicture.jpg");
       if (profilePictureFile.exists()) {
           return profilePictureFile;
       } else {
           return null;
       }
    }
    
    String getName(){
        return folder.getName();
    }
    
    double getConfidence(){
        return confidence;
    }
}
