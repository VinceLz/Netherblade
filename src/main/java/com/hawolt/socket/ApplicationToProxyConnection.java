package com.hawolt.socket;

import com.hawolt.logger.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created: 07/03/2023 17:46
 * Author: Twitter @hawolt
 **/

public class ApplicationToProxyConnection extends SocketConnection {

    public ApplicationToProxyConnection(SocketCallback callback, Socket in, Socket out) {
        super(callback, in, out);
    }

    @Override
    public void run() {
        SocketDataSpoofer spoofer = callback.getSpoofer();
        SocketInterceptor interceptor = callback.getInterceptor();
        try (InputStream input = in.getInputStream()) {
            OutputStream stream = out.getOutputStream();
            int code;
            while (in.isConnected() && out.isConnected() && (code = input.read()) != -1) {
                byte[] original = read(input, code, input.available());
                try {
                    if (interceptor != null) interceptor.sniffOriginalClient(original);
                    byte[] modified = spoofer.onApplicationData(original);
                    if (modified == null) continue;
                    if (interceptor != null) interceptor.sniffSpoofedClient(modified);
                    stream.write(modified);
                } catch (Exception e) {
                    stream.write(original);
                }
            }
        } catch (IOException e) {
            Logger.error(e);
        }
    }
}