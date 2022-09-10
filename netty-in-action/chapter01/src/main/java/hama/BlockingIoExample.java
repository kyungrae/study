package hama;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class BlockingIoExample {
    public void server(int portNumber) throws IOException {
        ServerSocket serverSocket = new ServerSocket(portNumber);
        Socket clientSocket = serverSocket.accept();

        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        String request, response;
        while ((request = in.readLine()) != null) {
            if ("Done".equals(request)) break;
            response = processRequest();
            out.println(response);
        }
    }

    private String processRequest() {
        return "Processed";
    }
}
