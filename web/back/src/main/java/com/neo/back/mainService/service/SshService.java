package com.neo.back.mainService.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neo.back.mainService.dto.EdgeServerCmdDto;
import com.neo.back.mainService.dto.EdgeServerInfoDto;
import com.jcraft.jsch.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class SshService {

    EdgeServerCmdDto getDataFromJson() throws IOException, StreamReadException, DatabindException {
        ObjectMapper objectMapper = new ObjectMapper();
        String edgeCmdControlPath = "edgeServer/control_edgeCmd.json";
        ClassPathResource jsonFile = new ClassPathResource(edgeCmdControlPath);
        EdgeServerCmdDto cmd = objectMapper.readValue(jsonFile.getInputStream(), EdgeServerCmdDto.class);
        return cmd;
    }
    
    String getCMDPort(Field[] fields_tmp, EdgeServerCmdDto cmd_tmp) throws IllegalArgumentException, IllegalAccessException, StreamReadException, DatabindException, IOException{
        fields_tmp[5].setAccessible(true);
        String portCMD = (String)fields_tmp[5].get(cmd_tmp);
        return portCMD;
    }

    Session getJsch(String host, String user, String password) throws JSchException {
        Session session;
        JSch jsch = new JSch();
        session = jsch.getSession(user, host, 22);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        return session;
    }

    void getLinesByCMDPortWithChannel(EdgeServerInfoDto edgeServer, Session session, String command, Field[] fields) 
                                throws JSchException, IOException {
        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);
        InputStream commandOutput = channel.getInputStream();
        channel.connect();

        BufferedReader reader = new BufferedReader(new InputStreamReader(commandOutput));
        List<String> lines =  new ArrayList<>();
        setPortEdgeServer(edgeServer, reader, lines);

        reader.close();
        channel.disconnect();
    }
    void setPortEdgeServer(EdgeServerInfoDto edgeServer, BufferedReader reader, List<String> lines) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        edgeServer.setPortUses(lines);
    }
    void selectPort(EdgeServerInfoDto edgeServer) {
        List<Integer> integerList = convertStringListToIntList(edgeServer.getPortUses());
        int minRange = 30000;
        int maxRange = 60000;
        int randomNumber = selectRandomNumber(minRange, maxRange, integerList);
        edgeServer.setPortSelect(randomNumber);
    }
    public static List<Integer> convertStringListToIntList(List<String> stringList) {
        List<Integer> integerList = new ArrayList<>();
        for (String str : stringList) {
            integerList.add(Integer.parseInt(str));
        }
        return integerList;
    }
    private int selectRandomNumber(int minRange, int maxRange, List<Integer> integerList) {
        Random random = new Random();
        int randomNumber;

        do {
            randomNumber = random.nextInt(maxRange - minRange + 1) + minRange;
        } while (integerList.contains(randomNumber));

        return randomNumber;
    }

}
