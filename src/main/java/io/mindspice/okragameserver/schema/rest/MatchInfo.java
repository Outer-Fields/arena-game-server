package io.mindspice.okragameserver.schema.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.mindspice.databaseservice.client.schema.Results;


public record MatchInfo(
        @JsonProperty("self_name") String selfName,
        @JsonProperty("self_avatar") String selfAvatar,
        @JsonProperty("self_results") Results selfResults,
        @JsonProperty("enemy_name") String enemyName,
        @JsonProperty("enemy_avatar") String enemyAvatar,
        @JsonProperty("enemy_results") Results enemyResults
) { }
