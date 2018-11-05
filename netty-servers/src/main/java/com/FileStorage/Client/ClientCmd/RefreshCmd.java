package com.FileStorage.Client.ClientCmd;

import com.FileStorage.Client.PathLine;
import com.FileStorage.Common.CmdID;
import com.FileStorage.Common.Command;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;

public class RefreshCmd extends Command {

    public RefreshCmd() {

        super.ID = CmdID.Refresh;

    }
    @Override
    public byte Execute(SocketChannel Skt, String[] Param) {

        return 0;

    }

    public  ObservableList<PathLine> Execute(SocketChannel Skt, String Param,byte mode) {

        try {

            ByteBuffer tmpBuff = ByteBuffer.allocate(1024);

            tmpBuff.put(ID.getBytes());
            tmpBuff.putInt(Param.length());
            tmpBuff.put(Param.getBytes());
            tmpBuff.put(mode);
            tmpBuff.flip();
            Skt.write(tmpBuff);
            tmpBuff.clear();
            int nSize;
            int pSize;
            byte flag = 0;

            ObservableList<PathLine> pathsList = FXCollections.observableArrayList();
            ByteBuffer nBuff = ByteBuffer.allocate(1024);
            Skt.read(nBuff);

            if (nBuff.position() >= 4) {
                nBuff.flip();
                pSize = nBuff.getInt();
                nSize = pSize + 10;
            }else {
                pSize = 1;
                nSize = 10;
            }

            while(flag != 2){

                if (nBuff.remaining() >= nSize && nBuff.remaining()>0){

                    if(pSize != 0){
                    byte[] msg = new byte[pSize];
                    nBuff.get(msg);

                    if (flag == 0) {
                        long size = nBuff.getLong();
                        byte fold = nBuff.get();
                        pathsList.add(new PathLine(new String(msg), size, fold == 1,fold == 1 ? 1 : 2));
                    }else{
                        byte rt = nBuff.get();
                        if (rt != 1) pathsList.add(new PathLine("...",0,true,0));
                        pathsList.add(new PathLine(new String(msg), 0, false,2));
                    }}

                    flag = nBuff.get();

                    if (nBuff.remaining() <= 4 && flag != 2){
                        nBuff.flip();
                        Skt.read(nBuff);
                        nBuff.flip();
                        pSize = nBuff.getInt();
                        nSize = pSize+10;
                    }else if(flag != 2){
                        pSize = nBuff.getInt();
                        nSize = pSize+(flag == 1 ? 1:10);
                    }
                }else{
                    nBuff.flip();
                    Skt.read(nBuff);
                    if (nBuff.remaining() >= 4 && pSize == 1) {
                        pSize = nBuff.getInt();
                        nSize = pSize + 10;
                    }
                }

            }

            return pathsList;


        }catch (InvalidPathException e){
            System.out.println("No such file found");
            return null;
        }catch (IOException e){
            System.out.println("IO error");
            return null;
        }

    }

}
