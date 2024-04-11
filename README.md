# VIS (Versatile Image Speech)

VIS (Versatile Image Speech) is a Java tool, which uses OCR (optical character recognition) and TTS (text-to-speech) services to enable images to be read aloud.

With the push of a button, a voice output can be played from an image on the clipboard.

## Features

- Converts images from clipboard to speech
- Plays back the converted speech
- (optional) Translates the detected text into the voices language before the speech is generated and played
- Configurable voice settings (language, voice, speed, pitch)

### OCR Services
- Google Cloud Vision API - https://cloud.google.com/vision

### TTS Services
- Google Cloud Text-To-Speech AI - https://cloud.google.com/text-to-speech

### Translation Services
- Google Cloud Translation - https://cloud.google.com/translate

## Requirements

- Java Development Kit (JDK) (17 or higher recommended)
- Internet connection for utilizing OCR and TTS services
- Service Account of a Google Cloud Project with enabled Vision API & Text-To-Speech API https://console.cloud.google.com/
- (optional) Google Cloud Translation could also be enabled if translation functionality should be used.

## Installation

1. Clone the repository:

```bash
git clone https://github.com/cech12/VIS.git
```

2. Navigate to the project directory:

```bash
cd VIS
```

3. Create a Google Cloud Project

https://cloud.google.com/resource-manager/docs/creating-managing-projects

Enable following APIs:
- Google Cloud Vision API
- Google Cloud Text-To-Speech AI
- (optional) Google Cloud Translation

4. Setup Service Account for Google Cloud Project

https://cloud.google.com/iam/docs/service-account-overview

- Create & Download the key file of the created Service Account and save it as "credentials.json"
- put the file into the config directory (directly located in the project)

## Usage

1. Run the application

```bash
./gradlew run
```

2. Once the application is running, you can configure your preferred language & voice
3. (optional) add your Google Cloud Project ID into the field for the translation functionality
4. Copy an image to the clipboard. (You can use Tools like Windows Snipping Tool to copy something on your screen)
5. Hit the "Read Image from Clipboard" button and hear the voice
6. You can hit the "Stop Speech" button to stop the voice output

## Configuration

- You can configure voice settings (language, voice, speed, pitch, project ID) by editing the parameters in the UI (works directly for the next voice output)
- or by changing the values in the "config/vis.config" file (needs an application restart)

## Contributing

Contributions to the project are welcome! If you find any issues or have suggestions for improvements, please open an issue or submit a pull request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.