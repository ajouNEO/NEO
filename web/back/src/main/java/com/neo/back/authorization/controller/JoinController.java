package com.neo.back.authorization.controller;


import com.neo.back.authorization.dto.JoinDTO;
import com.neo.back.authorization.service.JoinService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
public class JoinController {


    private final JoinService joinService;

    public JoinController(JoinService joinService){
        this.joinService = joinService;
    }


    @PostMapping("/api/join")
    public ResponseEntity<?> joinProcess(@RequestBody JoinDTO joinDTO){


        return joinService.joinProcess(joinDTO);
    }



}
