#!/bin/bash

# First argument is the path to anki archive (.apkg)
path=$(realpath "$1")

# Second argument is the index of column to be extracted in notes data
column_index="$2" 

workdir=$(mktemp -d)

# Change to working directory
cd "$workdir"

# Extract the database file from the archive
unzip "$path" collection.anki2 > /dev/null && \
    # Select the flds column in notes table
    sqlite3 collection.anki2 "SELECT flds FROM notes" | \
	# Extract the N-th field from 0x1f (\31) separated list
	cut -d $'\x1f' -f "$column_index"

# Clean up the working directory
rm -rf "$workdir"
