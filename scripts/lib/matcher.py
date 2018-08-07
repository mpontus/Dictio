# Phrase matcher reimplemented in python
import re
import sys

class PhraseMatcherFactory:
    def __init__(self, defaultTokenizer, tokenizerByLanguage):
        self.defaultTokenizer = defaultTokenizer
        self.tokenizerByLanguage = tokenizerByLanguage

    def create(self, language, text):
        return PhraseMatcher(self.getTokenizer(language), text)

    def getTokenizer(self, language):
        if not language in self.tokenizerByLanguage:
            language = language.split('-')[0]
            
        if language in self.tokenizerByLanguage:
            return self.tokenizerByLanguage[language]

        return self.defaultTokenizer

class PhraseMatcher:
    def __init__(self, tokenizer, text):
        self.tokenizer = tokenizer
        self.tokens = list(tokenizer.tokenize(text))

    def match(self, text):
        other_tokens = list(self.tokenizer.tokenize(text))

        try:
            start = other_tokens.index(self.tokens[0])

            if len(self.tokens) > len(other_tokens) - start:
                return False

            for original, candidate in zip(self.tokens, other_tokens):
                if original.lower() != candidate.lower():
                    return False

        except ValueError:
            return False

        return True

class BasicTokenizer:
    WORD_PATTERN = re.compile(r"([\u3040-\u30ff\uff00-\uffef\u4e00-\u9faf]|[\w'’]+)")

    def tokenize(self, text):
        return self.WORD_PATTERN.findall(text.lower())


class RussianTokenizer:
    def __init__(self, baseTokenizer):
        self.baseTokenizer = baseTokenizer

    def tokenize(self, text):
        for word in self.baseTokenizer.tokenize(text):
            yield word.replace("ё", "е")
        
            
defaultTokenizer = BasicTokenizer()
tokenizerByLanguage = {
    "ru": RussianTokenizer(defaultTokenizer),
}
phraseMatcherFactory = PhraseMatcherFactory(defaultTokenizer, tokenizerByLanguage)

if __name__ == "__main__":
    language, original, candidate = sys.argv[1:4]

    phraseMatcher = phraseMatcherFactory.create("ru-RU", original)

    a = phraseMatcher.match(candidate)

    print(a)
