package com.FileStorage.Client.ClientCmd;

import com.FileStorage.Common.CmdID;
import com.FileStorage.Common.Command;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.*;

public class DelCmd extends Command {

    public DelCmd() {
        super.ID = CmdID.Delete;
        super.Errors.put((byte)3,"Server file system error");
        super.Errors.put((byte)4,"Unknown client error");
        super.Errors.put((byte)2,"Unable to reach selected folder");
    }

    public byte Execute(SocketChannel Skt, String[] Param) {

        try {
            ByteBuffer tmpBuff = ByteBuffer.allocate(1024);
            tmpBuff.put(ID.getBytes());
            byte[] par= Param[0].getBytes();
            tmpBuff.putInt(par.length);
            tmpBuff.put(par);
            tmpBuff.flip();
            Skt.write(tmpBuff);
            tmpBuff.clear();
            Skt.read(tmpBuff);

            return 1;
        }catch (InvalidPathException e){
            return 2;
        }catch (IOException e){
            return 4;

        }


    }

}
