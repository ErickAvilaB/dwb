package com.product.common.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;

import com.product.exception.ApiException;

class ProductImageStorageTest {

    @TempDir
    Path tempDirectory;

    @Test
    void guardaConsultaYEliminaImagenPng() throws Exception {
        ProductImageStorage storage = new ProductImageStorage(tempDirectory.toString(), "img");
        byte[] bytes = new byte[] {
                (byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10, 0, 0, 0, 0
        };
        String encoded = Base64.getEncoder().encodeToString(bytes);

        String path = storage.save("data:image/png;base64," + encoded);
        assertTrue(path.startsWith("/product/"));
        assertArrayEquals(bytes, Base64.getDecoder().decode(storage.read(path)));

        Path savedFile = tempDirectory.resolve("img/product")
                .resolve(path.substring("/product/".length()));
        assertTrue(Files.exists(savedFile));
        storage.delete(path);
        assertFalse(Files.exists(savedFile));
    }

    @Test
    void rechazaDatosQueNoSonUnaImagen() {
        ProductImageStorage storage = new ProductImageStorage(tempDirectory.toString(), "img");
        String encoded = Base64.getEncoder().encodeToString("texto".getBytes());

        ApiException exception = assertThrows(ApiException.class,
                () -> storage.save(encoded));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }
}
