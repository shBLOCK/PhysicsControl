import socket

udp = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
udp.bind(("192.168.1.3", 8560))
while True:
    print(udp.recvfrom(1024))
udp.close()
