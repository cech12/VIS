package de.cech12.vis;

import de.cech12.vis.service.GoogleCloudService;
import de.cech12.vis.service.IOCRService;
import de.cech12.vis.service.ITTSService;
import de.cech12.vis.utils.ConfigUtils;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

public class Main {

    public static final String APPLICATION_NAME = "VIS";
    public static final Logger LOGGER = LogManager.getLogger(Main.class);

    private static IOCRService ocrService;
    private static ITTSService ttsService;

    private static JButton button;
    private static Thread speechThread = null;
    private static Player player = null;


    public static void main(String[] args) {
        LOGGER.info(APPLICATION_NAME + " starting.");
        try {
            File configDir = new File("config");
            if (!configDir.exists() && !configDir.mkdirs()) {
                LOGGER.error("Failed to create config directory.");
                return;
            }
            ConfigUtils.initConfig(configDir);
            GoogleCloudService gcs = new GoogleCloudService(configDir);
            ocrService = gcs;
            ttsService = gcs;
            createWindow();
        } catch (Exception ex) {
            LOGGER.error("Failed to initialize.", ex);
        }
        LOGGER.info(APPLICATION_NAME + " started.");
    }

    private static void createWindow() throws Exception {
        JFrame frame = new JFrame(APPLICATION_NAME);
        frame.setSize(400,400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalGlue()); //centered
        frame.add(panel);

        ocrService.addOCRFrameConfiguration(panel);
        ttsService.addTTSFrameConfiguration(panel);

        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        button = new JButton();
        setButtonTextToSpeechConversion();
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.addActionListener(e -> selectionButtonPressed());
        panel.add(button);

        panel.add(Box.createVerticalGlue()); //centered
        frame.setVisible(true);
    }

    private static void selectionButtonPressed() {
        if (speechThread != null && speechThread.isAlive() && !speechThread.isInterrupted()) {
            stopSpeech();
        } else {
            runImageToSpeechConversion();
        }
    }

    private static void setButtonTextToSpeechConversion() {
        button.setText("Read Image from Clipboard");
    }

    private static void setButtonTextToStopSpeech() {
        button.setText("Stop Speech");
    }

    private static void runImageToSpeechConversion() {
        LOGGER.info("Run image to speech conversion.");
        try {
            InputStream imageStream = getImageFromClipboard();
            if (imageStream == null) {
                LOGGER.error("No picture data found in clipboard.");
                return;
            }

            String text = ocrService.getTextFromImage(imageStream);
            LOGGER.info("Generated text: {}", text);

            InputStream speech = ttsService.getSpeechFromText(text);

            if (speech == null) {
                LOGGER.error("No speech was generated.");
                return;
            }

            playSpeech(speech);
        } catch (Exception ex) {
            LOGGER.error("Failed to run conversion.", ex);
        }
    }

    private static InputStream getImageFromClipboard() throws Exception {
        LOGGER.info("Get image from clipboard.");
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            BufferedImage img = (BufferedImage) c.getData(DataFlavor.imageFlavor);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(img, "png", os);
            return new ByteArrayInputStream(os.toByteArray());
        } catch (UnsupportedFlavorException ex) {
            return null;
        }
    }

    private static void playSpeech(InputStream speech) throws Exception {
        LOGGER.info("Play speech.");
        player = new Player(speech);
        // Starte die Wiedergabe in einem separaten Thread
        speechThread = new Thread(() -> {
            setButtonTextToStopSpeech();
            try {
                player.play();
            } catch (JavaLayerException ex) {
                LOGGER.error("Error while trying to play the generated speech.", ex);
            }
            setButtonTextToSpeechConversion();
        });
        speechThread.start();
    }

    private static void stopSpeech() {
        LOGGER.info("Stop speech.");
        player.close();
        player = null;
        speechThread.interrupt();
        speechThread = null;
        setButtonTextToSpeechConversion();
    }

}