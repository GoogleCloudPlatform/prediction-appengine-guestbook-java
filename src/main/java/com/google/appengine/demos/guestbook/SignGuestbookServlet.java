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
import com.google.api.services.prediction.model.Input;
import com.google.api.services.prediction.model.Output;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.DatastoreServiceFactory;


import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * This servlet provides an endpoint to create new Guestbook posts.
 *
 * It uses the Prediction API to predict language and sentiment.
 */
public class SignGuestbookServlet extends HttpServlet {

    /**
     * Main endpoint to create a new guestbook post.
     *
     * This method handles new Guestbook POST requests. It uses the
     * Prediction API to detect the language, detect the sentiment,
     * and then stores the post and it's predictions in Cloud Datastore
     * before redirecting back to the main view.
     * @param req Servlet request
     * @param resp Servlet response
     * @throws IOException Network error with the Prediction API.
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();

        String guestbookName = req.getParameter("guestbookName");
        Key guestbookKey = KeyFactory.createKey("Guestbook", guestbookName);
        String content = req.getParameter("content");
        Date date = new Date();
        Prediction predictionClient = PredictionClientFactory
                .getPredictionClient();
        boolean positive = getSentiment(predictionClient, content);
        String language = getLanguage(predictionClient, content);
        Entity greeting = new Entity("Greeting", guestbookKey);
        if (user != null) {
            greeting.setProperty("userNickname", user.getNickname());
        } else {
            greeting.setProperty("userNickname", "Anonymous");
        }
        greeting.setProperty("date", date);
        greeting.setProperty("content", content);
        greeting.setProperty("positive", positive);
        greeting.setProperty("language", language);

        DatastoreService datastore = DatastoreServiceFactory
                .getDatastoreService();
        datastore.put(greeting);

        String redirectUrl =
                "/guestbook.jsp?guestbookName=" + guestbookName;
        redirectUrl = resp.encodeRedirectURL(redirectUrl);
        resp.sendRedirect(redirectUrl);
    }

    /**
     * Uses the Prediction client to determine whether a given message was
     * positive or negative sentiment.
     * @param prediction The client to the Prediction API.
     * @param content The string representing the message we want to determine
     *                the sentiment of.
     * @return True if the sentiment was determined to be positive, false
     * otherwise.
     * @throws IOException Thrown on network exception with Prediction API
     */
    private boolean getSentiment(Prediction prediction, String content)
            throws IOException {
        Preconditions.checkNotNull(prediction);
        Preconditions.checkNotNull(content);

        Input input = new Input();
        Input.InputInput inputInput = new Input.InputInput();
        inputInput.set("csvInstance", Lists.newArrayList(content));
        input.setInput(inputInput);
        Output result = prediction.hostedmodels().predict("414649711441",
                "sample.sentiment", input).execute();
        String outputStr = result.getOutputLabel();
        return outputStr.equals("positive");
    }

    /**
     * Uses the Prediction API client to determine the language of a given
     * guestbook post.
     * @param prediction The Prediction API client
     * @param content The string representing the message we want to detect the
     *                language of
     * @return The language the Prediction API has predicted the message is in
     * @throws IOException Thrown on network exception connecting to
     *                     Prediction API
     */
    private String getLanguage(Prediction prediction, String content)
            throws IOException {
        Preconditions.checkNotNull(prediction);
        Preconditions.checkNotNull(content);

        Input input = new Input();
        Input.InputInput inputInput = new Input.InputInput();
        inputInput.set("csvInstance", Lists.newArrayList(content));
        input.setInput(inputInput);
        Output result = prediction.trainedmodels().predict(Utils.getProjectId(),
                Constants.MODEL_ID, input).execute();
        return result.getOutputLabel();
    }

}
