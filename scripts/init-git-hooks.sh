#!/bin/bash

# Script to initialize Git hooks from the scripts directory
# This script copies hook scripts to the .git/hooks directory and makes them executable

# Check if we're in a Git repository
if [ ! -d ".git" ]; then
    echo "Error: This script must be run from the root of a Git repository."
    exit 1
fi

# Create the hooks directory if it doesn't exist
mkdir -p .git/hooks

# Copy and set up the entity import check hook
if [ -f "scripts/check-entity-imports.sh" ]; then
    echo "Setting up entity import check hook..."
    cp scripts/check-entity-imports.sh .git/hooks/pre-commit
    chmod +x .git/hooks/pre-commit
    echo "Entity import check hook installed successfully."
else
    echo "Warning: check-entity-imports.sh not found in scripts directory."
fi

# Copy and set up the wildcard import check hook
if [ -f "scripts/check-wildcard-imports.sh" ]; then
    echo "Setting up wildcard import check hook..."
    cp scripts/check-wildcard-imports.sh .git/hooks/pre-commit-wildcard
    chmod +x .git/hooks/pre-commit-wildcard
    echo "Wildcard import check hook installed successfully."
else
    echo "Warning: check-wildcard-imports.sh not found in scripts directory."
fi

echo "Git hooks initialization complete!"
echo "You can now use Git as usual, and the hooks will automatically check your commits."
