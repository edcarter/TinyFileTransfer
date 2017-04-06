# TinyFileTransfer
File transfer utility leveraging the Tiny Encryption Algorithm to encrypt and decrypt communications.
A shared secret key is obtained by the client and server through the Diffie-Hellman key exchange method.
User credentials are salted, hashed, and then stored in a shadow file. Each client must enter their credentials which are then verified on the server by referencing the shadow file.
The client can then request files from the server which are sent over the encrypted connection.
Files are limited to 100MB in size for now since the whole file is loaded into memory before transferring to the client.

## Building
    make clean
    make
    
## Running
You need to either put libtea.so in your $LD_LIBRARY_PATH or extend the src directory to be in the $LD_LIBRARY_PATH by running this command in the source directory:

    export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:.

### Server
    java Server [my_port] [directory_to_serve_files_from]
    
### Client
    java Client [server_hostname] [server_port]
    
## Warning
Do not use this for actual security applications, the TEA method has known vulnerabilites and hash collisions
