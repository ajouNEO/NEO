package com.neo.back.server.service;


import org.springframework.stereotype.Service;

import com.neo.back.server.dto.MinecraftConfigDTO;

import java.io.FileWriter;
import java.io.IOException;

@Service
public class createMineCraftConfig {

    String host = "원격 호스트 IP";
    String user = "사용자 이름";
    String privateKey = "개인 키 파일 경로";


    public void createServerConfig(MinecraftConfigDTO config) throws IOException {
        try (FileWriter fileWriter = new FileWriter("server.properties")) {
            fileWriter.write("difficulty=" + config.getDifficulty() + "\n");
            fileWriter.write("game_mode=" + config.getGameMode() + "\n");
        }

    }

}