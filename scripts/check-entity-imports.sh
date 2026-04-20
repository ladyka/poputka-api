#!/bin/bash

# Pre-commit hook to check for "by.ladyka.poputka.data.entity" imports
# in files within src/main/java/by/ladyka/poputka/controllers directory

# Get the list of changed files in this commit
CHANGED_FILES=$(git diff --cached --name-only)

# Check if any changed files are in the controllers directory
CONTROLLERS_CHANGED=$(echo "$CHANGED_FILES" | grep '^src/main/java/by/ladyka/poputka/controllers/')

if [ -n "$CONTROLLERS_CHANGED" ]; then
    echo "Checking for 'by.ladyka.poputka.data.entity' imports in changed controller files..."

    # Check each changed controller file
    FOUND_IMPORTS=0
    for file in $CONTROLLERS_CHANGED; do
        if grep -q "by.ladyka.poputka.data.entity" "$file"; then
            echo "ERROR: File $file contains 'by.ladyka.poputka.data.entity' import"
            echo "This import should be avoided in controller files."
            echo "Commit blocked due to forbidden imports."
            FOUND_IMPORTS=1
        fi
    done

    if [ $FOUND_IMPORTS -eq 1 ]; then
        echo "Commit blocked. Please remove entity imports from controller files."
        exit 1
    else
        echo "No entity imports found in controller files. Commit allowed."
        exit 0
    fi
else
    echo "No controller files changed in this commit."
    exit 0
fi
