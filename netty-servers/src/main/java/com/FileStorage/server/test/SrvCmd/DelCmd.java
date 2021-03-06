package com.FileStorage.server.test.SrvCmd;

import com.FileStorage.Common.CmdID;
import com.FileStorage.Common.Command;
import com.FileStorage.Common.Folders;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DelCmd extends Command {

    public DelCmd() {

        super.ID = CmdID.Delete;

    }

    public byte Execute(ChannelHandlerContext ctx, ByteBuf buf) {
            int msgSize = buf.readInt();
            byte[] name = new byte[msgSize];
            buf.readBytes(name);
            Path fPath = Paths.get(new String(name));

        try {
            Files.deleteIfExists(fPath);
        } catch (IOException e) {
            return 3;
        }

        return 1;
    }
}
