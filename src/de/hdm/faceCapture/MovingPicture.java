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
    private int iterationsLeft = 1000;
    
    
    public MovingPicture(String name) {
        JLabel label = new JLabel();
        ImageIcon icon = new ImageIcon("faces/" + name + "/profilePicture.jpg");
        label.setIcon(icon);
        label.setText("<html><h1>Hallo " + name + "</h1></html>");
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        label.setVerticalTextPosition(SwingConstants.BOTTOM);
        add(label);
        pack();

        reset();
    }
    
    void reset() {
        iterationsLeft=1000;
        if (!isVisible()) {
            setVisible(true);
            startMoving();
        }
    }
    
    private void startMoving() {
        new moving().start();        
    }
    
    private class moving extends Thread {
        public void run() {
            int x = (int)(Math.random()*10)+1;
            int y = (int)(Math.random()*10)+1;
            while (iterationsLeft-->0) {
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
        new MovingPicture("Christian Rathke");
        new MovingPicture("Peter Thies");
        new MovingPicture("Alexander Roos");
    }
}
