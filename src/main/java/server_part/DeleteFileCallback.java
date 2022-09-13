package server_part;

import common.DeleteFileMessage;

public interface DeleteFileCallback {

    void call(DeleteFileMessage dfm);
}
