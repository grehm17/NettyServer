package com.FileStorage.Client.ClientCmd;

import com.FileStorage.Common.CmdID;
import com.FileStorage.Common.Command;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;

public class GetCmd extends Command {

    public GetCmd() {

        super.ID = CmdID.FromSrvID;
        super.Errors.put((byte)3,"Server file system error");
        super.Errors.put((byte)5,"Unknown client error");
        super.Errors.put((byte)2,"Unable to reach selected folder");
        super.Errors.put((byte)4,"Unknown server error");

    }

    public byte Execute(SocketChannel Skt, String[] Param) {

        Path fPath = Paths.get(Param[0]);

        try {
            Files.deleteIfExists(fPath);
            Files.createFile(fPath);
        } catch (IOException e) {
            System.out.println("IO error");
        }

        try (FileChannel fChan = (FileChannel)Files.newByteChannel(fPath, StandardOpenOption.WRITE)
        ){
            ByteBuffer tmpBuff = ByteBuffer.allocate(1024);
            tmpBuff.put(ID.getBytes());
            byte[] par= Param[1].getBytes();
            tmpBuff.putInt(par.length);
            tmpBuff.put(par);
            tmpBuff.flip();
            Skt.write(tmpBuff);
            tmpBuff = ByteBuffer.allocate(10485760);
            int bCount;
            bCount = Skt.read(tmpBuff);
            tmpBuff.flip();
            long fSize = tmpBuff.getLong() - bCount + 8;
            fChan.write(tmpBuff);

            while (fSize > 0){
                tmpBuff.clear();
                bCount = Skt.read(tmpBuff);
                fSize = fSize - bCount;
                tmpBuff.flip();
                fChan.write(tmpBuff);
            }
        }catch (InvalidPathException e){
            return 2;
        }catch (IOException e){
            return 5;
        }
        return 0;
    }

}
