package com.mashro3ai.business;

import java.util.UUID;

/** Replaced by an authenticated membership resolver in the JWT phase. */
public interface CurrentBusinessProvider {
    UUID currentBusinessId();
}
