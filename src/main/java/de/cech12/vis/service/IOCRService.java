package de.cech12.vis.service;

import javax.swing.JPanel;
import java.io.InputStream;

public interface IOCRService {

    String getTextFromImage(InputStream image) throws Exception;

    void addOCRFrameConfiguration(JPanel panel) throws Exception;

}
