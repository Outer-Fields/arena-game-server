package io.mindspice.okragameserver.core;

import io.mindspice.databaseservice.client.api.OkraGameAPI;
import io.mindspice.jxch.rpc.util.RPCException;
import io.mindspice.mindlib.http.UnsafeHttpJsonClient;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.util.Log;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.util.Collections;
import java.util.function.Supplier;


public class HttpServiceClient {
    private final UnsafeHttpJsonClient httpClient = new UnsafeHttpJsonClient();
    private final OkraGameAPI okraGameAPI;
    public final Supplier<RPCException> serviceError =
            () -> new RPCException("Required internal service call returned Optional.empty");

    public HttpServiceClient(OkraGameAPI okraGameAPI) {
        this.okraGameAPI = okraGameAPI;
    }

    // This handles the getting the player_id from the auth token, doubles as a way to fetch the player_id
    // from an auth token authoritatively as we cant trust the player to send their own player id.
    // Also handles all the exceptions for return code -2 on following methods
    public int getPlayerId(String token) {
        try {
            return Integer.parseInt(httpClient.requestBuilder()
                    .address(Settings.GET().authServiceUri)
                    .port(Settings.GET().authServicePort)
                    .path("/internal/player_id_from_token")
                    .contentType(ContentType.APPLICATION_JSON)
                    .credentials(Settings.GET().authServiceUser, Settings.GET().authServicePass)
                    .request(Collections.singletonMap("token", token))
                    .asPost()
                    .makeAndGetString());
        } catch (IOException e) {
            Log.SERVER.error(this.getClass(), "Error making player id call to auth", e);
            return -2;
        }
    }

    public int doAuth(String token) {
        try {
            return Integer.parseInt(httpClient.requestBuilder()
                    .address(Settings.GET().authServiceUri)
                    .port(Settings.GET().authServicePort)
                    .path("/internal/authenticate")
                    .contentType(ContentType.APPLICATION_JSON)
                    .credentials(Settings.GET().authServiceUser, Settings.GET().authServicePass)
                    .request(Collections.singletonMap("token", token))
                    .asPost()
                    .makeAndGetString());
        } catch (IOException e) {
            Log.SERVER.error(this.getClass(), "Error making player authenticate call to auth", e);
            return -2;
        }
    }

    public void updateAvatar(int playerId, String nftId) {
        try {
            httpClient.requestBuilder()
                    .address(Settings.GET().itemServiceUri)
                    .port(Settings.GET().itemServicePort)
                    .path("/internal/update_avatar")
                    .contentType(ContentType.APPLICATION_JSON)
                    .credentials(Settings.GET().itemServiceUser, Settings.GET().itemServicePass)
                    .request(new JsonUtils.ObjectBuilder()
                            .put("player_id", playerId)
                            .put("nft_id", nftId)
                            .buildNode())
                    .asPost()
                    .makeAndGetString();
        } catch (IOException e) {
            Log.SERVER.error(this.getClass(), "Error making avatar update call to item", e);
        }
    }

    public OkraGameAPI gameAPI() { return okraGameAPI; }
}
