package com.FileStorage.Client.ClientCmd;

import com.FileStorage.Common.CmdID;
import com.FileStorage.Common.Command;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.InvalidPathException;

public class RnmCmd extends Command {

    public RnmCmd() {

        super.ID = CmdID.Rename;
        super.Errors.put((byte)3,"Server file system error");
        super.Errors.put((byte)5,"Unknown client error");
        super.Errors.put((byte)2,"No such file exists");

    }

    public byte Execute(SocketChannel Skt, String[] Param) {

        try {

            ByteBuffer tmpBuff = ByteBuffer.allocate(1024);

            tmpBuff.put(ID.getBytes());

            tmpBuff.putInt(Param[0].length());
            tmpBuff.put(Param[0].getBytes());

            tmpBuff.putInt(Param[1].length());
            tmpBuff.put(Param[1].getBytes());

            tmpBuff.flip();

            Skt.write(tmpBuff);

            tmpBuff.clear();

            Skt.read(tmpBuff);

            return tmpBuff.get();


        }catch (InvalidPathException e){

            return 2;

        }catch (IOException e){

            return 4;

        }

    }

}
