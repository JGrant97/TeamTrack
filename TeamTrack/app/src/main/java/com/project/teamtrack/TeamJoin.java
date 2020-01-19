package com.project.teamtrack;

public class TeamJoin {
    private String TeamID;
    private String TeamMemberID;
    private String TeamMemberName;
    private String userLocation;


    public TeamJoin(String teamID, String teamMemberID, String teamMemberName, String userLocation) {
        TeamID = teamID;
        TeamMemberID = teamMemberID;
        TeamMemberName = teamMemberName;
        this.userLocation = userLocation;
    }

    public String getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    public String getTeamID() {
        return TeamID;
    }

    public void setTeamID(String teamID) {
        TeamID = teamID;
    }

    public String getTeamMemberID() {
        return TeamMemberID;
    }

    public void setTeamMemberID(String teamMemberID) {
        TeamMemberID = teamMemberID;
    }

    public String getTeamMemberName() {
        return TeamMemberName;
    }

    public void setTeamMemberName(String teamMemberName) {
        TeamMemberName = teamMemberName;
    }

    public TeamJoin(){

    }
}
