package common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FileMessage implements Serializable {

    private File file;
    private String name;
    private long size;
    private String fileOwner;

}