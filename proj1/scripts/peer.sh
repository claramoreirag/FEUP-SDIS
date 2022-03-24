#! /usr/bin/bash

# Script for running a peer
# To be run in the root of the build tree
# No jar files used
# Assumes that Peer is the main class 
#  and that it belongs to the peer package
# Modify as appropriate, so that it can be run 
#  from the root of the compiled tree

# Check number input arguments
argc=$#

if (( argc != 9 )) 
then
	echo "Usage: $0 <version> <peer_id> <svc_access_point> <mc_addr> <mc_port> <mdb_addr> <mdb_port> <mdr_addr> <mdr_port>"
	exit 1
fi

# Assign input arguments to nicely named variables

ver=$1
id=$2
sap=$3
mc_addr=$4
mc_port=$5
mdb_addr=$6
mdb_port=$7
mdr_addr=$8
mdr_port=$9

# Execute the program
# Should not need to change anything but the class and its package, unless you use any jar file

# echo "java peer.Peer ${ver} ${id} ${sap} ${mc_addr} ${mc_port} ${mdb_addr} ${mdb_port} ${mdr_addr} ${mdr_port}"

java Peer ${ver} ${id} ${sap} ${mc_addr} ${mc_port} ${mdb_addr} ${mdb_port} ${mdr_addr} ${mdr_port}

