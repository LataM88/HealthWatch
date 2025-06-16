package com.examplehealtwatch.request;

import lombok.*;

/**
 * Klasa pomocnicza do obsługi żądań dotyczących leków.
 * Przechowuje dane przesyłane przez użytkownika podczas dodawania nowego leku.
 * Ułatwia mapowanie danych wejściowych na encję Medication.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class MedicationRequest {

    private String name;
    private String dosage;
    private String time;
    private String days;

}
