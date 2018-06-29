const fs = require("fs");

const languages = ["en-US", "ru-RU", "sv-SE"];
const categories = ["word", "phrase"];

function* collectPrompts() {
  let id = 0;

  for (let language of languages) {
    for (let category of categories) {
      const filename = `${category}_${language}.txt`;

      yield* fs
        .readFileSync(filename, "utf8")
        .split("\n")
        .map(text => ({
          id: `${id++}`,
          text,
          language,
          category,
          translations: {}
        }));
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
