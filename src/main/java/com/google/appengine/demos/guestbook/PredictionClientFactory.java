/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.appengine.demos.guestbook;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.prediction.Prediction;
import com.google.api.services.prediction.PredictionScopes;

import java.io.IOException;

/**
 * This factory provides the Prediction Client and allows easier mocking for
 * testing.
 */
public class PredictionClientFactory {

    /**
     * Name of the application from the Cloud console provided to the
     * Prediction API.
     */
    private static final String APPLICATION_NAME =
            "google-cloud-prediction-appengine-sample/1.0";

    /**
     * Google Prediction API Client.
     */
    private static Prediction mPrediction;

    /**
     * Private constructor for utility class.
     */
    private PredictionClientFactory() {

    }

    /**
     * This method lets test classes set a mock client to be used instead.
     * @param prediction The prediction client to be used, typically a mock.
     */
    static void setPrediction(Prediction prediction) {
        mPrediction = prediction;
    }

    static {
        try {
            GoogleCredential credential =
                    GoogleCredential.getApplicationDefault();
            HttpTransport httpTransport = Utils.getDefaultTransport();
            if (credential.createScopedRequired()) {
                credential = credential.createScoped(PredictionScopes
                        .all());
            }
            // Use custom HttpRequestInitializer for automatic
            // retry upon failures.
            HttpRequestInitializer initializer =
                    new RetryHttpInitializerWrapper(credential);
            mPrediction = new Prediction.Builder(httpTransport,
                    Utils.getDefaultJsonFactory(), initializer)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get an instance of the singleton Prediction API client.
     * @return A prediction API client.
     */
    public static Prediction getPredictionClient() {
        return mPrediction;
    }
}
