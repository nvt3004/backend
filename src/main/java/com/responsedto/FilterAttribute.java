package com.responsedto;

import java.util.List;

import com.entities.AttributeOption;
import com.entities.Category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterAttribute {
    private Att attribute;
    private List<Category> categories;
}

