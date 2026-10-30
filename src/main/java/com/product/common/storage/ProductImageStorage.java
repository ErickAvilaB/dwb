package com.product.common.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.product.exception.ApiException;

/** Almacena archivos en uploads/img/product. La BD solamente guarda su ruta relativa. */
@Component
public class ProductImageStorage {

    private static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024;
    private final Path imageDirectory;

    public ProductImageStorage(
            @Value("${app.upload.dir}") String uploadDir,
            @Value("${app.upload.images}") String uploadImages) {
        imageDirectory = Paths.get(uploadDir, uploadImages, "product")
                .toAbsolutePath().normalize();
    }

    public String save(String imageBase64) {
        byte[] bytes = decodeImage(imageBase64);
        String extension = detectExtension(bytes);
        String fileName = UUID.randomUUID() + extension;
        Path destination = imageDirectory.resolve(fileName);

        try {
            Files.createDirectories(imageDirectory);
            Files.write(destination, bytes,
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            return "/product/" + fileName;
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible guardar la imagen del producto");
        }
    }

    public String read(String relativePath) {
        Path file = resolve(relativePath);
        try {
            if (!Files.isRegularFile(file) || Files.size(file) > MAX_IMAGE_BYTES) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "El archivo de imagen no está disponible");
            }
            return Base64.getEncoder().encodeToString(Files.readAllBytes(file));
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible leer la imagen del producto");
        }
    }

    public void delete(String relativePath) {
        try {
            Files.deleteIfExists(resolve(relativePath));
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible eliminar el archivo de imagen");
        }
    }

    public void deleteQuietly(String relativePath) {
        try {
            Files.deleteIfExists(resolve(relativePath));
        } catch (IOException ignored) {
            // Limpieza compensatoria: no ocultar el error original de base de datos.
        }
    }

    private Path resolve(String relativePath) {
        if (relativePath == null || !relativePath.startsWith("/product/")) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "La ruta almacenada de la imagen no es válida");
        }

        String fileName = relativePath.substring("/product/".length());
        if (fileName.isBlank() || !fileName.matches("[a-zA-Z0-9-]+\\.(png|jpg|gif|webp)")) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "La ruta almacenada de la imagen no es válida");
        }
        return imageDirectory.resolve(fileName).normalize();
    }

    private byte[] decodeImage(String image) {
        String encoded = image.trim();
        if (encoded.startsWith("data:")) {
            int comma = encoded.indexOf(',');
            if (comma < 0 || !encoded.substring(0, comma).matches(
                    "data:image/(png|jpeg|gif|webp);base64")) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "La imagen debe estar codificada en Base64");
            }
            encoded = encoded.substring(comma + 1);
        }

        if (encoded.isBlank() || encoded.length() > MAX_IMAGE_BYTES * 4 / 3 + 16) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "La imagen está vacía o supera el límite de 5 MB");
        }

        try {
            byte[] bytes = Base64.getDecoder().decode(encoded);
            if (bytes.length == 0 || bytes.length > MAX_IMAGE_BYTES) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "La imagen está vacía o supera el límite de 5 MB");
            }
            return bytes;
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "La imagen no contiene un Base64 válido");
        }
    }

    private String detectExtension(byte[] bytes) {
        // Comprobamos la firma del archivo, no únicamente la extensión anunciada.
        if (bytes.length >= 8 && (bytes[0] & 0xff) == 0x89
                && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G'
                && bytes[4] == 0x0d && bytes[5] == 0x0a
                && bytes[6] == 0x1a && bytes[7] == 0x0a) {
            return ".png";
        }
        if (bytes.length >= 3 && (bytes[0] & 0xff) == 0xff
                && (bytes[1] & 0xff) == 0xd8 && (bytes[2] & 0xff) == 0xff) {
            return ".jpg";
        }
        if (bytes.length >= 6 && bytes[0] == 'G' && bytes[1] == 'I'
                && bytes[2] == 'F' && bytes[3] == '8'
                && (bytes[4] == '7' || bytes[4] == '9') && bytes[5] == 'a') {
            return ".gif";
        }
        if (bytes.length >= 12 && bytes[0] == 'R' && bytes[1] == 'I'
                && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E'
                && bytes[10] == 'B' && bytes[11] == 'P') {
            return ".webp";
        }
        throw new ApiException(HttpStatus.BAD_REQUEST,
                "Solo se aceptan imágenes PNG, JPEG, GIF o WebP");
    }
}
