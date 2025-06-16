package com.examplehealtwatch.request;

import lombok.*;

/**
 * Klasa pomocnicza do obsługi żądań dotyczących wizyt lekarskich.
 * Przechowuje dane przesyłane przez użytkownika podczas dodawania nowej wizyty.
 * Ułatwia mapowanie danych wejściowych na encję Appointment.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class AppointmentRequest {

    private String doctorName;
    private String date;
    private String time;
    private String notes;

}
