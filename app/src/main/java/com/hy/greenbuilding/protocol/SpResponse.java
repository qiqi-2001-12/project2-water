package com.hy.greenbuilding.protocol;

import java.util.Arrays;

public class SpResponse extends SpCommand{
    private byte[] otaData;
    public SpResponse(byte[] response) {
        super(response[7]);
        command = Arrays.copyOfRange(response,5,7);
        functionId = response[10];
        if(response.length > 15){
            data = Arrays.copyOfRange(response,13,response.length-2);
        }
        if(response.length >= 12){
            otaData = Arrays.copyOfRange(response,10,response.length-2);
        }
    }
    public byte[] getOtaData(){
        return this.otaData;
    }
}
