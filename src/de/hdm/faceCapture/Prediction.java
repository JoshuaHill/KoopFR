/*
 * Created on 08.01.2016
 *
 */
package de.hdm.faceCapture;

public class Prediction {
    private String name;
    private double confidence;
    
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
