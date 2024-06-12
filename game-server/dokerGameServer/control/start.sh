timeout 5m tail -n 5 -f /control/output.txt | {
    flag=0
    while IFS= read -r line; do
        # echo "$line"
        if [[ "$line" == *"Done"* ]]; then
            echo "startAck"
            pkill -P $$ tail  # 현재 스크립트의 부모 프로세스로 속한 tail 프로세스를 종료
            flag=1
            break
        fi
    done
    if [ $flag -eq 0 ]; then
        echo "startERR"  # 2분 동안 실패한 경우
    fi
}