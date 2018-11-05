package com.FileStorage.server.test.SrvCmd;

import com.FileStorage.Common.CmdID;
import com.FileStorage.Common.Command;
import com.FileStorage.Common.Folders;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class SendCmd extends Command {

    private FileChannel fChan = null;
    private long fSize = 0;


    public SendCmd() {

        super.ID = CmdID.ToSrvID;

    }

    public byte Execute(ChannelHandlerContext ctx,ByteBuf buf) {

        if (fChan == null){
            int msgSize = buf.readInt();
            byte[] name = new byte[msgSize];
            buf.readBytes(name);
            Path fPath = Paths.get( new String(name));

            try {
                Files.deleteIfExists(fPath);
                Files.createFile(fPath);
                fChan = (FileChannel)Files.newByteChannel(fPath,StandardOpenOption.WRITE);
            } catch (IOException e) {
                return 3;
            }

            fSize = buf.readLong();
        }

            fSize = fSize - buf.readableBytes();

            try {
                fChan.write(buf.nioBuffer());
                if (fSize <= 0){
                    fChan.close();
                    return 1;
                }

            } catch (IOException e) {
                try {
                    fChan.close();
                } catch (IOException e1) {
                    return 3;
                }
                return 3;
            }catch (Exception e1){
                return 4;

            }

         return 0;
    }


    public void refresh(){

        fChan = null;
        fSize = 0;

    }

}
