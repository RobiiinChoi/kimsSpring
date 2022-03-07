package com.study.querydsl.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

// gradle > tasks > other > compileQuerydsl

@Entity
@Getter @Setter
public class Hello {

    @Id @GeneratedValue
    private Long id;
}
