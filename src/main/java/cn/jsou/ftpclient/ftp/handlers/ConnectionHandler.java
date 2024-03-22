package cn.jsou.ftpclient.ftp.handlers;

import java.net.Socket;

public interface ConnectionHandler {
	void handleConnection(Socket socket);
}
