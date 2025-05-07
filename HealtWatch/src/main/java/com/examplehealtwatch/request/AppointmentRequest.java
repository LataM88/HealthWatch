package com.examplehealtwatch.request;

import lombok.*;

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
