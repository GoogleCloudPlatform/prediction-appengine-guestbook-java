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
import com.google.api.services.prediction.model.Insert2;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet that when hit with a GET request will return the status of the model
 * being trained. Typically this would be called after TrainModelServlet has
 * started the training.
 */
public class CheckModelServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Prediction predictionClient = PredictionClientFactory
                .getPredictionClient();
        Insert2 insert2 = predictionClient.trainedmodels()
                .get(Constants.PROJECT_ID, Constants.MODEL_ID).execute();
        String status = insert2.getTrainingStatus();
        resp.setContentType("text/plain");
        resp.getWriter().println(status);
    }
}
