/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
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
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class SignGuestbookServletTest {

    private SignGuestbookServlet signGuestbookServlet;

    private Prediction.Trainedmodels mockTrainedModels;

    private Prediction.Hostedmodels mockHostedModel;

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig())
                    .setEnvIsLoggedIn(true)
                    .setEnvAuthDomain("localhost")
                    .setEnvEmail("test@localhost");

    @Before
    public void setupSignGuestBookServlet() throws IOException {
        helper.setUp();
        signGuestbookServlet = new SignGuestbookServlet();
        PredictionClientFactory.setPrediction(getMockPredictionClient());
    }

    @After
    public void tearDownHelper() {
        helper.tearDown();
    }

    private Prediction getMockPredictionClient() throws IOException {
        Prediction predictionClient = mock(Prediction.class);
        PredictionClientFactory.setPrediction(predictionClient);

        mockHostedModel =
                mock(Prediction.Hostedmodels.class);
        Prediction.Hostedmodels.Predict mockHostedPrediction =
                mock(Prediction.Hostedmodels.Predict.class);
        Output mockHostedOutput = new Output();

        when(predictionClient.hostedmodels()).thenReturn(mockHostedModel);
        when(mockHostedModel.predict(anyString(), anyString(),
                Mockito.<Input>any())).thenReturn(mockHostedPrediction);
        when(mockHostedPrediction.execute()).thenReturn(mockHostedOutput);
        mockHostedOutput.setOutputLabel("positive");

        mockTrainedModels =
                mock(Prediction.Trainedmodels.class);
        Prediction.Trainedmodels.Predict mockTrainedPrediction =
                mock(Prediction.Trainedmodels.Predict.class);
        Output mockTrainedOutput = new Output();

        when(predictionClient.trainedmodels()).thenReturn(mockTrainedModels);
        when(mockTrainedModels.predict(anyString(), anyString(),
                Mockito.<Input>any())).thenReturn(mockTrainedPrediction);
        when(mockTrainedPrediction.execute()).thenReturn(mockTrainedOutput);
        mockTrainedOutput.setOutputLabel("english");

        return predictionClient;
    }

    @Test
    public void testDoPost() throws IOException, EntityNotFoundException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);


        String guestbookName = "TestGuestbook";
        String testContent = "Test Content";
        String EXPECTED_URL = "/guestbook.jsp?guestbookName=TestGuestbook";

        when(request.getParameter("guestbookName")).thenReturn(guestbookName);
        when(request.getParameter("content")).thenReturn(testContent);
        when(response.encodeRedirectURL(EXPECTED_URL)).thenReturn
                (EXPECTED_URL);


        Date priorToRequest = new Date();
        signGuestbookServlet.doPost(request, response);
        Date afterRequest = new Date();

        verify(response).sendRedirect(EXPECTED_URL);

        User currentUser = UserServiceFactory.getUserService().getCurrentUser();

        Entity greeting = DatastoreServiceFactory.getDatastoreService()
                .prepare(new Query()).asSingleEntity();

        assertEquals(guestbookName, greeting.getKey().getParent().getName());
        assertEquals(testContent, greeting.getProperty("content"));
        assertEquals(currentUser.getUserId(), greeting.getProperty("userId"));

        Date date = (Date) greeting.getProperty("date");
        assertTrue("The date in the entity [" + date +
                        "] is prior to the request being performed",
                priorToRequest.before(date) || priorToRequest.equals(date));
        assertTrue("The date in the entity [" + date +
                        "] is after to the request completed",
                afterRequest.after(date) || afterRequest.equals(date));

        verify(mockTrainedModels).predict(anyString(), anyString(),
                Mockito.<Input>any());
        verify(mockHostedModel).predict(anyString(), anyString(), Mockito
                .<Input>any());
    }
}
