package com.neo.back.docker.service;

import org.springframework.stereotype.Service;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.neo.back.docker.dto.EdgeServerCmdDto;
import com.neo.back.docker.dto.EdgeServerInfoDto;
import com.neo.back.docker.entity.EdgeServer;
import java.lang.reflect.Field;

@Service
public class GetEdgeServerService {

    private final SshService sshService;

    public GetEdgeServerService(SshService sshService) {
        this.sshService = sshService;
    }

    public EdgeServerInfoDto changeEdgeServerEntityTODTO(EdgeServer edgeServer) {
        EdgeServerInfoDto edgedgeServerDTO = new EdgeServerInfoDto(edgeServer.getEdgeServerName());
        int memoryIdle = edgeServer.getMemoryTotal() - edgeServer.getMemoryUse();
        Session session = null;
        String portCMD = "";
        try {
            
            EdgeServerCmdDto cmd = sshService.getDataFromJson();
            Class<?> clazz = cmd.getClass();
            Field[] fields = clazz.getDeclaredFields();

            portCMD = sshService.getCMDPort(fields,cmd);
            session = sshService.getJsch(edgeServer.getIp(), edgeServer.getUser(), edgeServer.getPassWord());

            sshService.getLinesByCMDPortWithChannel(edgedgeServerDTO, session, portCMD, fields);
            sshService.selectPort(edgedgeServerDTO);
            edgedgeServerDTO.setMemoryUse(edgeServer.getMemoryUse());
            edgedgeServerDTO.setMemoryIdle(memoryIdle);
            edgedgeServerDTO.setIP(edgeServer.getIp());

        } catch (JSchException | java.io.IOException e) {
            e.printStackTrace();
        } catch(IllegalAccessException e){
            e.printStackTrace();
        } catch(NumberFormatException  e){
            e.printStackTrace();
        }
        finally {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
        return edgedgeServerDTO;
    }

    
}
