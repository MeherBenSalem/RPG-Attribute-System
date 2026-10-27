package tn.nightbeam.ras.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Reads and writes RAS config files the way people actually edit them. Windows editors often save as ANSI
 * (cp1252), where the default files' {@code §} color codes are not valid UTF-8, and hand edits often leave a
 * trailing comma. Both used to make a file unreadable, and RAS then rebuilt it from defaults.
 */
public final class ConfigFiles {
    private static final Charset ANSI = Charset.isSupported("windows-1252") ? Charset.forName("windows-1252")
            : StandardCharsets.ISO_8859_1;

    private ConfigFiles() {
    }

    /** UTF-8 (with or without a BOM), falling back to ANSI for files a Windows editor re-encoded. */
    public static String readText(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        String text;
        try {
            text = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException notUtf8) {
            text = new String(bytes, ANSI);
        }
        return !text.isEmpty() && text.charAt(0) == '﻿' ? text.substring(1) : text;
    }

    /** Parses a JSON object; a trailing comma before a closing bracket is accepted, and a blank file is empty. */
    public static JsonObject parseObject(String text) {
        if (text.isBlank()) {
            return new JsonObject();
        }
        JsonElement root;
        try {
            root = JsonParser.parseString(text);
        } catch (JsonParseException strictError) {
            String repaired = withoutTrailingCommas(text);
            if (repaired.equals(text)) {
                throw strictError;
            }
            try {
                root = JsonParser.parseString(repaired);
            } catch (JsonParseException ignored) {
                throw strictError;
            }
        }
        if (root == null || !root.isJsonObject()) {
            throw new JsonParseException("the file must contain a JSON object");
        }
        return root.getAsJsonObject();
    }

    public static JsonObject readObject(Path path) throws IOException {
        return parseObject(readText(path));
    }

    /** Writes UTF-8 through a temporary sibling, so a crash mid-write never leaves a truncated config. */
    public static void writeText(Path path, String text) throws IOException {
        Files.createDirectories(path.getParent());
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        Files.writeString(temporary, text, StandardCharsets.UTF_8);
        try {
            try {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException moveFailed) {
            // Windows refuses to replace a file another program holds open; write it in place instead.
            Files.deleteIfExists(temporary);
            Files.writeString(path, text, StandardCharsets.UTF_8);
        }
    }

    /** Drops each comma (outside strings) whose next significant character closes an object or array. */
    static String withoutTrailingCommas(String text) {
        StringBuilder out = new StringBuilder(text.length());
        boolean inString = false;
        char quote = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inString) {
                out.append(c);
                if (c == '\\' && i + 1 < text.length()) {
                    out.append(text.charAt(++i));
                } else if (c == quote) {
                    inString = false;
                }
                continue;
            }
            if (c == '"' || c == '\'') {
                inString = true;
                quote = c;
            } else if (c == ',') {
                int next = i + 1;
                while (next < text.length() && Character.isWhitespace(text.charAt(next))) {
                    next++;
                }
                if (next < text.length() && (text.charAt(next) == '}' || text.charAt(next) == ']')) {
                    continue;
                }
            }
            out.append(c);
        }
        return out.toString();
    }
}
