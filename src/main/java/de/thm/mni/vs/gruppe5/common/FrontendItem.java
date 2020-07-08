package de.thm.mni.vs.gruppe5.common;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Abstract representation of items in frontend, implemented by orders and tickets
 */
public interface FrontendItem {
    /**
     * @return whether or not the user-created item is valid
     */
    boolean isValid();

    /**
     * This method is to allow interactive item creation in frontend
     *
     * @return A user-created item
     */
    FrontendItem interactiveCreation();

    /**
     * Parse item from JSON file
     *
     * @param path file path
     * @param c    actual class of item, required by gson framework
     * @return item retrieved from JSON
     * @throws IOException in case of file system errors
     */
    static FrontendItem parseJsonFile(String path, Class<? extends FrontendItem> c) throws IOException {
        var reader = Files.newBufferedReader(Paths.get(path));
        return new Gson().fromJson(reader, c);
    }
}
