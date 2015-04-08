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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpBackOffUnsuccessfulResponseHandler;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpBackOffIOExceptionHandler;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.client.util.Sleeper;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * RetryHttpInitializerWrapper will automatically retry upon RPC
 * failures, preserving the auto-refresh behavior of the Google
 * Credentials.
 */
public class RetryHttpInitializerWrapper implements HttpRequestInitializer {

    /**
     * Logger.
     */
    private static final Logger LOG =
            Logger.getLogger(RetryHttpInitializerWrapper.class.getName());

    /**
     * Intercepts the request for filling in the "Authorization"
     * header field, as well as recovering from certain unsuccessful
     * error codes wherein the Credential must refresh its token for a
     * retry.
     */
    private final Credential wrappedCredential;

    /**
     * A sleeper; you can replace it with a mock in your test.
     */
    private final Sleeper sleeper;

    /**
     * Creates an Retry wrapper.
     * @param wrappedCredential Google Application credentials to wrap.
     */
    public RetryHttpInitializerWrapper(Credential wrappedCredential) {
        this(wrappedCredential, Sleeper.DEFAULT);
    }

    /**
     * Use only for testing.
     * @param sleeper Used to make the current thread sleep
     * @param wrappedCredential Decorator pattern so credential used retries
     *                          HTTP.
     */
    RetryHttpInitializerWrapper(
            Credential wrappedCredential, Sleeper sleeper) {
        this.wrappedCredential = Preconditions.checkNotNull(wrappedCredential);
        this.sleeper = sleeper;
    }

    /**
     * Initializes a API Client request to retry on failure.
     * @param request Http servlet request
     */
    @Override
    public void initialize(HttpRequest request) {
        final int MINUTES_IN_MILLISECONDS = 60000;
        final int READ_TIMEOUT_IN_MINUTES = 2;
        request.setReadTimeout(READ_TIMEOUT_IN_MINUTES
                * MINUTES_IN_MILLISECONDS);
        final HttpUnsuccessfulResponseHandler backoffHandler =
                new HttpBackOffUnsuccessfulResponseHandler(
                        new ExponentialBackOff())
                        .setSleeper(sleeper);
        request.setInterceptor(wrappedCredential);
        request.setUnsuccessfulResponseHandler(
                new HttpUnsuccessfulResponseHandler() {
                    @Override
                    public boolean handleResponse(
                            HttpRequest request,
                            HttpResponse response,
                            boolean supportsRetry) throws IOException {
                        if (wrappedCredential.handleResponse(
                                request, response, supportsRetry)) {
                            // If credential decides it can handle it,
                            // the return code or message indicated
                            // something specific to authentication,
                            // and no backoff is desired.
                            return true;
                        } else if (backoffHandler.handleResponse(
                                request, response, supportsRetry)) {
                            // Otherwise, we defer to the judgement of
                            // our internal backoff handler.
                            LOG.info("Retrying "
                                    + request.getUrl().toString());
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
        request.setIOExceptionHandler(
                new HttpBackOffIOExceptionHandler(new ExponentialBackOff())
                        .setSleeper(sleeper));
    }
}

