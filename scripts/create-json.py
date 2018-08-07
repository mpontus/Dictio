#!/usr/bin/env python3
import argparse
import csv
import json
import re
import html
from lib.cleanup import formatSourceLanguage, formatTranslationLanguage, formatText

def getKey(language, text):
    return language + text

parser = argparse.ArgumentParser()
parser.add_argument('--trans', help="Translation table (CSV, cols: srcLng, text, transLng, transText)")
parser.add_argument('prompts', help="Prompts file (CSV, cols: difficulty, language, category, text)")

args = parser.parse_args()

trans = {}

if args.trans is not None:
    with open(args.trans) as tf:
        for srcLng, text, transLng, transText in csv.reader(tf):
            key = srcLng + text

            trans.setdefault(key, {})

            trans[key][formatTranslationLanguage(transLng)] = formatText(transText)

prompts = []

id = 0
with open(args.prompts) as tf:
    for lang, category, difficulty, text in csv.reader(tf):
        key = getKey(lang, text)

        prompt = {
            "id": id,
            "difficulty": int(difficulty),
            "language": formatSourceLanguage(lang),
            "category": category,
            "text": formatText(text),
            "translations": trans[key] if key in trans else {}
        }

        prompts.append(prompt)

        id += 1

print(json.dumps(prompts, indent=4))
