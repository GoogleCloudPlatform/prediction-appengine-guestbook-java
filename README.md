![status: inactive](https://img.shields.io/badge/status-inactive-red.svg)

This project is no longer actively developed or maintained.

For new work on this check out [Cloud AutoML](https://cloud.google.com/automl/)

# prediction-appengine-guestbook-python

## Phases
This project is associated with this tutorial:

https://cloud.google.com/appengine/articles/prediction_service_accounts

Each directory represents a phase in the tutorial. phase1 is the basic skeleton, phase2
represents the project after Step 4 of the tutorial (sentiment analysis), phase3 represents
the final code product of the tutorial and includes language detection.

## Prerequisites

- Install Java and Maven

- Install [Google Cloud SDK](https://cloud.google.com/sdk/)

- Change into the project directory

  $ mvn clean install

## Register your application

- If you don't have a project, go to [Google Developers Console][1]
  and create a new project.

- Enable the "Prediction" API under "APIs & auth > APIs > Google Cloud APIs"

## Edit Constants.java

- DATA_FILE
- MODEL_ID
- PROJECT_ID

## Run Locally

$ mvn appengine:devserver

## Deploy

$ mvn appengine:update

[1]: https://console.developers.google.com/project
