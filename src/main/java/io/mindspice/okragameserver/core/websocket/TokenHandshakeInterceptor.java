package io.mindspice.okragameserver.core.websocket;

import io.mindspice.okragameserver.core.GameServer;
import io.mindspice.okragameserver.core.HttpServiceClient;
import io.mindspice.okragameserver.game.player.Player;
import io.mindspice.okragameserver.util.Log;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.http.*;
import org.springframework.web.socket.*;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.server.HandshakeInterceptor;


@Component
public class TokenHandshakeInterceptor implements HandshakeInterceptor {

    private final HttpServiceClient serviceClient;
    private final Map<Integer, Player> playerTable;

    public TokenHandshakeInterceptor(
            @Qualifier("httpServiceClient") HttpServiceClient serviceClient,
            @Qualifier("playerTable") Map<Integer, Player> playerTable) {
        this.serviceClient = serviceClient;
        this.playerTable = playerTable;
    }

    @PostConstruct
    public void init() {
        Log.SERVER.debug(this.getClass(), "TokenHandshakeInterceptor Init");
        Log.SERVER.debug(this.getClass(), "HttpServiceClient:" + serviceClient);
    }

    //    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {
        // take the users token and validate it with the auth service, if the user has been authenticated their
        // player id will be return, or else it will return -1, or -2 in the case of an error
        Log.SERVER.debug(this.getClass(), "handshake init");

        String authToken = getTokenFromUri(request.getURI());

        if (authToken == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            // TODO Ip Log
            return false;
        }

        int playerId = serviceClient.doAuth(authToken);
        Log.SERVER.debug(this.getClass(), String.valueOf(playerId));

        if (playerId == -1) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            // TODO ip log
            return false;
        }
        if (playerId == -2) {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return false;
        }

        if (playerTable.containsKey(playerId)) {
            Player player = playerTable.get(playerId);
            if (player.isConnected()) {
                player.disconnect();
                Log.ABUSE.info("Player reconnected without disconnection first"
                        + " | Player Id: " + playerId
                        + " | Player Ips:" + player.getIp());
            }
        }

        attributes.put("playerId", playerId);
        return true;
    }

//    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
//        Log.SERVER.debug(this.getClass(), "handshake init");
//        HttpHeaders headers = request.getHeaders();
//
//        String authHeader = headers.getFirst("Authorization");
//
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            response.setStatusCode(HttpStatus.UNAUTHORIZED);
//            // TODO Ip Log
//            return false;
//        }
//
//        System.out.println("we handshake token:" + authHeader);
//
//        String token = authHeader.substring(7); // Skip past "Bearer "
//
//        Log.SERVER.debug(this.getClass(),token);
//
//        if (token == null) {
//            response.setStatusCode(HttpStatus.UNAUTHORIZED);
//            // TODO Ip Log
//            return false;
//        }
//
//        int playerId = serviceClient.getPlayerId(token);
//        Log.SERVER.debug(this.getClass(), String.valueOf(playerId));
//
//        System.out.println(playerId);
//        if (playerId == -1) {
//            response.setStatusCode(HttpStatus.UNAUTHORIZED);
//            // TODO ip log
//            return false;
//        }
//        if (playerId == -2) {
//            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
//            return false;
//        }
//
//        attributes.put("playerId", playerId);
//        System.out.println("authenticated:" + playerId);
//        return true;
//    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }

    private String getTokenFromUri(URI uri) {
        String query = uri.getQuery();
        if (query == null) { return null; }
        String[] pairs = query.split("&");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length > 1 && keyValue[0].equals("token")) {
                return keyValue[1];
            }
        }

        return null;
    }

}