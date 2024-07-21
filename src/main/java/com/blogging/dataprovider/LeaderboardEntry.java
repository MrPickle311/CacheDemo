package com.blogging.dataprovider;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardEntry  implements Serializable {
    private String userId;
    private int score;
}