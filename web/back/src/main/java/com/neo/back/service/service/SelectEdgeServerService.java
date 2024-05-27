package com.neo.back.service.service;


import com.neo.back.service.dto.EdgeServerInfoDto;
import com.neo.back.service.entity.EdgeServer;
import com.neo.back.service.repository.EdgeServerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;



@Service
@RequiredArgsConstructor
public class SelectEdgeServerService {

	private final EdgeServerRepository edgeServerRepo;
	private final GetEdgeServerService getEdgeServerService;
	
	public synchronized EdgeServerInfoDto selectingEdgeServer(int UserMemory){
		List<EdgeServer> allEdgeServers = edgeServerRepo.findAll();
		List<EdgeServerInfoDto> edgedgeServerInfoList =  new ArrayList<>();;
		EdgeServerInfoDto selecteEdgeServer = null;

        int edgeServermemoryLeft = 1; // 엣지 서버의 시스템 메모리 공간 할당

		for(EdgeServer edgeServer : allEdgeServers){
			EdgeServerInfoDto edgeServerInfo = getEdgeServerService.changeEdgeServerEntityTODTO(edgeServer);
			double canUseMemory = edgeServerInfo.getMemoryIdle() - UserMemory - edgeServermemoryLeft;
			if(0 <= canUseMemory){
				edgedgeServerInfoList.add(edgeServerInfo);
			}
		}
        /*
         * 모든 edgeServer에 대한 데이터를 기반으로 선정 알고리즘 실행
         * 
         * 항시 지켜야하는 조건 : 생성하고 남는 램이 1GB이상 남아있어야 한다. -> 조건문으로 달성
         * 가장 작은 램(1GB 이상)이 남은데에 할당한다 -> 오름차순에서 확인하면서 달성 
         * 
         * 현 상황에서는 각각의 edgeServer에 들어가서 위의 사용자가 사용할 최소 기준(사용자가 요구한)을 만족하면, -> 위에서 달성
         * 서버를 개설시키도록 한다.
         * 만약 기준을 만족하는게 없다면, NULL값을 리턴한다. 
         */
        Collections.sort(edgedgeServerInfoList, Comparator.comparingDouble(EdgeServerInfoDto::getMemoryIdle));

        for(EdgeServerInfoDto edgeServer : edgedgeServerInfoList){
			selecteEdgeServer = edgeServer;
			return selecteEdgeServer;
        }
		
        return selecteEdgeServer;
    }


}