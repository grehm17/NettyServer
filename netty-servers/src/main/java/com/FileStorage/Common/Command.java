package com.FileStorage.Common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.net.Socket;
import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

public class Command {

    protected String ID;
    protected HashMap<Byte,String> Errors = new HashMap<>();

    public byte Execute(ChannelHandlerContext ctx,ByteBuf buf){
        return 0;
    }
    public byte Execute(ChannelHandlerContext ctx,ByteBuf buf,String ...args){
        return 0;
    }
    public byte Execute(Socket Skt, String[] Param){
        return 0;
    }
    public byte Execute(SocketChannel Skt, String[] Param){
        return 0;
    }

    public void refresh(){
    }

    public String getID() {

        return ID;

    }

    public String getCmdInfo() {

        return "";

    }

    public String getError(byte id){

        if(Errors.containsKey(id)){
            return Errors.get(id);
        }else return "";
    }


}
