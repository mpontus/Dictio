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

const fixLang = lng => lng.replace("_", "-");

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
          language: fixLang(lng),
          type: cat,
          text: trans[lng][i],
          translations: {}
        };

        for (const transLng of languages) {
          if (lng === transLng || !trans[transLng]) {
            continue;
          }

          result.translations[fixLang(transLng)] = trans[transLng][i];
        }

        yield result;
      }
    }
  }
}

process.stdout.write(
  JSON.stringify(
    {
      version: 1,
      prompts: [...collectPrompts()]
    },
    null,
    2
  )
);
