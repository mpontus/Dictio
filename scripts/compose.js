#!/usr/bin/env node
const fs = require("fs");
const path = require("path");

const LIMIT_PER_FILE = 100;

const categories = ["word", "phrase"];
const languages = process.argv.slice(2);

const genId = (() => {
  let id = 0;

  return () => id++;
})();

const absolutePath = filename => path.resolve(process.cwd(), filename);

const fileExists = filename => fs.existsSync(absolutePath(filename));

const readFile = filename =>
  fs
    .readFileSync(absolutePath(filename), "utf8")
    .split("\n")
    .slice(0, LIMIT_PER_FILE);

// Replace underscores in language with minus signs
const fixLang = lng => lng.replace("_", "-");

// Remove country part of the language
const transLang = lng => lng.split("_")[0];

// Capitzlize first letter of a string
const capitalize = text => text && text[0].toUpperCase() + text.slice(1);

// Remove tags and stuff in parentheses
const cleanup = text =>
  text
    .trim()
    .replace(/\(.*?\)/g, "")
    .replace(/\<.*?\>/g, "")
    .replace(/\[.*?\]/g, "");

function* collectPrompts() {
  for (const lng of languages) {
    for (const cat of categories) {
      const trans = {};

      for (const transLng of languages) {
        const filename = `${cat}-${lng}-${transLng}.txt`;

        if (!fileExists(filename)) {
          continue;
        }

        trans[transLng] = readFile(filename);
      }

      for (let i = 0; i < trans[lng].length; ++i) {
        const result = {
          id: genId(),
          text: capitalize(cleanup(trans[lng][i])),
          language: fixLang(lng),
          category: cat,
          translations: {}
        };

        for (const transLng of languages) {
          if (lng === transLng || !trans[transLng]) {
            continue;
          }

          result.translations[transLang(transLng)] = capitalize(
            cleanup(trans[transLng][i])
          );
        }

        yield result;
      }
    }
  }
}

process.stdout.write(
  JSON.stringify(
    [...collectPrompts()]
      // Leave non-empty prompts
      .filter(prompt => prompt.text)
      // Leave prompts with non-empty translation
      .filter(
        prompt =>
          Object.values(prompt.translations).filter(translation => translation)
            .length
      ),
    null,
    2
  )
);
