package com.chatapp.websocket;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

public class WebSocketConfigurator
        extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(
            ServerEndpointConfig config,
            HandshakeRequest request,
            HandshakeResponse response
    ) {

        HttpSession httpSession =
                (HttpSession) request
                        .getHttpSession();

        if (httpSession == null) {
            return;
        }

        Object userId =
                httpSession.getAttribute("userId");

        if (userId != null) {

            config.getUserProperties().put(
                    "userId",
                    userId
            );
        }
    }
}