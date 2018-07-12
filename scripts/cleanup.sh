#!/bin/bash

# Removes tags and string in parenthesis and brackets
sed -re 's/\(.*?\)//g' -e 's/\[.*?\]//g' -e 's/<.*?>//g'

