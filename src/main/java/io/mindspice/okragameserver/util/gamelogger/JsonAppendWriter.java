package io.mindspice.okragameserver.util.gamelogger;

import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.core.Settings;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collections;
import java.util.UUID;


public class JsonAppendWriter {
    private final RandomAccessFile file;

    public JsonAppendWriter(UUID uuid) throws IOException {
            file = new RandomAccessFile(Settings.GET().gameLogPath + uuid.toString() + ".json", "rw");
            file.writeBytes("[]");
    }

    public void writeLog(RoundRecord roundRecord) throws IOException {
        var json = JsonUtils.writeString(Collections.singletonMap(roundRecord.roundNumber(),roundRecord));
        file.seek(file.length() - 1);

        if (file.length() > 2) {
            file.writeBytes("," + json);
        } else {
            file.writeBytes(json);
        }
        file.writeBytes("]");
    }

    public void close() throws IOException {
        file.close();
    }
}
