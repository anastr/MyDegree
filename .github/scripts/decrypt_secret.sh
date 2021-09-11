#!/bin/sh

# Decrypt the file
mkdir $HOME/app
# --batch to prevent interactive command
# --yes to assume "yes" for questions
gpg --quiet --batch --yes --decrypt --passphrase="$GOOGLE_JSON_SECRET_PASSPHRASE" \
--output $HOME/app/google-services.json google-services.json.gpg