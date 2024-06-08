// package com.neo.back.service.service;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// import com.neo.back.authorization.entity.User;
// import com.neo.back.exception.*;
// import com.neo.back.service.dto.CreateDockerDto;
// import com.neo.back.service.dto.EdgeServerInfoDto;
// import com.neo.back.service.entity.DockerImage;
// import com.neo.back.service.entity.DockerServer;
// import com.neo.back.service.entity.Game;
// import com.neo.back.service.middleware.DockerAPI;
// import com.neo.back.service.repository.DockerServerRepository;
// import com.neo.back.service.repository.EdgeServerRepository;
// import com.neo.back.service.repository.DockerImageRepository;
// import com.neo.back.service.repository.GameRepository;
// import com.neo.back.service.utility.MakeWebClient;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.*;
// import org.springframework.web.reactive.function.client.WebClient;
// import reactor.core.publisher.Mono;
// import reactor.test.StepVerifier;

// import java.util.Optional;

// public class CreateDockerServiceTest {

//     @Mock
//     private DockerAPI dockerAPI;

//     @Mock
//     private DockerServerRepository dockerRepo;

//     @Mock
//     private EdgeServerRepository edgeRepo;

//     @Mock
//     private DockerImageRepository imageRepo;

//     @Mock
//     private GameRepository gameRepo;

//     @Mock
//     private SelectEdgeServerService selectEdgeServerService;

//     @Mock
//     private ScheduleService scheduleService;

//     @Mock
//     private MakeWebClient makeWebClient;

//     @InjectMocks
//     private CreateDockerService createDockerService;

//     private WebClient webClient;

//     private User user;
//     private CreateDockerDto config;

//     @BeforeEach
//     public void setUp() {
//         MockitoAnnotations.openMocks(this);
//         webClient = mock(WebClient.class);
//         when(makeWebClient.makeDockerWebClient(anyString())).thenReturn(webClient);

//         // 테스트에서 공통으로 사용할 객체를 초기화합니다.
//         user = new User();
//         user.setPoints((long) 10);
//         user.setId(1L);

//         config = new CreateDockerDto();
//         config.setServerName("test-server");
//         config.setRamCapacity(4);
//         config.setGameName("Minecraft");
//         config.setVersion("1.20.4");
//     }

//     @Test
//     public void testCreateContainer_UserPointsNotEnough() {
//         user.setPoints((long) 0); // 포인트가 부족한 경우

//         Mono<Object> result = createDockerService.createContainer(config, user);
//         StepVerifier.create(result)
//                 .expectErrorMatches(throwable -> throwable instanceof LackPointException)
//                 .verify();
//     }

//     @Test
//     public void testCreateContainer_UserAlreadyHasServer() {
//         when(dockerRepo.findByUser(user)).thenReturn(new DockerServer());

//         Mono<Object> result = createDockerService.createContainer(config, user);
//         StepVerifier.create(result)
//                 .expectErrorMatches(throwable -> throwable instanceof DualServerException)
//                 .verify();
//     }

//     @Test
//     public void testCreateContainer_NoAvailableServers() {
//         when(dockerRepo.findByUser(user)).thenReturn(null);
//         when(selectEdgeServerService.selectingEdgeServer(anyInt())).thenReturn(null);

//         Mono<Object> result = createDockerService.createContainer(config, user);
//         StepVerifier.create(result)
//                 .expectErrorMatches(throwable -> throwable instanceof UserCapacityExceededException)
//                 .verify();
//     }

//     @Test
//     public void testRecreateContainer_UserPointsNotEnough() {
//         user.setPoints((long) 0); // 포인트가 부족한 경우

//         Mono<Object> result = createDockerService.recreateContainer(config, user);
//         StepVerifier.create(result)
//                 .expectErrorMatches(throwable -> throwable instanceof LackPointException)
//                 .verify();
//     }

//     @Test
//     public void testRecreateContainer_UserAlreadyHasServer() {
//         when(dockerRepo.findByUser(user)).thenReturn(new DockerServer());

//         Mono<Object> result = createDockerService.recreateContainer(config, user);
//         StepVerifier.create(result)
//                 .expectErrorMatches(throwable -> throwable instanceof DualServerException)
//                 .verify();
//     }

//     @Test
//     public void testRecreateContainer_NoAvailableServers() {
//         when(dockerRepo.findByUser(user)).thenReturn(null);
//         when(selectEdgeServerService.selectingEdgeServer(anyInt())).thenReturn(null);

//         Mono<Object> result = createDockerService.recreateContainer(config, user);
//         StepVerifier.create(result)
//                 .expectErrorMatches(throwable -> throwable instanceof UserCapacityExceededException)
//                 .verify();
//     }

//     @Test
//     public void testRecreateContainer_DockerImageDoesNotExist() {
//         when(dockerRepo.findByUser(user)).thenReturn(null);
//         EdgeServerInfoDto edgeServerInfoDto = new EdgeServerInfoDto();
//         edgeServerInfoDto.setIP("192.168.1.1");
//         edgeServerInfoDto.setPortSelect(8080);
//         when(selectEdgeServerService.selectingEdgeServer(anyInt())).thenReturn(edgeServerInfoDto);
//         when(imageRepo.findById(anyLong())).thenReturn(Optional.empty());

//         Mono<Object> result = createDockerService.recreateContainer(config, user);
//         StepVerifier.create(result)
//                 .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException)
//                 .verify();
//     }

//     @Test
//     public void testRecreateContainer_ImageDoesNotExistInStorage() {
//         when(dockerRepo.findByUser(user)).thenReturn(null);
//         EdgeServerInfoDto edgeServerInfoDto = new EdgeServerInfoDto();
//         edgeServerInfoDto.setIP("192.168.1.1");
//         edgeServerInfoDto.setPortSelect(8080);
//         when(selectEdgeServerService.selectingEdgeServer(anyInt())).thenReturn(edgeServerInfoDto);
//         DockerImage dockerImage = new DockerImage();
//         dockerImage.setImageId("image-id");
//         dockerImage.setServerName("server-name");
//         dockerImage.setUser(user);
//         dockerImage.setGame(new Game());
//         when(imageRepo.findById(anyLong())).thenReturn(Optional.of(dockerImage));

//         Mono<Object> result = createDockerService.recreateContainer(config, user);
//         StepVerifier.create(result)
//                 .expectErrorMatches(throwable -> throwable instanceof DoesNotExistImageException)
//                 .verify();
//     }
// }
