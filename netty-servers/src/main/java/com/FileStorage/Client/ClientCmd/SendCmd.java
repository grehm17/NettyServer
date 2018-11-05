package com.FileStorage.Client.ClientCmd;

import com.FileStorage.Common.CmdID;
import com.FileStorage.Common.Command;
import com.sun.deploy.util.ArrayUtil;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Scanner;

public class SendCmd extends Command {

    public SendCmd() {
        super.ID = CmdID.ToSrvID;
        super.Errors.put((byte)3,"Server file system error");
        super.Errors.put((byte)4,"Unknown server error");
    }

    public byte Execute(SocketChannel Skt, String[] Param) {

        Path fPath = Paths.get(Param[0]);

        try (FileChannel fChan = (FileChannel) Files.newByteChannel(fPath, StandardOpenOption.READ)
             ){
            ByteBuffer tmpBuff = ByteBuffer.allocate(10485760);
            tmpBuff.put(ID.getBytes());
            byte[] par= Param[1].getBytes();

            tmpBuff.putInt(par.length);
            tmpBuff.put(par);
            tmpBuff.putLong(Files.size(fPath));
            int bCount;
            bCount = fChan.read(tmpBuff);

            while (bCount != -1){
                tmpBuff.flip();
                Skt.write(tmpBuff);
                tmpBuff.clear();
                bCount = fChan.read(tmpBuff);
            }

            tmpBuff = ByteBuffer.allocate(1);

            Skt.read(tmpBuff);
            tmpBuff.flip();

            return tmpBuff.get();

        }catch (InvalidPathException e){
            System.out.println("No such file found");
            return 3;
        }catch (IOException e){
            System.out.println("IO error");
            return 3;
        }

    }

}
