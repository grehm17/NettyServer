package com.FileStorage.server.test.SrvCmd;

import com.FileStorage.Common.CmdID;
import com.FileStorage.Common.Command;
import com.FileStorage.Common.Folders;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class GetCmd extends Command {

    private FileChannel fChan = null;
    private long fSize = 0l;

    public GetCmd() {

        super.ID = CmdID.FromSrvID;

    }

    public byte Execute(ChannelHandlerContext ctx, ByteBuf buf) {

        if (fChan == null) {
            int msgSize = buf.readInt();
            byte[] name = new byte[msgSize];
            buf.readBytes(name);
            Path fPath = Paths.get(new String(name));

            try {
                fChan = (FileChannel) Files.newByteChannel(fPath, StandardOpenOption.READ);
                fSize = Files.size(fPath);
            } catch (IOException e) {
                return 3;
            }
        }

            try {
                ByteBuffer tmpBuff = ByteBuffer.allocate(10485760);
                tmpBuff.putLong(fSize);
                ByteBuf answ = Unpooled.buffer(10485760);
                answ.retain();
                int bCount = fChan.read(tmpBuff);
                while (bCount != -1){
                    tmpBuff.flip();
                    answ.writeBytes(tmpBuff);
                    ctx.writeAndFlush(answ);
                    tmpBuff.clear();
                    answ.retain();
                    answ.clear();
                    bCount = fChan.read(tmpBuff);
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

            return 1;
        }

    public void refresh(){

        try {
            fChan.close();
        } catch (IOException e) {
        }
        fChan = null;

    }

}
