#!/bin/sh

mkdir $HOME/app

gpg --quiet --batch --yes --decrypt --passphrase="$GOOGLE_JSON_SECRET_PASSPHRASE" \
--output $HOME/app/google-services.json $HOME/.github/scripts/google-services.json.gpg