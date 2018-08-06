# Scripts

Scripts in this directory are used for creating a prompt list from Anki decks.



## Useful commands

Alias mrli to work with csv with implicit header:

```
alias mlri="mlr  --csv --implicit-csv-header --headerless-csv-output"
```


Change the order of fields in CSV file:

```
mlr --csv --implicit-csv-header --headerless-csv-output cut -o -f 2,3,1,4 prompts.csv
```

Pick 300 random prompts from a file and output them in sorted order:

```
shuf $file | head -300 | mlr  --csv --implicit-csv-header --headerless-csv-output sort -f 1,2 -n 3
```





Extract `language` and `text` from CSV file in `prompts` format.

```


mlr --csv --implicit-csv-header cut -f 1,3 prompts.csv
```
