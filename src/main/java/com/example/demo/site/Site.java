package com.example.demo.site;

import com.example.demo.common.BaseAuditableEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "sites", indexes = {
        @Index(name = "idx_site_name", columnList = "siteName")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_site_name_location", columnNames = {"siteName", "location"})
})
public class Site extends BaseAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String siteName;
    @Column(nullable = false)
    private String location;
    @Column(nullable = false)
    private boolean active = true;

    public Long getId() { return id; }
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
