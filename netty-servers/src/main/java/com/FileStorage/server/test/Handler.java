package com.FileStorage.server.test;

import com.FileStorage.Common.CmdID;
import com.FileStorage.Common.CmdManager;
import com.FileStorage.server.test.SrvCmd.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


public class Handler extends ChannelInboundHandlerAdapter { // (1)
    private boolean state = false;
    private CmdManager Mgr;
    private byte exitCode;
    private String User;
    private ByteBuf answ = Unpooled.buffer(1);

    public Handler() {

        Mgr = new CmdManager();

        Mgr.addCmd(new SendCmd());
        Mgr.addCmd(new GetCmd());
        Mgr.addCmd(new DelCmd());
        Mgr.addCmd(new RnmCmd());
        Mgr.addCmd(new MoveCmd());
        Mgr.addCmd(new AuthCmd());
        Mgr.addCmd(new RefreshCmd());
        Mgr.addCmd(new RegCmd());
        Mgr.addCmd(new ShrCmd());

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf buf = ((ByteBuf)msg);

        if (!state){
            byte[] inCmd = new byte[2];
            buf.readBytes(inCmd);
            state = Mgr.setCmd(new String(inCmd));
            answ.retain();
        }

        if (state){
            if(Mgr.activeCmd.getID() == CmdID.Refresh || Mgr.activeCmd.getID() == CmdID.Share){
                exitCode = Mgr.activeCmd.Execute(ctx,buf,User);
            }else  exitCode = Mgr.activeCmd.Execute(ctx,buf);

            if (exitCode > 0){
                state = false;

                if(Mgr.activeCmd.getID() == CmdID.Auth) User = Mgr.activeCmd.getCmdInfo();

            } else {
                state = true;
            }

            if (!state && exitCode != 2){
                Mgr.activeCmd.refresh();
            }

            if( exitCode != 5 && exitCode != 0) {
                answ.writeByte(exitCode);
                ctx.writeAndFlush(answ);
                answ.retain();
                answ.clear();

            }

        }}
    }
