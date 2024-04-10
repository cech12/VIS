package de.cech12.vis.service;

import javax.swing.JPanel;
import java.io.InputStream;

public interface ITTSService {

    InputStream getSpeechFromText(String text) throws Exception;

    void addTTSFrameConfiguration(JPanel panel) throws Exception;

}
