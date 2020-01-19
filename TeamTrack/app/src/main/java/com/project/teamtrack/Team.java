package com.project.teamtrack;

public class Team {
    private String TeamID;
    private String TeamCode;
    private String OwnerID;

    public Team() {

    }

    public Team(String teamID, String teamCode, String ownerID) {
        TeamID = teamID;
        TeamCode = teamCode;
        OwnerID = ownerID;
    }

    public String getOwnerID() {
        return OwnerID;
    }

    public void setOwnerID(String ownerID) {
        OwnerID = ownerID;
    }

    public String getTeamID() {
        return TeamID;
    }

    public void setTeamID(String teamID) {
        TeamID = teamID;
    }

    public String getTeamCode() {
        return TeamCode;
    }

    public void setTeamCode(String teamCode) {
        TeamCode = teamCode;
    }
}
