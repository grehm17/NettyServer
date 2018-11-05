package com.FileStorage.server.test.SrvCmd;

import com.FileStorage.Common.CmdID;
import com.FileStorage.Common.Command;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

public class RegCmd extends Command {

    private Connection connection;

    public RegCmd() {

        super.ID = CmdID.Register;

    }

    public byte Execute(ChannelHandlerContext ctx, ByteBuf buf) {

        connect();

        int nLength = buf.readInt();
        byte[] bytes = new byte[nLength];
        buf.readBytes(bytes);
        String aName = new String(bytes);
        int pLength = buf.readInt();
        bytes = new byte[pLength];
        buf.readBytes(bytes);
        String aPass = new String(bytes);
        Path init = Paths.get("ServerDir.txt");
        try(SeekableByteChannel bChan = Files.newByteChannel(init)) {
            PreparedStatement RegStmt = connection.prepareStatement("SELECT StartFolder FROM Users WHERE Name = ? ;");
            RegStmt.setString(1,aName);
            ResultSet rs = RegStmt.executeQuery();
            if (rs.next()){
               return 3;
            }else{
                ByteBuffer tmpBuff = ByteBuffer.allocate(256);
                bChan.read(tmpBuff);
                bytes = new byte[tmpBuff.position()];
                tmpBuff.flip();
                tmpBuff.get(bytes);
                String initPath = new String(bytes)+"\\"+aName;
                Files.createDirectory(Paths.get(initPath));
                RegStmt = connection.prepareStatement("INSERT INTO Users (Name, Password, StartFolder) VALUES (?,?,?);");
                RegStmt.setString(1, aName);
                RegStmt.setString(2, aPass);
                RegStmt.setString(3, initPath);
                RegStmt.execute();

                RegStmt = connection.prepareStatement("INSERT INTO AccessTable (Name,  Folder) VALUES (?,?);");
                RegStmt.setString(1, aName);
                RegStmt.setString(2, initPath);
                RegStmt.execute();
            }

        } catch (SQLException e) {
            return 4;
        }catch (IOException e){

        }finally {

            disconnect();
        }


        return 1;

    }

    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:RegBase.db");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
