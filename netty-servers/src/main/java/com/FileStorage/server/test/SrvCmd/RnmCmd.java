package com.FileStorage.server.test.SrvCmd;

import com.FileStorage.Common.CmdID;
import com.FileStorage.Common.Command;
import com.FileStorage.Common.Folders;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.io.IOException;


public class RnmCmd extends Command {

    public RnmCmd() {

        super.ID = CmdID.Rename;

    }

    public byte Execute(ChannelHandlerContext ctx, ByteBuf buf) {

        int msgSize = buf.readInt();
        byte[] name = new byte[msgSize];
        buf.readBytes(name);
        msgSize = buf.readInt();
        byte[] newname = new byte[msgSize];
        buf.readBytes(newname);

        try {
            File OldF = new File(new String(name));
            OldF.renameTo(new File(new String(newname)));
        } catch (Exception e){
            return 3;
        }

        return 1;
    }
}
