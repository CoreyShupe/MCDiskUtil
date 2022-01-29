package io.github.coreyshupe.mcdiskutil;

import com.google.common.collect.Lists;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

@SuppressWarnings("ClassCanBeRecord")
public class DiskUtilMessenger<T extends Audience> {
    private final static double KB_SIZE = 1000.0;
    private final static double MB_SIZE = 1000000.0;
    private final static double GB_SIZE = 1000000000.0;

    private final static double FD_SIZE = 1024.0;
    private final static String BASE_PATH = "/home/minecraft/server".replace('/', File.separatorChar);
    private final static int RAW_LENGTH = BASE_PATH.length();
    private final static Component DISK_UTIL_PREFIX = Component.text("[DiskUtil] ", NamedTextColor.DARK_AQUA);
    private final BiFunction<T, String[], Boolean> filePermissionsCheck;

    public DiskUtilMessenger(BiFunction<T, String[], Boolean> filePermissionsCheck) {
        this.filePermissionsCheck = filePermissionsCheck;
    }

    private static Component withPrefix(Component component) {
        return Component.join(JoinConfiguration.noSeparators(), DISK_UTIL_PREFIX, component);
    }

    private static double parseFileSize(File directory) throws IOException {
        if (directory.isFile()) {
            return Files.size(directory.toPath());
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return 0;
        }
        if (files.length == 0) {
            return FD_SIZE;
        }
        double size = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                size += parseFileSize(file);
            } else {
                size += Files.size(file.toPath());
            }
        }
        return size + FD_SIZE;
    }

    private static String getReadableSize(double size) {
        if (size >= GB_SIZE) {
            return "%sGb".formatted(size / GB_SIZE);
        } else if (size >= MB_SIZE) {
            return "%sMb".formatted(size / MB_SIZE);
        } else if (size >= KB_SIZE) {
            return "%sKb".formatted(size / KB_SIZE);
        } else {
            return "%s bytes".formatted(size);
        }
    }

    private static Component componentFromFileType(File f) {
        if (f.isFile()) {
            return Component.text("[FILE]", NamedTextColor.RED);
        } else {
            return Component.text("[DIRECTORY]", NamedTextColor.RED);
        }
    }

    private static Component componentFromFile(File f) {
        try {
            return Component.text("<file_prefix> (<file_path>) is <file_size>", NamedTextColor.AQUA)
                    .replaceText(TextReplacementConfig.builder()
                            .matchLiteral("<file_prefix>")
                            .replacement(componentFromFileType(f))
                            .build())
                    .replaceText(TextReplacementConfig.builder()
                            .matchLiteral("<file_path>")
                            .replacement(Component.text(f.getPath(), NamedTextColor.GRAY))
                            .build())
                    .replaceText(TextReplacementConfig.builder()
                            .matchLiteral("<file_size>")
                            .replacement(Component.text(getReadableSize(parseFileSize(f)), NamedTextColor.GRAY))
                            .build());
        } catch (Exception exception) {
            return Component.text("Failed to resolve (%s)".formatted(f.getPath()), NamedTextColor.RED);
        }
    }

    public void display(T executor, String filePath) {
        File file = new File(BASE_PATH, filePath);
        try {
            String truePath = file.getCanonicalPath();
            if (!truePath.contains(BASE_PATH)) {
                executor.sendMessage(withPrefix(Component.text(
                        "Resolved file path read outside of %s.".formatted(BASE_PATH),
                        NamedTextColor.RED
                )));
                return;
            }
            if (!filePermissionsCheck.apply(executor, generateRawPermissionComponents(truePath))) {
                executor.sendMessage(withPrefix(Component.text(
                        "You do not have permission to read this path.",
                        NamedTextColor.RED
                )));
                return;
            }
        } catch (IOException | SecurityException e) {
            executor.sendMessage(withPrefix(Component.text(
                    "Could not access file path for %s, [%s]".formatted(filePath, e.getMessage()),
                    NamedTextColor.RED
            )));
            return;
        }

        if (!file.exists()) {
            executor.sendMessage(withPrefix(Component.text(
                    "File or folder does not exist.",
                    NamedTextColor.RED
            )));
            return;
        }

        Component header = withPrefix(Component.text("Disk Information (%s)".formatted(filePath), NamedTextColor.AQUA));

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                executor.sendMessage(Component.join(JoinConfiguration.separator(Component.newline()),
                        header,
                        Component.text("- File list returned null on directory.", NamedTextColor.RED)
                ));
                return;
            }
            if (file.length() == 0) {
                executor.sendMessage(Component.join(JoinConfiguration.separator(Component.newline()),
                        header,
                        Component.text("- File list returned empty on directory.", NamedTextColor.RED)
                ));
                return;
            }
            List<Component> components = Lists.newArrayList(header);
            components.addAll(Arrays.stream(files).map(DiskUtilMessenger::componentFromFile).toList());
            executor.sendMessage(Component.join(JoinConfiguration.separator(Component.newline()), components));
        } else {
            executor.sendMessage(withPrefix(componentFromFile(file)));
        }
    }

    private static String[] generateRawPermissionComponents(String filePath) {
        filePath = filePath.substring(RAW_LENGTH);
        if (filePath.length() == 0) {
            return new String[]{};
        } else {
            filePath = filePath.substring(1);
        }
        return filePath.split(Pattern.quote(File.pathSeparator));
    }
}
