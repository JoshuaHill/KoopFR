/*
 * Created on 12.01.2016
 *
 */
package de.hdm.faceCapture;

import java.awt.Toolkit;

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
    private static int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
    private static int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
    
    
    public MovingPicture(Prediction pred) {
        JLabel label = new JLabel();
        ImageIcon icon = new ImageIcon(pred.getProfilePictureFile().getAbsolutePath());
        label.setIcon(icon);
        label.setText("<html><h3>Hallo " + pred.getName() + "</h3></html>");
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
    
    void terminate() {
        iterationsLeft=0;
    }
    
    private void startMoving() {
        new moving().start();        
    }
    
    private class moving extends Thread {
        public void run() {
            int x = (int)(Math.random()*10)+1;
            int y = (int)(Math.random()*10)+1;
            while (iterationsLeft-->0) {
                if (getX()<0 || getX() + getWidth() > screenWidth) {
                    x = -x;
                }
                if (getY()<0 || getY() + getHeight() > screenHeight) {
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
    }
}
