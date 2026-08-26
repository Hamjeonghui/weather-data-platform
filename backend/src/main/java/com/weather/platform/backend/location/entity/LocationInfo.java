package com.weather.platform.backend.location.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "location_info")
public class LocationInfo {

    @Id
    @Column(name = "reg_id")
    private String regId;

    @Column(name = "location_id")
    private String locationId;

    @Column(name = "stn_ko", nullable = false)
    private String stnKo;

    @Column(name = "nx")
    private Long nx;

    @Column(name = "ny")
    private Long ny;

    protected LocationInfo() {
    }

    public LocationInfo(String regId, String locationId, String stnKo, Long nx, Long ny) {
        this.regId = regId;
        this.locationId = locationId;
        this.stnKo = stnKo;
        this.nx = nx;
        this.ny = ny;
    }

    public String getRegId() {
        return regId;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getStnKo() {
        return stnKo;
    }

    public Long getNx() {
        return nx;
    }

    public Long getNy() {
        return ny;
    }
}
