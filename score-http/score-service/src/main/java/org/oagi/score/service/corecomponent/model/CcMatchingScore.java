package org.oagi.score.service.corecomponent.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CcMatchingScore<T extends Object> {

    private double score;

    private T source;

    private T target;

}
