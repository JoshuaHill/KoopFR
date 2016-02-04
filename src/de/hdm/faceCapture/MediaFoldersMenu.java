/*
 * Created on 31.01.2016
 *
 */
package de.hdm.faceCapture;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class MediaFoldersMenu extends JMenu {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public MediaFoldersMenu(String text) {
        super(text);
        JMenuItem first = new JMenuItem("Select Media Folder");
        first.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                new AddFaceDialog();
            }
        });
        add(first);
        readMediaFolders();
    }

    void addFolder(File folder) {
        if (folder != null) {
            JMenuItem menuItem = new JMenuItem(folder.getAbsolutePath());
            menuItem.addActionListener(new ActionListener() {
                File file = folder;

                public void actionPerformed(ActionEvent ae) {
                    addFolder(file);
                    FaceRecog.retrain(file);
                }
            });
            add(menuItem, 1);

            for (int i = 2; i < getItemCount(); i++) {
                menuItem = (JMenuItem) getMenuComponent(i);
                if (folder.getAbsolutePath().equals(menuItem.getText())) {
                    remove(menuItem);
                    i--;
                }
            }
            if (getItemCount() > 10) {
                remove(getMenuComponent(getItemCount() - 1));
            }
        }
    }

    void readMediaFolders() {
        File file = new File(System.getenv("appdata") + "\\FaceRecognizerMedia.txt");
        String[] pathNames = new String[0];
        if (file.exists()) {
            try {
                FileReader fr = new FileReader(file.getAbsoluteFile());
                BufferedReader br = new BufferedReader(fr);
                pathNames = br.readLine().split(";");
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (String pathName : pathNames) {
                addFolder(new File(pathName));
            }
        }
    }

    void saveMediaFolders() {
        String pathNames = new String();
        for (int i=1; i<getItemCount(); i++) {
            pathNames += ";" + ((JMenuItem) getMenuComponent(i)).getText();
        }
        if (pathNames.length() > 0) {
            pathNames = pathNames.substring(1);
        }
        File file = new File(System.getenv("appdata") + "\\FaceRecognizerMedia.txt");
        try {
            file.createNewFile();
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(pathNames);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
