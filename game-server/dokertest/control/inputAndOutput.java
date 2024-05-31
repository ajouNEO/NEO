import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;

public class inputAndOutput {

    public static AtomicBoolean startFlag = new AtomicBoolean(false);
    public static AtomicBoolean outputFlag = new AtomicBoolean(false);
    public static BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) {
        // 입력 받는 쓰레드
        Thread inputThread = new Thread(() -> {
            try{
                Thread manageGame = null;
                String input;
                Boolean exit = false;
                String[] parts;
                while (!exit) {
                    BufferedReader inputReader = new BufferedReader(new FileReader("/control/input.txt"));
                    while (!(input = inputReader.lines()
                                .collect(Collectors.joining("\n"))).equals("")) {
                        FileWriter fileWriter = new FileWriter("/control/input.txt");
                        fileWriter.close();
                        if (input == null)continue;
                        if(manageGame != null && !manageGame.isAlive()){
                            System.out.println("minecraftServerProcess down in inputThread");
                            startFlag.set(false);
                            manageGame.join();
                            manageGame = null;
                            inputQueue.clear();
                        }
                        System.out.println("input : " + input);
                        parts = input.split("\\s+");
                        System.out.println("parts[0] : " + parts[0]);
                        if (parts[0].equals("start") && !startFlag.get()) {
                            System.out.println("input th start");
                            startFlag.set(true);
                            manageGame =  new Thread(new ManageGameRunnable());
                            manageGame.start();
                        } else if (manageGame != null && parts[0].equals("quit") && startFlag.get()) {
                            System.out.println("input th end");
                            startFlag.set(false);
                            inputQueue.offer("quit");
                            manageGame.join();
                            manageGame = null;
                            exit = true;
                            break;
                        } else if (parts[0].equals("input")) {
                            System.out.println("input th input");

                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < parts.length; i++) {
                                sb.append(parts[i]);
                                if (i < parts.length - 1) {
                                    sb.append(" ");
                                }
                            }
                            String combinedString = sb.toString();
                            inputQueue.offer(combinedString);

                            if(parts.length >= 2 && parts[1].equals("stop")){
                                inputQueue.clear();
                            }
                        }
                    }
                    inputReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally{
                System.out.println("End inputThread");
            }
        });

        inputThread.start();

        try {
            inputThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class ManageGameRunnable implements Runnable {
        @Override
        public void run() {

            Process minecraftServerProcess;

            try {
                BufferedReader meomoryReader = new BufferedReader(new FileReader("/control/meomory.txt"));

                // 파일의 한 줄을 읽어서 전체 명령어 문자열로 저장
                String cmd = meomoryReader.readLine();
                // BufferedReader 닫기
                meomoryReader.close();

                minecraftServerProcess = new ProcessBuilder(cmd.split(",")).directory(new File("/server/")).start();

            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            try{
                String[] parts;
                PrintWriter serverInput = new PrintWriter(new OutputStreamWriter(minecraftServerProcess.getOutputStream()), true);
                outputFlag.set(true);
                
                Thread runningPrintThr = new Thread(new RunningPrintThrRunnable(minecraftServerProcess));
                runningPrintThr.start();
                while (true) {
                    // 사용자로부터 명령어 입력 받기

                    if(!minecraftServerProcess.isAlive()){
                        System.out.println("minecraftServerProcess down in ManageGameRunnable");
                        outputFlag.set(false);
                        runningPrintThr.join();
                        startFlag.set(false);
                        break;
                    }
                    String input;
                    input = inputQueue.poll(); // 큐에서 요소를 꺼내오고, 비어있으면 null 반환
                    if (input == null) {
                        continue; // 큐가 비어있으면 다음 반복으로 넘어감
                    }

                    // 첫 번째 공백의 인덱스 찾기
                    int firstSpaceIndex = input.indexOf(' ');
                    // 공백이 없는 경우 무시
                    if (firstSpaceIndex == -1) {
                        System.out.println("Invalid input format");
                        continue;
                    }
                    // 나머지 부분 가져오기
                    String command = input.substring(0, firstSpaceIndex);
                    String arguments = input.substring(firstSpaceIndex + 1);

                    System.out.println("Enter " + input);

                    // 입력이 "input"이면 서버에 명령어 전달
                    if ("input".equals(command)) {
                        System.out.println("input");
                        serverInput.println(arguments);
                        if("stop".equals(arguments)){
                            System.out.println("stop in manage");
                            // 특정 배쉬를 실행해서 끝나고 난 뒤에 이 뒤에 코드를 실행하기
                            Process checkEnd = new ProcessBuilder("sh","./control/stop.sh").start();

                            // 외부 프로세스의 출력을 읽어오기 위한 BufferedReader 생성
                            BufferedReader reader = new BufferedReader(new InputStreamReader(checkEnd.getInputStream()));

                            // 외부 프로세스의 출력을 출력
                            String line;
                            while ((line = reader.readLine()) != null) {
                                System.out.println(line);
                            }

                            // 외부 프로세스가 완료될 때까지 대기
                            int exitCode = checkEnd.waitFor();
                            Thread.sleep(10000);
                            System.out.println("외부 프로세스 종료 코드: " + exitCode);
                            outputFlag.set(false);
                            runningPrintThr.join();
                            break;
                        }
                    }
                    // 입력이 "quit"이면 종료
                    else if ("quit".equals(command)) {
                        System.out.println("manage th quit");
                        minecraftServerProcess.destroy();
                        outputFlag.set(false);
                        runningPrintThr.join();
                        startFlag.set(false);
                        break;
                    }
                    
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }finally{
                System.out.println("End manageGame");
            }
        }
    }


    static class RunningPrintThrRunnable implements Runnable {
        private BufferedReader serverOutput;
    
        public RunningPrintThrRunnable(Process minecraftServerProcess) {
            this.serverOutput = new BufferedReader(new InputStreamReader(minecraftServerProcess.getInputStream()));
        }
        
        public void userList(int userCount, Set<String> userSet ,String line, String[] UserParts){
            try {
                BufferedWriter writer_user = new BufferedWriter(new FileWriter("/control/user.txt"));
                // System.out.println(line);
    
                // 유저 체킹하기
                // "joined" 이벤트 처리
                if (line.contains(UserParts[1])) {
                    System.out.println(UserParts[1]);
                    // 유저 수 증가
                    userCount++;
                    // 누가 들어왔는지를 기록
                    String username = line.substring(line.lastIndexOf(UserParts[0]) + UserParts[0].length(), line.indexOf(UserParts[1]));
                    System.out.println(username);
                    userSet.add(username); // 유저 목록에 추가
                }
    
                // "left" 이벤트 처리
                if (line.contains(UserParts[3])) {
                    System.out.println(UserParts[3]);
                    // 누가 나갔는지를 찾음
                    String username = line.substring(line.lastIndexOf(UserParts[2]) + UserParts[2].length(), line.indexOf(UserParts[3]));
                    System.out.println(username);
                    if (userSet.contains(username)) {
                        // 유저 수 감소
                        userCount--;
                        // 유저 목록에서 제거
                        userSet.remove(username);
                    }
                }
    
                // 파일에 결과를 쓰기
                writer_user.write("Users : " + userCount + "\n");
                for (String user : userSet) {
                    writer_user.write(user + "\n");
                }
                writer_user.flush();
                writer_user.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try(BufferedWriter writer = new BufferedWriter(new FileWriter("/control/output.txt"))){
                BufferedReader dataSplit = new BufferedReader(new FileReader("/control/user_cmd.txt"));
                Boolean flag = true;
                String[] UserParts = null;
                int userCount = 0;
                Set<String> userSet = new HashSet<>();
                String cmd = dataSplit.readLine();
                dataSplit.close();
                if(cmd == null){
                    flag = false;
                }
                else{
                    UserParts = cmd.split(",");
                }

                while (outputFlag.get()) {
                    if (serverOutput.ready()) {
                        String line;
                        line = serverOutput.readLine();
                        if (line == null) {
                            continue;
                        }

                        if(flag){
                            userList(userCount,userSet,line,UserParts);
                        }

                        // 게임 로그 넣기
                        writer.write(line);
                        writer.newLine();
                        writer.flush();
                    }
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }finally{
                System.out.println("End runningPrintThr");
            }
        }
    }

}
