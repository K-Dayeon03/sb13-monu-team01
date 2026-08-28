package com.project.monu.global.logging;

import java.nio.file.Path;
import java.time.LocalDate;

public interface LogArchiveStorage {

    ArchivedLog archive(LocalDate logDate, Path sourceFile);

    record ArchivedLog(LocalDate logDate, String location) {
    }
}
