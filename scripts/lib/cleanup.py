import re
import html

# Replace '_' with '-' in language
# This is the language format used by Google STT and by extension Dictio application
def formatSourceLanguage(lang):
    return lang.replace('_', '-')

# Cut off country part
# This is the format used for translations since country doesn't really matter
def formatTranslationLanguage(lang):
    return lang.split('_')[0]

# Clean up text visible to the user:
# - Unescape html entities
# - Remove tags and text in parentheses and brackets
# - Trim whitespace
# - Capitalize first letter
def formatText(text):
    text = html.unescape(text)

    text = re.sub('<[^<]+?>', '', text)
    text = re.sub('\([^)]+?\)', '', text)
    text = re.sub('\[[^]]+?\]', '', text)

    return text.strip().capitalize()
