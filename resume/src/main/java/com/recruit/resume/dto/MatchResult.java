package com.recruit.resume.dto;

import lombok.Data;

import java.util.List;

@Data
public class MatchResult {
    private Integer score;
    private String summary;
    private List<String> strengths;
    private List<String> gaps;
}
