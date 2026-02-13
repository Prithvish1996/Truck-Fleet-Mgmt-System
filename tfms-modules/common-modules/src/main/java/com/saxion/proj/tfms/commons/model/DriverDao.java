package com.saxion.proj.tfms.commons.model;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "drivers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class DriverDao extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;

    // Link to user account (one-to-one)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true, referencedColumnName = "id")
    private UserDao user;

    // Driver base location
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "location_id")
    private LocationDao location;

    // Routes assigned to this driver
    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<RouteDao> routes = new ArrayList<>();

    // assignment to this driver
    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<DriverTruckAssignmentDao> assignments = new ArrayList<>();

    // availability date
    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<DriverAvailabilityDao> availabilities = new ArrayList<>();

    // availability date
    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<DriverSuggestionDao> suggestions = new ArrayList<>();
}