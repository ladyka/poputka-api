#!/bin/bash

# Pre-commit hook to check for wildcard imports (.*;) in Java files

# Get the list of changed files in this commit
CHANGED_FILES=$(git diff --cached --name-only)

# Filter for Java files only
JAVA_FILES_CHANGED=$(echo "$CHANGED_FILES" | grep '\.java$')

if [ -n "$JAVA_FILES_CHANGED" ]; then
    echo "Checking for wildcard imports (*.*) in changed Java files..."
    
    # Check each changed Java file
    FOUND_WILDCARD_IMPORTS=0
    for file in $JAVA_FILES_CHANGED; do
        if grep -q "import .*\.\\*;" "$file"; then
            echo "ERROR: File $file contains wildcard imports (import .*;)"
            echo "Wildcard imports should be avoided."
            echo "Commit blocked due to forbidden wildcard imports."
            FOUND_WILDCARD_IMPORTS=1
        fi
    done
    
    if [ $FOUND_WILDCARD_IMPORTS -eq 1 ]; then
        echo "Commit blocked. Please remove wildcard imports from Java files."
        exit 1
    else
        echo "No wildcard imports found in Java files. Commit allowed."
        exit 0
    fi
else
    echo "No Java files changed in this commit."
    exit 0
fi
