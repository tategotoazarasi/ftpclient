package cn.jsou.ftpclient.ftp;

import java.net.Socket;

public interface ConnectionHandler {
	void handleConnection(Socket socket);
}
