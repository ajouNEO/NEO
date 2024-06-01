FROM ubuntu:20.04

WORKDIR /server/

# 환경변수 설정
ENV DEBIAN_FRONTEND=noninteractive

# 기본 패키지 설치 및 i386 아키텍처 추가 (루트 사용자로 실행)
RUN apt-get update && \
    apt-get install -y sudo curl software-properties-common wget git locales dialog nano software-properties-common apt-transport-https ca-certificates gnupg2 lib32gcc1 && \
    dpkg --add-architecture i386 && \
    add-apt-repository multiverse && \
    apt-get update

RUN useradd -m -d /home/steam steam && \
    echo "steam:steam" | chpasswd && \
    usermod -aG sudo steam && \
    chsh -s /bin/bash steam

USER steam

# SteamCMD 설치
RUN mkdir -p /home/steam/steamcmd && \
    cd /home/steam/steamcmd && \
    wget https://steamcdn-a.akamaihd.net/client/installer/steamcmd_linux.tar.gz && \
    tar -xvzf steamcmd_linux.tar.gz && \
    chmod +x /home/steam/steamcmd/steamcmd.sh

# 환경 변수 설정
ENV PATH="/home/steam/steamcmd:$PATH"

# SteamCMD 업데이트 및 앱 설치
RUN /home/steam/steamcmd/steamcmd.sh +login anonymous +app_update 2394010 validate +quit

# 필요한 디렉토리 생성 및 Steamworks SDK 복사
RUN mkdir -p /home/steam/.steam/sdk64/ && \
    /home/steam/steamcmd/steamcmd.sh +login anonymous +app_update 1007 validate +quit && \
    find /home/steam/steamcmd -name "steamclient.so" -exec cp {} /home/steam/.steam/sdk64/ \;


# RUN chmod +x /control/start.sh
# RUN chmod +x /control/stop.sh

WORKDIR /

CMD ["sleep", "infinity"]

# CMD [ "./server/PalServer.exe" ]

# CMD ["java", "/control/inputAndOutput.java"]
