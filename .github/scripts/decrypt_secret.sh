#!/bin/sh

mkdir ./app

gpg --quiet --batch --yes --decrypt --passphrase="$GOOGLE_JSON_SECRET_PASSPHRASE" \
--output ./app/google-services.json ./.github/scripts/google-services.json.gpg