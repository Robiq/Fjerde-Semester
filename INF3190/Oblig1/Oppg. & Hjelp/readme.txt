


  After downloading, unzip the files.
  For Qemu:
    cd qemu
    ./configure
    make

  Run the following command to start MininetTestbed.
   
   ./qemu/x86_64-softmmu/qemu-system-x86_64 -hda MininetTestbed.qcow2

   It runs in recovery mode, so enter resume once it asks.
   After booting, we will get the login:
   Root user: root
   Password:  mininet
   Normal user: mininet
   Password:    mininet 

   Good Luck!