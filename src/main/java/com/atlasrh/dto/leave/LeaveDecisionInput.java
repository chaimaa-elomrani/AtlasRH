package com.atlasrh.dto.leave;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LeaveDecisionInput(@NotNull Boolean approved, @Size(max = 1000) String comment) {}
