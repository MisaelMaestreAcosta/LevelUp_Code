package com.mycompany.levelup_code.domain.model;

import com.mycompany.levelup_code.domain.enums.PublicationState;

/** Administrative data. Reference solutions are never part of the student Challenge model. */
public record AuthoredChallenge(Challenge challenge, String referenceSolution,
        PublicationState state, int revision) { }
