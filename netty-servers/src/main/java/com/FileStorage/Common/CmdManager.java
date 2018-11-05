package com.FileStorage.Common;

import java.net.Socket;
import java.util.HashMap;

public class CmdManager {

    private HashMap<String,Command> Mgr = new HashMap<>();
    public Command activeCmd;

    public void addCmd(Command cmd){

        Mgr.put(cmd.getID(),cmd);

    }

    public boolean setCmd(String ID){

        if (Mgr.containsKey(ID)) {

            activeCmd =  Mgr.get(ID);

            return true;

        }else return false;

    }

}
