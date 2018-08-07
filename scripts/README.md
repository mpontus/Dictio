# Dictio Utility Scripts

This directory contains some scripts useful in data extraction.




## Scripts

#### `anki-extract-data.sh FILENAME.apkg N`

> Extract contents from Anki deck

Anki decks are a good source of words and phrases. Each deck is a ZIP archive with media files and an SQLite database. 

Fields of each card are stored in one of the tables as a string separated by `\x1f` character.

This script extracts the content of the `flds` column, splits it by `\x1f` and prints the content `N`-th field for each row.

It takes some guesswork to find the index of the field you are looking for.

#### `real-validator.py -r [ROUNDTRIP-FILE] -l [LIMIT] [-c]`

> Validates that prompt can be matched if spoken correctly.

Validates prompts by running them though Google Speech Recognition after speech synthesis and matching them using Dictio's matching algorithm.

##### Arguments:

- `-r FILENAME` A filename to store round-trip results.
  
  To avoid unnecessary requests to the speech recongition service, after each request the results are saved into this file.
  
  Prior to request, this file is checked first and if it contains saved results then no request is made.
  
  The file will be created if it doesn't already exist.

- `-l LIMIT` Limit the number of requests to the speech recognition service.

	The number of reuests to speech recognition service will be limitted by this value.
	
	Results found in the round-trip table do not count toward this limit.
	
	After the limit is exhausted, only the prompts saved to the round trip table will be considered, unless the `-k` option is set.

- `-k` Consider prompts which can be verified due to limit exhausted as valid.

	After is exhausted, prompts which can not be verified by exsting records in the round trip table will be included in the output.

	This option allows you to create a file which is guaranteed to start with verified prompts, but later prompts may or may not be verified.
	
#### `create-json.py --translation [TRANSLATIONS.csv] [PROMPTS.csv]` 

Create JSON file from prompts and translations.

Prompts file should contain following columns:

- Prompt language (e.g. `en_US` or `sv_SK`)
- Category: `word` or `phrase`
- Difficulty: An integer representing relative difficulty of the prompt
- Prompt text

Translations file should contain following columns:

- Source language
- Source text
- Translation language
- Translation text 

#### `interpose.py [FILE...]`

> Interposes specified files line by line

When making a queue for `real-validator.py` its useful to mix up the prompts of different languages and categories to appraoch a more-or-less equal distribution.

## Process

This is what the process of collecting prompts for a given language looks like.

For this example I will be using the process of collecting words for the german language.

1. Find anki decks which contains individual German words and their english translations.
   For example this: https://ankiweb.net/shared/info/653061995

2. Download the deck and use anki-extract-data to extract the contents of the column corresponding to individual words.

   ```bash
   anki-extract-data.sh Deutsch_4000_German_Words_by_Frequency.apkg 1 > word-de_DE-de_DE.txt
   ```

3. Extract the contents of the column with english translations   

   ```bash
   anki-extract-data.sh Deutsch_4000_German_Words_by_Frequency.apkg 6 > word-de_DE-en_US.txt
   ```

4. Open `word-de_DE-de_DE.txt` in LibreOffice Calc as CSV file without delimiter. If the contents are clean, this should not cause any problems. Otherwise, remove the quotes on the problematic line.

5. Add 3 colums at the beginning corresponding to language, category, and difficulty.

6. Fill language column with "de_DE", category column with "word". Fill difficulty with incrementing numbers starting with 1.

7. Save the file as `prompts/word-de_DE.csv`

8. Open `word-de_DE-en_US.txt` in LibreOffice Calc in order to create a translations file.

9. Add three columns at the begging, corresponding to source language, source text, and translation language.

10. Fill the first column with "de_DE", third column with "en_US", and copy the contents of the last column of `word-de_DE.csv` into the second column of translations file.

11. Save the file as `translations/word-de_DE-en_US.csv`.

12. Run `cat prompts/word-de_DE.csv | real-validator -r roundtrip.csv -l 100 > verified/word-de_DE.csv` to generate a subset of words which can be recognized by google STT from synthesized speech and matched correctly by phrase matching algorithm used in Dictio.

13. Repeat the process until you are satisfied with the amount of prompts in `verified/word-de_DE.csv`. At each iteration at most 100 requests will be made which amount to $0.6 in Google Cloud Speech costs.

14. At this point you have two files which will be used to create final JSON: `translations/word-de_DE-en_US.csv` and `verified/word-de_DE.csv`. Repeat the process for other languages and prompt categories.

15. Once you have collected prompts and translations for every language and category, combine them into two CSV files:
	```
	cat translations/*.csv > translations.csv
	cat verified/*.csv > prompts.csv
	```
	
16. Create JSON file containing all the prompts: `create-json.py --trans translations.csv prompts.csv > prompts.json`.

17. Update `backend/src/main/webapp/WEB-INF/prompts.json` with newly created file and redeploy the backend.

