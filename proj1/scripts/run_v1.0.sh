cd ../src/build

osascript -e 'tell application "Terminal"' -e 'do script "cd '"$(pwd) && rmiRegistry"\" -e 'end tell'

osascript -e 'tell application "Terminal"' -e 'do script "cd '"$(pwd) && java Peer 1.0 0 peer0 224.0.0.1 3456 224.0.0.2 3457 224.0.0.3 3458"\" -e 'end tell'

osascript -e 'tell application "Terminal"' -e 'do script "cd '"$(pwd) && java Peer 1.0 1 peer1 224.0.0.1 3456 224.0.0.2 3457 224.0.0.3 3458"\" -e 'end tell'

osascript -e 'tell application "Terminal"' -e 'do script "cd '"$(pwd) && java Peer 1.0 2 peer2 224.0.0.1 3456 224.0.0.2 3457 224.0.0.3 3458"\" -e 'end tell'

sleep 1

java TestApp peer0 BACKUP text.txt 1
