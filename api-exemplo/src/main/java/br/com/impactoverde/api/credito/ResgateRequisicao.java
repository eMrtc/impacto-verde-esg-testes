package br.com.impactoverde.api.credito;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Payload de resgate de créditos por alimentos em um parceiro. */
public record ResgateRequisicao(
        @NotBlank(message = "A família é obrigatória") @Size(max = 80, message = "A família deve ter no máximo 80 caracteres") String familia,
        @NotBlank(message = "O parceiro é obrigatório") @Size(max = 80, message = "O parceiro deve ter no máximo 80 caracteres") String parceiro,
        @NotBlank(message = "O item é obrigatório") @Size(max = 80, message = "O item deve ter no máximo 80 caracteres") String item,
        @NotNull(message = "Os pontos são obrigatórios") @Min(value = 1, message = "Os pontos devem ser no mínimo 1") @Max(value = 100000, message = "Os pontos devem ser no máximo 100000") Integer pontos) {
}
