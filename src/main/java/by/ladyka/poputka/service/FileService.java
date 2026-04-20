package by.ladyka.poputka.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

    @Value("${application.storage.documents}")
    private String documentStorage;

    public String saveFile(MultipartFile file, String... path) {
        // Генерация уникального имени файла
        String uniqueFileName = "%s_%s".formatted(UUID.randomUUID(), file.getOriginalFilename());

        // Путь к папке для сохранения файла
        Path directoryPath = Paths.get(documentStorage, path);

        // Проверка, существует ли папка, если нет - создаем
        if (!Files.exists(directoryPath)) {
            try {
                Files.createDirectories(directoryPath);
            } catch (IOException e) {
                throw new RuntimeException("Не удалось создать папку: " + directoryPath, e);
            }
        }

        // Путь к файлу
        Path filePath = directoryPath.resolve(uniqueFileName);

        // Сохранение файла
        try {
            file.transferTo(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить файл: " + uniqueFileName, e);
        }

        // Возвращаем уникальное имя файла
        return uniqueFileName;
    }
}
