package br.com.impactoverde.api.coleta;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Payload de criação de coleta (POST). */
public record ColetaRequisicao(
        @NotBlank(message = "A família é obrigatória") @Size(max = 80, message = "A família deve ter no máximo 80 caracteres") String familia,
        @NotNull(message = "O tipo de resíduo é obrigatório") TipoResiduo tipoResiduo,
        @NotNull(message = "A quantidade de unidades é obrigatória") @Min(value = 1, message = "A quantidade deve ser no mínimo 1") @Max(value = 1000, message = "A quantidade deve ser no máximo 1000") Integer quantidadeUnidades,
        @NotNull(message = "O peso em kg é obrigatório") @DecimalMin(value = "0.01", message = "O peso deve ser maior que zero") @DecimalMax(value = "500.0", message = "O peso deve ser no máximo 500 kg") Double pesoKg,
        @NotBlank(message = "O ponto de coleta é obrigatório") @Size(max = 80, message = "O ponto de coleta deve ter no máximo 80 caracteres") String pontoColeta) {
}
