package de.cech12.vis.service;

import javax.swing.JPanel;

public interface ITranslationService {

    String getTranslationOfText(String language, String text) throws Exception;

    boolean isTranslationAvailableForLanguage(String language) throws Exception;

    void addTranslationFrameConfiguration(JPanel panel) throws Exception;

}
