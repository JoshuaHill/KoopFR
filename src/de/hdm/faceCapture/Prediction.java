/*
 * Created on 08.01.2016
 *
 */
package de.hdm.faceCapture;

public class Prediction {
    String name;
    double confidence;
    
    Prediction(String n, double c){
        name = n;
        confidence = c;
    }
    
    String getName(){
        return name;
    }
    
    double getConfidence(){
        return confidence;
    }
}
