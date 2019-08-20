/**
 * Interface pertaining to whether the implementation can persist to the local disk
 */
package name.chen.dave.api;

import java.io.IOException;

public interface DiskPersistable {

    void persistToLocalDisk() throws IOException;
}
