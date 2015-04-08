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

import com.google.api.services.prediction.Prediction;
import com.google.api.services.prediction.model.Insert;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet which, when hit with a POST request, will train our model using the
 * prediction API.
 */
public class TrainModelServlet extends HttpServlet {

    /**
     * This endpoint serves to check on the status of a model currently
     * being trained. In this example, the model is being used to predict
     * the language, and once it's ready then language detection can be used.
     * @param req Servlet request
     * @param resp Servlet response
     * @throws IOException Thrown on network error connecting to Prediction API.
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Prediction predictionClient = PredictionClientFactory
                .getPredictionClient();
        Insert insert = new Insert();
        insert.set("id", Constants.MODEL_ID);
        insert.set("storageDataLocation", Constants.DATA_FILE);
        predictionClient.trainedmodels().insert(Utils.getProjectId(), insert);
        resp.sendRedirect("/checkmodel");
    }
}
