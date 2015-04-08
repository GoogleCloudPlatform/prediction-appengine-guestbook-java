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

/**
 * Configure the constants used to interact with Google Cloud and Prediction
 * API.
 */
public class Constants {

    /**
     * Private constructor for utility class.
     */
    private Constants() {

    }

    /**
     * Cloud storage location of the model used to train the language
     * prediction model.
     */
    public static final String DATA_FILE =
            "your-cloud-storage-bucket/language_id.txt";

    /**
     * Name of the model to train for language detection.
     */
    public static final String MODEL_ID = "your-model-id";

    /**
     * This is your numeric project id, found in the Cloud console.
     */
    public static final String PROJECT_ID = "your-numeric-project-id";

}
