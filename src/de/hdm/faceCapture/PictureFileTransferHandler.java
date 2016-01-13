/*
 * Created on 15.11.2014
 *
 */
package de.hdm.faceCapture;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.swing.TransferHandler;

public class PictureFileTransferHandler extends TransferHandler {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            return false;
        }

        boolean copySupported = (COPY & support.getSourceDropActions()) == COPY;

        if (!copySupported) {
            return false;
        }

        support.setDropAction(COPY);
        return true;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        Transferable t = support.getTransferable();

        try {
            if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                @SuppressWarnings("unchecked")
                Collection<File> l = (java.util.List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                if (l.size() != 1) {
                    return false;
                }
                File file = l.iterator().next();

                FacePicture face = new FacePicture();
                face.importFrom(file);
                new AddFaceDialog(face);
            }
        } catch (UnsupportedFlavorException e) {
            return false;
        } catch (IOException e) {
            return false;
        }

        return true;
    }
}
