package com.studyfinder.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.studyfinder.dao.SpotPhotoDAO;

/** Students adding photos to study spots. */
public class SpotPhotoService {

    private static final String JPEG_PREFIX = "data:image/jpeg;base64,";
    /** The browser shrinks photos to about 1280px before sending, so 1.5 MB is plenty. */
    private static final int MAX_BYTES = 1_500_000;

    private final SpotPhotoDAO dao = new SpotPhotoDAO();

    public List<Map<String, Object>> getVisiblePhotos(int spotId) throws SQLException { return dao.getVisiblePhotos(spotId); }
    public List<Map<String, Object>> getRecentPhotosForAdmin() throws SQLException { return dao.getRecentPhotosForAdmin(); }

    /**
     * Checks the photo (a JPEG sent as a data URL), saves it in photoDirectory, then saves the row.
     * The file is removed again if the row is not saved.
     *
     * @return null on success, otherwise a message to show the student
     */
    public String upload(int userId, int spotId, String dataUrl, double latitude, double longitude,
            String photoDirectory) throws SQLException, IOException {
        if (dataUrl == null || !dataUrl.startsWith(JPEG_PREFIX)) return "Choose a photo first.";
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(dataUrl.substring(JPEG_PREFIX.length()));
        } catch (IllegalArgumentException exception) {
            return "That photo could not be read. Try another one.";
        }
        // JPEG files start with FF D8 FF.
        if (bytes.length < 3 || (bytes[0] & 0xFF) != 0xFF || (bytes[1] & 0xFF) != 0xD8 || (bytes[2] & 0xFF) != 0xFF) {
            return "That photo could not be read. Try another one.";
        }
        if (bytes.length > MAX_BYTES) return "That photo is too large. Try a smaller one.";
        if (photoDirectory == null) throw new IOException("Could not find the photo folder.");

        Path folder = Paths.get(photoDirectory);
        Files.createDirectories(folder);
        String fileName = UUID.randomUUID() + ".jpg";
        Path file = folder.resolve(fileName);
        Files.write(file, bytes);

        String error;
        try {
            error = dao.addPhoto(userId, spotId, latitude, longitude, fileName);
        } catch (SQLException exception) {
            Files.deleteIfExists(file);
            throw exception;
        }
        if (error != null) Files.deleteIfExists(file);
        return error;
    }
}
