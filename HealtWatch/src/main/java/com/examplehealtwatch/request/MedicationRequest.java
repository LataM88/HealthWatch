package com.examplehealtwatch.request;

import lombok.*;

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
