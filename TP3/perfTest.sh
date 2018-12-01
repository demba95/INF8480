begin=$(date +%s)

for i in {1..30}
do 
	wget -qO- 132.207.12.224:8080
done
wait
end=$(date +%s)
duration=$(($end-$begin))
echo "Time elapsed: $duration seconds"
