package com.mycompany.levelup_code.service;

/** Define el cálculo del nivel según la experiencia acumulada. */
public interface LevelPolicy {

    int levelForExperience(int totalExperience);
}
