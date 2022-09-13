package server_part;

import common.FileMessage;

public interface SaveFileCallback {

    void call(FileMessage fileMessage);
}
