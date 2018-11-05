package com.FileStorage.server.test.SrvCmd;

import com.FileStorage.Client.PathLine;
import com.FileStorage.Common.CmdID;
import com.FileStorage.Common.Command;
import com.FileStorage.Common.Folders;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.sql.*;
import java.util.HashSet;
import java.util.Iterator;

public class RefreshCmd extends Command {

    private Path rootPath;
    private Connection connection;

    public RefreshCmd() {

        super.ID = CmdID.Refresh;
        Path init = Paths.get("ServerDir.txt");

        try(SeekableByteChannel bChan = Files.newByteChannel(init)) {ByteBuffer tmpBuff = ByteBuffer.allocate(256);
            bChan.read(tmpBuff);
            byte[] bytes = new byte[tmpBuff.position()];
            tmpBuff.flip();
            tmpBuff.get(bytes);
            rootPath = Paths.get(new String(bytes));
        }catch (IOException e){

        }

    }

    public byte Execute(ChannelHandlerContext ctx, ByteBuf buf,String ...args) {

        int nSize = buf.readInt();
        byte[] msg = new byte[nSize];

        buf.readBytes(msg);
        byte mode = buf.readByte();
        Path fPath = Paths.get(new String(msg));
        ByteBuf aw = Unpooled.buffer(1024);

        if (mode == 1 || mode == 4) fPath = fPath.getParent();

        boolean isRoot = rootPath.equals(fPath);
        ResultSet rs = null;
        HashSet<String> grantList = new HashSet<>();
        if (isRoot){
         try{
             connect();
             PreparedStatement AuthStmt = connection.prepareStatement("SELECT Folder FROM AccessTable WHERE Name = ? ;");
           AuthStmt.setString(1,args[0]);
           rs = AuthStmt.executeQuery();
             while (rs.next()){
                 grantList.add(rs.getString("Folder"));
             }
         } catch (SQLException e) {
             disconnect();
           e.printStackTrace();
         }finally {
             disconnect();
         }
        }


        try (DirectoryStream<Path> stream = Files.newDirectoryStream(fPath)) {
            int i = 0;
                for (Path file : stream) {

                    if (mode == 3 || mode == 4){
                        if (!Files.isDirectory(file)) continue;
                    }

                    if (isRoot){
                        if (!grantList.contains(file.toString())) continue;
                    }

                    i = i+1;
                    msg = file.getFileName().toString().getBytes();
                    if (aw.writerIndex() + msg.length + 14 > aw.capacity()) RewindBuff(ctx, aw);
                    aw.writeInt(msg.length);
                    aw.writeBytes(msg);
                    aw.writeLong(Files.size(file));
                    aw.writeByte(Files.isDirectory(file) ? 1 : 0);
                    aw.writeByte(0);
                }
                if(i == 0) {
                    aw.writeInt(0);
                    aw.writeByte(1);
                }else {
                    aw.setByte(aw.writerIndex() - 1, 1);
                }
                aw.writeInt(fPath.toString().length());
                aw.writeBytes(fPath.toString().getBytes());
                aw.writeByte(isRoot ? 1 : 0);
                aw.writeByte(2);
                ctx.writeAndFlush(aw);

        } catch (IOException | DirectoryIteratorException x) {
            return 3;
        }

        return 5;

    }

    void RewindBuff(ChannelHandlerContext ctx, ByteBuf aw){

        ctx.writeAndFlush(aw);
        aw.clear();
        aw.retain();

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
