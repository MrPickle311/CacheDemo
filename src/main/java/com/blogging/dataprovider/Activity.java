package com.blogging.dataprovider;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Activity implements Serializable {
    private String userId;
    private String action;
    private LocalDateTime timestamp;
}
