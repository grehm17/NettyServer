package com.FileStorage.server.test.SrvCmd;

import com.FileStorage.Common.CmdID;
import com.FileStorage.Common.Command;
import com.FileStorage.Common.Folders;
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

public class AuthCmd extends Command {

    private Connection connection;
    private String Usr;

    public AuthCmd() {

        super.ID = CmdID.Auth;

    }

    public byte Execute(ChannelHandlerContext ctx, ByteBuf buf) {

        connect();

        ByteBuf answ = Unpooled.buffer(1024);
        String msg = "Denied";

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
            PreparedStatement AuthStmt = connection.prepareStatement("SELECT StartFolder FROM Users WHERE Name = ? AND Password = ?;");
            AuthStmt.setString(1,aName);
            AuthStmt.setString(2,aPass);
            ResultSet rs = AuthStmt.executeQuery();
            if (rs.next()){
                msg = rs.getString("StartFolder");
                Usr = aName;
                ByteBuffer tmpBuff = ByteBuffer.allocate(256);
                bChan.read(tmpBuff);
                bytes = new byte[tmpBuff.position()];
                tmpBuff.flip();
                tmpBuff.get(bytes);
                String initPath = new String(bytes);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }catch (IOException e){

        }finally {

            disconnect();
        }


        answ.writeInt(msg.length());
        answ.writeBytes(msg.getBytes());

        ctx.writeAndFlush(answ);

        return 5;

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
    @Override
    public String getCmdInfo() {
        return Usr;
    }
}
