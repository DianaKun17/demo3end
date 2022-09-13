package common;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.Serializable;

@AllArgsConstructor
@Data
public class DeleteFileMessage implements Serializable {

    private String name;

}
