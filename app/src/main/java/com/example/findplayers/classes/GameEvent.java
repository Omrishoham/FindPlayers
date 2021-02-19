package com.example.findplayers.classes;

import java.util.ArrayList;
import java.util.UUID;

public class GameEvent {

    private String gameId;
    private String ownerId;
    private String type;
    private String date;
    private String startTime;
    private String endTime;
    private String maxPlayers;
    private ArrayList<String> playersList;
    private String notices;
    private String locationLat;
    private String locationLng;

    public GameEvent() {}

    public GameEvent(String ownerId, String type, String date, String startTime, String endTime, String maxPlayers, String notices, String locationLat, String locationLng) {
        this.ownerId = ownerId;
        this.type = type;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxPlayers = maxPlayers;
        this.playersList = new ArrayList<String>();
        this.playersList.add(ownerId);
        this.notices = notices;
        this.locationLat = locationLat;
        this.locationLng = locationLng;
        this.gameId = UUID.randomUUID().toString();
    }

    public String getNotices() {
        return notices;
    }

    public void setNotices(String notices) {
        this.notices = notices;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public ArrayList<String> getPlayersList() {
        return playersList;
    }

    public void setPlayersList(ArrayList<String> playersList) {
        this.playersList = playersList;
    }

    public String getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(String maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public String getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(String locationLat) {
        this.locationLat = locationLat;
    }

    public String getLocationLng() {
        return locationLng;
    }

    public void setLocationLng(String locationLng) {
        this.locationLng = locationLng;
    }
}
