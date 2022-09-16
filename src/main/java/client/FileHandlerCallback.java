package client;

import common.FileMessage;

import java.io.IOException;

public interface FileHandlerCallback {

    void call(FileMessage fm) throws IOException;
}