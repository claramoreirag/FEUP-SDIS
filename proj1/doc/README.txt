->Compiling and running instructions

Compiling:
 - Run script located in scripts/compile.sh which will compile the code from the src/ folder. It places the compiled code under a subdirectory named "build".

Pre-running:
 - Start the RMI registry using the script scripts/rmi.sh, it will open a terminal with the RMI registry running. It can take the port as argument.

Run a peer:
 - Run script located in scripts/peer.sh from the build/ folder. It takes as arguments the arguments specified in the last paragraph before Section 3.1 of the specification, in the same order. When executed from the root of the build tree, it shall start the execution of the peer instance with the specified id (and arguments).

Run testApp:
 - Run script located in scripts/test.sh from the build/ folder.  This script takes as arguments the arguments of the TestApp class, as specified in Section 6 of the specification, in the same order. When executed from the root of the build tree, it shall start the execution of the TestApp, with the specified arguments.

Clean peer directory:
 - Run script located in scripts/cleanup.sh from the build/ folder. This script takes as argument the peer id. When executed from the root of the build tree, it shall clean up the directory tree used by that peer to save the chunks sent by remote peers and the files it has recovered.

Starting multiple peers with version 1.0 :
 - Run script located in scripts/run_v1.0.sh from the build/ folder. This script will start the peers in separate terminals. The title of each terminal will have the protocol version and the peer ID/access point.

Starting multiple peers with version 2.0 :
 - Run script located in scripts/run_v2.0.sh from the build/ folder. This script will start the peers in separate terminals. The title of each terminal will have the protocol version and the peer ID/access point.
 