package de.thm.mni.vs.gruppe5.common;

import com.google.gson.Gson;

import javax.jms.JMSException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public interface FrontendItem {
    boolean isValid();
    FrontendItem interactiveCreation();

    static FrontendItem parseJsonFile(String path, Class<? extends FrontendItem> c) throws IOException {
        var reader = Files.newBufferedReader(Paths.get(path));
        return new Gson().fromJson(reader, c);
    }
}
