package com.blogging.dataprovider;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Visitor  implements Serializable {
    private String id;
    private LocalDate visitDate;
}
