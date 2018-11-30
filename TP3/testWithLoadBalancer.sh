begin=$(date +%s)

for i in {1..30}
do 
	wget -qO- 172.15.128.251:8080
done
wait
end=$(date +%s)
duration=$(($end-$begin))
echo "Time elapsed: $duration seconds"