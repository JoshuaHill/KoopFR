/*
 * Created on 12.01.2016
 *
 */
package de.hdm.faceCapture;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;

public class MovingPicture extends JWindow {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    
    public MovingPicture() {
        JLabel label = new JLabel();
        ImageIcon icon = new ImageIcon("C:/Users/Christian/Pictures/Kollegen/rathke.jpg");
        label.setIcon(icon);
        label.setText("<html><h1>Hallo Christian</h1></html>");
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        label.setVerticalTextPosition(SwingConstants.BOTTOM);
        add(label);
        pack();
        setVisible(true);
    }
    
    private void startMoving() {
        new moving().start();        
    }
    
    private class moving extends Thread {
        public void run() {
            int x = (int)(Math.random()*10)+1;
            int y = (int)(Math.random()*10)+1;
            int iterations = 1000;
            while (iterations-->0) {
                if (getX()<0 || getX() + getWidth() > 1920) {
                    x = -x;
                }
                if (getY()<0 || getY() + getHeight() > 1200) {
                    y = -y;
                }
                setLocation(getX()+x, getY()+y);
                
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            setVisible(false);
            dispose();
        }
    };
    
    public static void main(String[] args){
        new MovingPicture().startMoving();
        new MovingPicture().startMoving();
        new MovingPicture().startMoving();
    }
}
