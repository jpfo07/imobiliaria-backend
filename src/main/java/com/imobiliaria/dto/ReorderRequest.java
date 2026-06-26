package com.imobiliaria.dto;

import lombok.Data;
import java.util.List;

/**
 * Payload enviado pelo admin para reordenar imóveis.
 * orderedIds: lista de IDs na nova ordem desejada (índice 0 = primeiro a aparecer).
 */
@Data
public class ReorderRequest {
    private List<Long> orderedIds;
}
