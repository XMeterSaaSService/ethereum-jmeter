#!/bin/bash

target=<serverIP>:8545

influxdb=localhost:8989

prev_blknum=0
total_txs=0
id=1
start=$(date +%s)

function probe {
	result=$(curl $target -sS -XPOST -d '{"jsonrpc":"2.0","method": "eth_blockNumber", "params": [], "id": '$id'}')
	((id++))
	blknum_hex=$(echo $result | jq .result | sed s/\"//g)  # get current block number
	if [[ ! "$blknum_hex" =~ ^0x ]] ; then
		echo "eth_blockNumber failed"
		return
	fi

	blknum=$(printf "%d\n" $blknum_hex)  # convert to decimal
	if [[ $prev_blknum -eq 0 ]] ; then
		echo "first loop, won't write influxdb"
	else
		new_blks=$((blknum-prev_blknum))
		if [[ $new_blks -eq 0 ]] ; then
			return # no new blocks
		fi

		new_txs=0
		for ((blk=prev_blknum+1; blk<=blknum; blk++)) ; do
			blk_hex=$(printf "0x%x\n" $blk)
			result=$(curl $target -sS -XPOST -d '{"jsonrpc":"2.0","method": "eth_getBlockTransactionCountByNumber", "params": ["'$blk_hex'"], "id": '$id'}')
			((id++))
			tx_num=$(echo $result | jq .result | sed s/\"//g)  # count tx number in current block
			if [[ "$tx_num" =~ ^0x ]] && [[ "$tx_num" != "0x0" ]] ; then
				new_txs=$((new_txs+tx_num))
			fi
		done
		total_txs=$((total_txs+new_txs))
		now=$(date +%s)
		#tps_for_run=$(echo "$total_txs $now $start" | awk '{printf "%.2f", $1/($2-$3)}')
		curl -XPOST "http://$influxdb/write?db=ethereum" -u root:root -d \
			"stat,host=sut1 new_blks=$new_blks,new_txs=$new_txs,total_txs=$total_txs"
		echo "total new transactions in block $((prev_blknum+1)) .. $blknum : $new_txs, total_txs=$total_txs"
	fi

	prev_blknum=$blknum
}

######### main ################
while true ; do
	probe
	sleep 5
done

