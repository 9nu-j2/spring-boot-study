package com.kt.mytestapi.lectures;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LectureTest {
    @Test
    public void builder() {

        Lecture lecture = Lecture.builder()
                .name("Spring REST API")
                .description("REST API developmemt with Spring")
                .build();
        assertEquals("Spring REST API", lecture.getName());

    }
}