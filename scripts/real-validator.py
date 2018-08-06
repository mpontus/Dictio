#!/usr/bin/env python3

# Validate that a phrase can be recognized by running it through TTS

def synthesize_text(language, sample_rate, text):
    """Synthesizes speech from the input string of text."""
    from google.cloud import texttospeech
    client = texttospeech.TextToSpeechClient()
    input_text = texttospeech.types.SynthesisInput(text=text)
    voice = texttospeech.types.VoiceSelectionParams(language_code=language)
    audio_config = texttospeech.types.AudioConfig(
        audio_encoding=texttospeech.enums.AudioEncoding.LINEAR16,
        sample_rate_hertz=sample_rate)
    response = client.synthesize_speech(input_text, voice, audio_config)

    return response.audio_content;

def recognize_speech(language, sample_rate, content):
    from google.cloud import speech

    client = speech.SpeechClient()

    audio = speech.types.RecognitionAudio(content=content)
    config = speech.types.RecognitionConfig(
        encoding=speech.enums.RecognitionConfig.AudioEncoding.LINEAR16,
        sample_rate_hertz=sample_rate,
        language_code=language)

    return client.recognize(config, audio)

if __name__ == '__main__':
    sample_rate = 16000

    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument('-l', '--language', required=True,
                        help="Phrase language, e.g. \"en-US\"")
    parser.add_argument('text', help="Phrase text")

    args = parser.parse_args()

    audio = synthesize_text(args.language, sample_rate, args.text)
    result = recognize_speech(args.language, sample_rate, audio)

    print(result)



