package com.FileStorage.server.test.SrvCmd;

import com.FileStorage.Common.CmdID;
import com.FileStorage.Common.Command;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;


public class ShrCmd extends Command {

    private Connection connection;

    public ShrCmd() {

        super.ID = CmdID.Share;

    }

    public byte Execute(ChannelHandlerContext ctx, ByteBuf buf,String ...args) {

        connect();

        int nLength = buf.readInt();
        byte[] bytes = new byte[nLength];
        buf.readBytes(bytes);
        String aName = new String(bytes);
        try{
        PreparedStatement RegStmt = connection.prepareStatement("SELECT StartFolder FROM Users WHERE Name = ? ;");
        RegStmt.setString(1,aName);
        ResultSet rs = RegStmt.executeQuery();
        if (!rs.next()){
            return 3;
        }else{

            RegStmt = connection.prepareStatement("SELECT StartFolder FROM Users WHERE Name = ? ;");
            RegStmt.setString(1,args[0]);
            rs = RegStmt.executeQuery();

            RegStmt = connection.prepareStatement("INSERT INTO AccessTable (Name,  Folder) VALUES (?,?);");
            RegStmt.setString(1, aName);
            RegStmt.setString(2, rs.getString("StartFolder"));
            RegStmt.execute();
        }

    } catch (SQLException e) {
        return 4;
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
