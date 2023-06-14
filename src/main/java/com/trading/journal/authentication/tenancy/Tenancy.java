package com.trading.journal.authentication.tenancy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity
@Table(name = "Tenancy")
public class Tenancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Builder.Default
    private Integer userLimit = 1;

    @Builder.Default
    private Integer userUsage = 0;

    @Builder.Default
    private Boolean enabled = true;

    public void disable() {
        this.enabled = false;
    }

    public void enable() {
        this.enabled = true;
    }

    public void newLimit(Integer limit) {
        this.userLimit = limit;
    }

    public void lowerUsage() {
        if (userUsage > 0) {
            userUsage--;
        }
    }

    public void increaseUsage() {
        if (userUsage < userLimit) {
            userUsage++;
        }
    }

    public boolean increaseUsageAllowed() {
        return userLimit > userUsage;
    }
}
