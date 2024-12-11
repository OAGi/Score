package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.LibraryObject;

import java.math.BigInteger;

/**
 * APIs for the library management.
 */
public interface LibraryAPI {

    /**
     * Retrieves a library by its unique ID.
     *
     * @param libraryId The unique ID of the library.
     * @return The library object associated with the given ID.
     */
    LibraryObject getLibraryById(BigInteger libraryId);

    /**
     * Retrieves a library by its name.
     *
     * @param name The name of the library.
     * @return The library object associated with the given name.
     */
    LibraryObject getLibraryByName(String name);

}
