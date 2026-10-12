package com.bovigestor.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

public record BovineInternalCode(String value) {

    private static final Pattern FORMAT = Pattern.compile(
            "[A-Z]{3,4}-[0-9]{2}-(?=[0-9]*[1-9])[0-9]{4,}"
    );

    public BovineInternalCode {
        Objects.requireNonNull(value, "O código interno do bovino é obrigatório");

        if (!FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "O código interno deve seguir o formato SERIE-AA-SEQUENCIA, com série de 3 a 4 letras maiúsculas e sequência positiva de pelo menos quatro dígitos"
            );
        }
    }
}
