package com.blogging.dataprovider;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product  implements Serializable {
    private String id;
    private String name;
    private Double price;
    private String category;
}
