package com.hy.greenbuilding.event;

public class RoomChangeEvent {
    private String roomListJson;
    public RoomChangeEvent(String roomListJson){
        this.roomListJson = roomListJson;
    }
    public String getRoomListJson(){
        return roomListJson;
    }
}
