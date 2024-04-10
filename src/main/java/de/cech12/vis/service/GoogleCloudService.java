package de.cech12.vis.service;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.ListVoicesRequest;
import com.google.cloud.texttospeech.v1.ListVoicesResponse;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.TextToSpeechSettings;
import com.google.cloud.texttospeech.v1.Voice;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import com.google.protobuf.ByteString;
import de.cech12.vis.utils.ConfigUtils;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoogleCloudService implements IOCRService, ITTSService {

    public static final String CONFIG_LANGUAGE = "google.tts.language";
    public static final String CONFIG_VOICE = "google.tts.voice";
    public static final String CONFIG_SPEED = "google.tts.speed";
    public static final String CONFIG_PITCH = "google.tts.pitch";

    private final CredentialsProvider credentialsProvider;

    private Map<String, List<String>> allVoices; // language > name

    public GoogleCloudService(File configDir) throws Exception {
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(new File(configDir.toURI().resolve("./credentials.json"))));
        credentialsProvider = FixedCredentialsProvider.create(credentials);
        getAllVoices();
    }

    public void getAllVoices() throws IOException {
        allVoices = new HashMap<>();
        // Instantiates a client
        TextToSpeechSettings settings = TextToSpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(settings)) {
            // Builds the text to speech list voices request
            ListVoicesRequest request = ListVoicesRequest.getDefaultInstance();

            // Performs the list voices request
            ListVoicesResponse response = textToSpeechClient.listVoices(request);
            List<Voice> voices = response.getVoicesList();

            for (Voice voice : voices) {
                List<ByteString> languageCodes = voice.getLanguageCodesList().asByteStringList();
                for (ByteString languageCode : languageCodes) {
                    String language = languageCode.toStringUtf8();
                    if (!allVoices.containsKey(language)) {
                        allVoices.put(language, new ArrayList<>());
                    }
                    List<String> voiceList = allVoices.get(language);
                    voiceList.add(voice.getName());
                }
            }
        }
    }

    @Override
    public void addOCRFrameConfiguration(JPanel panel) {
        //do nothing
    }

    @Override
    public void addTTSFrameConfiguration(JPanel panel) throws IOException {
        //load configured values
        String[] languages = allVoices.keySet().stream().sorted().toArray(String[]::new);
        String language = ConfigUtils.getPropertyOrDefault(CONFIG_LANGUAGE, "en-US");
        String[] voices = allVoices.get(language).stream().sorted().toArray(String[]::new);
        String voice = ConfigUtils.getPropertyOrDefault(CONFIG_VOICE, voices[0]);
        double speed = ConfigUtils.getDoublePropertyOrDefault(CONFIG_SPEED, 1);
        double pitch = ConfigUtils.getDoublePropertyOrDefault(CONFIG_PITCH, 0);

        JLabel voiceLabel = new JLabel("Select a voice");
        voiceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JComboBox<String> voiceComboBox = new JComboBox<>(voices);
        voiceComboBox.setSelectedIndex(Arrays.asList(voices).indexOf(voice));
        voiceComboBox.setMaximumSize(voiceComboBox.getPreferredSize());
        voiceComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        voiceComboBox.addActionListener(event -> {
            String item = (String) voiceComboBox.getSelectedItem();
            if (item != null) {
                try {
                    ConfigUtils.setProperty(CONFIG_VOICE, item);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        JLabel languageLabel = new JLabel("Select a language");
        languageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JComboBox<String> languageComboBox = new JComboBox<>(languages);
        languageComboBox.setSelectedIndex(Arrays.asList(languages).indexOf(language));
        languageComboBox.setMaximumSize(languageComboBox.getPreferredSize());
        languageComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        languageComboBox.addActionListener(event -> {
            String item = (String) languageComboBox.getSelectedItem();
            try {
                ConfigUtils.setProperty(CONFIG_LANGUAGE, item);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //change voice combo box
            voiceComboBox.removeAllItems();
            for (String voiceOfLanguage : allVoices.get(item).stream().sorted().toArray(String[]::new)) {
                voiceComboBox.addItem(voiceOfLanguage);
            }
            voiceComboBox.setSelectedIndex(0);
        });

        JLabel speedLabel = new JLabel("Select the voice speed (%)");
        speedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JSlider speedSlider = new JSlider(SwingConstants.HORIZONTAL, 25, 400, (int) (speed * 100D));
        speedSlider.setMaximumSize(voiceComboBox.getPreferredSize());
        speedSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
        speedSlider.addChangeListener(event -> {
            try {
                ConfigUtils.setDoubleProperty(CONFIG_SPEED, speedSlider.getValue() / 100D);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        JLabel pitchLabel = new JLabel("Select the voice pitch");
        pitchLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JSlider pitchSlider = new JSlider(SwingConstants.HORIZONTAL, -2000, 2000, (int) (pitch * 100D));
        pitchSlider.setMaximumSize(pitchSlider.getPreferredSize());
        pitchSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
        pitchSlider.addChangeListener(event -> {
            try {
                ConfigUtils.setDoubleProperty(CONFIG_PITCH, pitchSlider.getValue() / 100D);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        panel.add(languageLabel);
        panel.add(languageComboBox);
        panel.add(voiceLabel);
        panel.add(voiceComboBox);
        panel.add(speedLabel);
        panel.add(speedSlider);
        panel.add(pitchLabel);
        panel.add(pitchSlider);
    }

    @Override
    public String getTextFromImage(InputStream imageData) throws Exception {
        StringBuilder result = new StringBuilder();

        List<AnnotateImageRequest> requests = new ArrayList<>();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build())
                .setImage(Image.newBuilder().setContent(ByteString.readFrom(imageData)).build())
                .build();
        requests.add(request);

        ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create(settings)) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            for (AnnotateImageResponse res : response.getResponsesList()) {
                if (res.hasError()) {
                    throw new Exception("Error in AnnotateImageResponse: " + res.getError().getMessage());
                }
                result.append(res.getFullTextAnnotation().getText());
            }
        }
        return result.toString();
    }

    @Override
    public InputStream getSpeechFromText(String text) throws Exception {
        TextToSpeechSettings settings = TextToSpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(settings)) {
            // Set the text input to be synthesized
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

            // Build the voice request
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode(ConfigUtils.getProperty(CONFIG_LANGUAGE))
                    .setName(ConfigUtils.getProperty(CONFIG_VOICE))
                    .build();

            // Select the type of audio file you want returned
            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.MP3)
                    .setSpeakingRate(ConfigUtils.getDoubleProperty(CONFIG_SPEED))
                    .setPitch(ConfigUtils.getDoubleProperty(CONFIG_PITCH))
                    .build();

            // Perform the text-to-speech request
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            return new ByteArrayInputStream(response.getAudioContent().toByteArray());
        }
    }

}
