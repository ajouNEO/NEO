package com.neo.back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
// import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EnableJpaAuditing
public class BackApplication {
//	private final EdgeServerRepository edgeServerRepository;
//
//	@Value("#{'${edgeservers.ip}'.split(',')}")private List<String> hostsTest;
//	@Value("#{'${edgeservers.id}'.split(',')}")private List<String> IDsTest;
//
//    public BackApplication(EdgeServerRepository edgeServerRepository) {
//        this.edgeServerRepository = edgeServerRepository;
//    }


    public static void main(String[] args) {

//		EdgeServer edgeServer = new EdgeServer();
//		edgeServer.setEdgeServerName(IDsTest.get(0));
//		edgeServer.setIp(hostsTest.get(0));
//		edgeServerRepository.save(dockerServer);
//		edgeServer.setEdgeServerName(IDsTest.get(1));
//		edgeServer.setIp(hostsTest.get(1));
//		edgeServerRepository.save(dockerServer);
		// Dotenv dotenv = Dotenv.load();
        // dotenv.entries().forEach(entry -> {
        //     // .env 파일의 키를 Spring 프로퍼티 명명 규칙에 맞게 변환
        //     // 예: SPRING_DATASOURCE_URL -> spring.datasource.url
        //     String propName = entry.getKey().toLowerCase().replace('_', '.');
        //     System.setProperty(propName, entry.getValue());
        // });
		SpringApplication.run(BackApplication.class, args);
	}

}
