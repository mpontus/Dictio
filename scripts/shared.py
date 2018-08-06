# Replace '_' with '-' in locale specifying prompt language
def formatSourceLanguage(lang):
    return lang.replace('_', '-')

# Cut off country part
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
