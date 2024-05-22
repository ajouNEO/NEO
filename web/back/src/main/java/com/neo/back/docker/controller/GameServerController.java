package com.neo.back.docker.controller;

import com.neo.back.docker.service.*;
import com.neo.back.docker.utility.GetCurrentUser;
import com.neo.back.springjwt.entity.User;

import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.neo.back.docker.dto.FileDataDto;

import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Map;



@RestController
@RequiredArgsConstructor
public class GameServerController {

    private final GameDataService gameDataService;
    private final GameServerSettingService serverSettingService;
    private final StartAndStopGameServerService startAndStopGameServerService;
    private final UploadAndDownloadService uploadAndDownloadService;
    private final OtherServerManagingService otherServerManagingService;
    private final ServerJoinService serverJoinService;
    private final GameLog gameLog;
    private final GetCurrentUser getCurrentUser;

    @PutMapping("/api/server/start")
    public Mono<Object> serverStart() {
        User user = getCurrentUser.getUser();
        return startAndStopGameServerService.getStartGameServer(user);
    }

    @PutMapping("/api/server/stop")
    public Mono<Object> serverStop() {
        User user = getCurrentUser.getUser();
        return startAndStopGameServerService.getStopGameServer(user);

    }

    @GetMapping("/api/server/setting")
    public Mono<Object> getServerSetting() {
        User user = getCurrentUser.getUser();
        return serverSettingService.getServerSetting(user);
    }

    @PostMapping("/api/server/setting")
    public Mono<Object> setServerSetting(@RequestBody Map<String, String> setting) throws IOException {
        User user = getCurrentUser.getUser();
        return serverSettingService.setServerSetting(setting, user);
    }

    @GetMapping("/api/server/ListOfFileAndFolder")
    public ResponseEntity<List<FileDataDto>> getDockerFileList(@RequestParam String path) {
        User user = getCurrentUser.getUser();
        Mono<String> fileListInst = gameDataService.getFileAndFolderListInst(path, user);
        List<FileDataDto> fileList = gameDataService.getFileAndFolderList(fileListInst, user);
        return ResponseEntity.ok(fileList);
    }

    @PostMapping("api/server/upload")
        public  ResponseEntity<String> uploadFile(@RequestParam("files") MultipartFile[] files,@RequestParam String path) {
            User user = getCurrentUser.getUser();
            Mono<String> Mes = uploadAndDownloadService.upload(files,path,user);
        return Mes.map(message -> ResponseEntity.ok().body("{\"uploadStatus\": \"" + message + "\"}")).block();
    }

    @PutMapping("api/server/delete")
    public ResponseEntity<Map<String, String>> deleteGameServerData(@RequestParam String path) {
        User user = getCurrentUser.getUser();
        Map<String, String> Mes = uploadAndDownloadService.deleteFileAndFolder(path,user);
        return ResponseEntity.ok(Mes);
    }

    @PutMapping("api/server/makeDir")
    public ResponseEntity<Map<String, String>> makeDirInGameServer(@RequestParam String path) {
        User user = getCurrentUser.getUser();
        Map<String, String> Mes = uploadAndDownloadService.makeDir(path,user);
        return ResponseEntity.ok(Mes);
    }

    @GetMapping("/api/server/info")
    public Mono<Object> getServerInfo() {
        User user = getCurrentUser.getUser();
        return otherServerManagingService.getServerInfo(user);
    }

    @PutMapping("/api/server/public")
    public Mono<Object> setPublic() {
        User user = getCurrentUser.getUser();
        return otherServerManagingService.setPublic(user);
    }

    @PutMapping("/api/server/freeAccess")
    public Mono<Object> setFreeAccess() {
        User user = getCurrentUser.getUser();
        return otherServerManagingService.setFreeAccess(user);
    }

    @PutMapping("/api/server/comment")
    public Mono<Object> setComment(@RequestBody(required = false) String comment) {
        User user = getCurrentUser.getUser();
        return otherServerManagingService.setComment(user, comment);
    }

    @PutMapping("/api/server/tags")
    public Mono<Object> setTags(@RequestBody List<String> tags) {
        User user = getCurrentUser.getUser();
        return otherServerManagingService.setTags(user,tags);
    }

    @GetMapping("/api/server/applicants")
    public SseEmitter getApplicants() {
        User user = getCurrentUser.getUser();
        return serverJoinService.getApplicants(user);
    }

    @GetMapping("/api/server/participants")
    public SseEmitter getParticipants() {
        User user = getCurrentUser.getUser();
        return serverJoinService.getParticipants(user);
    }

    @PostMapping("/api/server/allow/{userName}")
    public Mono<Object> allowParticipation(@PathVariable String userName) {
        User user = getCurrentUser.getUser();
        return serverJoinService.allowParticipation(user, userName);
    }

    @PostMapping("/api/server/refuse/{userName}")
    public Mono<Object> refuseParticipation(@PathVariable String userName) {
        User user = getCurrentUser.getUser();
        return serverJoinService.refuseParticipation(user, userName);
    }

    @PostMapping("/api/get-banlist")//특정 파일 읽어오 는 용도 api
    public Mono<String> readAndConvertToJson(String containerId, String filePath) {
        String command = "cat " + filePath;
        return serverSettingService.executeCommand(containerId, command)
                .map(content -> {
                    // 파일 내용을 JSON 객체로 변환
                    JSONObject json = new JSONObject();
                    json.put("content", content);
                    return json.toString();
                });
    }

    @GetMapping("/api/server/gamelog")
    public SseEmitter sendGameLog(@RequestParam String token) {
        User user = getCurrentUser.getUser();
        return gameLog.sendLogContinue(user); 
    }


}
