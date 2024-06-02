import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class TerrariaServerManager {

    public static AtomicBoolean startFlag = new AtomicBoolean(false); // 서버 시작 여부를 나타내는 플래그
    public static AtomicBoolean outputFlag = new AtomicBoolean(false); // 서버 출력 여부를 나타내는 플래그
    public static BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>(); // 입력 명령어를 저장하는 큐

    public static void main(String[] args) {
        // 설정 파일이 존재하는지 확인하고, 없으면 생성
        ensureConfigFilesExist();

        // Worlds 폴더를 설정 디렉토리로 링크
        linkWorldsFolder();

        // 입력을 처리하는 스레드
        Thread inputThread = new Thread(() -> {
            try {
                Thread manageGame = null;
                String input;
                boolean exit = false;
                String[] parts;
                while (!exit) {
                    BufferedReader inputReader = new BufferedReader(new FileReader("/control/input.txt"));
                    while (!(input = inputReader.lines().collect(Collectors.joining("\n"))).isEmpty()) {
                        try (FileWriter fileWriter = new FileWriter("/control/input.txt")) {
                            fileWriter.write(""); // 입력 파일을 비움
                        }
                        if (manageGame != null && !manageGame.isAlive()) {
                            System.out.println("TerrariaServerProcess down in inputThread");
                            manageGame.join();
                            startFlag.set(false);
                            manageGame = null;
                            inputQueue.clear();
                        }

                        if (input == null) continue;

                        System.out.println("input : " + input);
                        parts = input.split("\\s+");
                        System.out.println("parts[0] : " + parts[0]);

                        if (parts[0].equals("start") && !startFlag.get()) {
                            System.out.println("input th start");
                            startFlag.set(true);
                            manageGame = new Thread(new ManageGameRunnable());
                            manageGame.start();
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
                        }
                    }
                    inputReader.close();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
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

    static void ensureConfigFilesExist() {
        // 설정 파일이 존재하는지 확인하고, 없으면 기본 설정 파일을 복사
        try {
            File configFile = new File("/config/serverconfig.txt");
            if (!configFile.exists()) {
                Files.copy(Paths.get("./serverconfig-default.txt"), configFile.toPath());
            }

            File banlistFile = new File("/config/banlist.txt");
            if (!banlistFile.exists()) {
                banlistFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void linkWorldsFolder() {
        // Worlds 폴더를 설정 디렉토리로 링크
        try {
            File worldsFolder = new File("/root/.local/share/Terraria/Worlds");
            if (!worldsFolder.exists() || worldsFolder.list().length == 0) {
                new File("/root/.local/share/Terraria").mkdirs();
                Files.createSymbolicLink(worldsFolder.toPath(), Paths.get("/config"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ManageGameRunnable implements Runnable {
        @Override
        public void run() {
            Process terrariaServerProcess;
            String cmd = "./TerrariaServer -x64 -config /config/serverconfig.txt -banlist /config/banlist.txt";

            try (BufferedReader meomoryReader = new BufferedReader(new FileReader("/control/meomory.txt"))) {
                String world = System.getenv("world");
                if (world != null && !world.isEmpty() && new File("/config/" + world).exists()) {
                    cmd += " -world /config/" + world;
                }

                terrariaServerProcess = new ProcessBuilder(cmd.split(" ")).directory(new File("/server/")).start();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            try {
                PrintWriter serverInput = new PrintWriter(new OutputStreamWriter(terrariaServerProcess.getOutputStream()), true);
                outputFlag.set(true);

                Thread runningPrintThread = new Thread(new RunningPrintThreadRunnable(terrariaServerProcess));
                runningPrintThread.start();
                while (true) {
                    if (!terrariaServerProcess.isAlive()) {
                        System.out.println("TerrariaServerProcess down in ManageGameRunnable");
                        outputFlag.set(false);
                        runningPrintThread.join();
                        startFlag.set(false);
                        break;
                    }

                    String input = inputQueue.poll();
                    if (input == null) {
                        continue;
                    }

                    int firstSpaceIndex = input.indexOf(' ');
                    if (firstSpaceIndex == -1) {
                        System.out.println("Invalid input format");
                        continue;
                    }

                    String command = input.substring(0, firstSpaceIndex);
                    String arguments = input.substring(firstSpaceIndex + 1);

                    if ("input".equals(command)) {
                        serverInput.println(arguments);
                        if ("stop".equals(arguments)) {
                            System.out.println("stop in manage");
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("End manageGame");
            }
        }
    }

    static class RunningPrintThreadRunnable implements Runnable {
        private BufferedReader serverOutput;

        public RunningPrintThreadRunnable(Process terrariaServerProcess) {
            this.serverOutput = new BufferedReader(new InputStreamReader(terrariaServerProcess.getInputStream()));
        }

        @Override
        public void run() {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("/control/output.txt"))) {
                while (outputFlag.get()) {
                    if (serverOutput.ready()) {
                        String line = serverOutput.readLine();
                        if (line == null) {
                            continue;
                        }

                        writer.write(line);
                        writer.newLine();
                        writer.flush();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                System.out.println("End runningPrintThread");
            }
        }
    }
}
