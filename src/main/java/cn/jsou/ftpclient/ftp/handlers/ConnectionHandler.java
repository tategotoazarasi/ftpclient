package cn.jsou.ftpclient.ftp.handlers;

import java.net.Socket;
import java.util.concurrent.CompletableFuture;

public interface ConnectionHandler {
	CompletableFuture<Void> handleConnection(Socket socket);

	void waitForCompletion() throws InterruptedException;
}
