#!/usr/bin/env python3
# Validate that a phrase can be recognized by running it through TTS

import os
import sys
import csv
import gtts
from google.cloud import speech
from io import BytesIO
from pydub import AudioSegment
from lib.cleanup import formatSourceLanguage, formatText

# Generate key by combining language and text
def getkey(language, text):
    return language + text

class TextToSpeech:
    def __init__(self):
        self.langs = gtts.lang.tts_langs()

    def synthesize(self, language, text):
        lang = formatSourceLanguage(language).split('-')[0]

        # if not lang in self.langs:
        #     lang = lang.split('-')[0]
            
        if not lang in self.langs:
            raise Exception("Unsupported language: %s"%language)

        tts = gtts.gTTS(text, lang)

        with BytesIO() as fp_mp3, BytesIO() as fp_wav:
            tts.write_to_fp(fp_mp3)
            fp_mp3.seek(0)

            AudioSegment.from_mp3(fp_mp3).export(fp_wav, format="wav")
            fp_wav.seek(0)

            return fp_wav.read()

class SpeechToText:
    def __init__(self):
        self.client = speech.SpeechClient()

    def recognize(self, language, content):
        lang = formatSourceLanguage(language)
        config = speech.types.RecognitionConfig(
            encoding=speech.enums.RecognitionConfig.AudioEncoding.LINEAR16,
            language_code=lang)
        audio = speech.types.RecognitionAudio(content=content)
        response = self.client.recognize(config, audio)

        results = []

        for result in response.results:
            for alternative in result.alternatives:
                results.append(alternative.transcript)

        return results

class RoundtripTable:
    def __init__(self, filename):
        self.filename = filename
        self.map = {}

        if os.path.isfile(filename):
            with open(filename, "r") as file:
                for language, text, *results in csv.reader(file):
                    key = getkey(language, text)
                    self.map[key] = results

    def get(self, language, text):
        key = getkey(language, text)

        if not key in self.map:
            return None

        return self.map[key]

    def put(self, language, text, results):
        key = getkey(language, text)
        self.map[key] = results

        with open(self.filename, 'a') as file:
            csv.writer(file).writerow([language, text, *results])


def match_any(language, text, results):
    from lib.matcher import phraseMatcherFactory

    phraseMatcher = phraseMatcherFactory.create(language, text)

    for result in results:
        if phraseMatcher.match(result):
            return True

    return False

if __name__ == '__main__':
    import argparse

    parser = argparse.ArgumentParser()
    parser.add_argument('-r', '--roundtrip', required=True,
                        help="Roundtrip results CSV file")
    parser.add_argument('-l', '--limit', required=True,
                        help="Limit the number of requests to STT service")
    parser.add_argument('-k', '--keepgoing', action='store_true',
                        help="Assume records which can't be validate due to limit as verified")
    args = parser.parse_args()

    output = csv.writer(sys.stdout)
    limit = int(args.limit)
    tts = TextToSpeech()
    stt = SpeechToText()
    roundtrip_table = RoundtripTable(args.roundtrip)

    for row in csv.reader(sys.stdin):
        language, category, difficulty, text = row

        # CSV are often exported from Anki and may include tags, and unnnecessary characters
        # They will be cleaned up before shipped to Dictio to be able to used in speech synthesis
        text = formatText(text)

        results = roundtrip_table.get(language, text)

        if results is None:
            if limit <= 0:
                if args.keepgoing:
                    output.writerow(row)
                continue
                    
            limit -= 1
            audio = tts.synthesize(language, text)
            results = stt.recognize(language, audio)
            roundtrip_table.put(language, text, results)

        if match_any(formatSourceLanguage(language), text, results):
            output.writerow(row)
