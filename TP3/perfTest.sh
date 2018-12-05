#!/bin/bash
begin=$(date +%s)

for i in {1..30}
do 
	wget -qO- $1
done
wait
end=$(date +%s)
duration=$(($end-$begin))
echo "Time elapsed: $duration seconds"
