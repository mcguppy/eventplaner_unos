package org.mcguppy.eventplaner.jpa.entities;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author stefan meichtry
 */
@Entity
@Table(name="shift")
public class Shift implements Serializable, Comparable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private int staffNr;
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;
    private String description;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Location location;
    @ManyToMany(fetch = FetchType.LAZY)
    Collection<StaffMember> staffMembers;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Collection<StaffMember> getStaffMembers() {
        return staffMembers;
    }

    public void setStaffMembers(Collection<StaffMember> staffMembers) {
        this.staffMembers = staffMembers;
    }

    public int getStaffMembersSize() {
        return staffMembers.size();
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Shift)) {
            return false;
        }
        Shift other = (Shift) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.mcguppy.eventplaner.jpa.entity.Shift[id=" + id + "]";
    }

    public int compareTo(Object o) {
        Shift shift = (Shift) o;
        if (this.startTime.compareTo(shift.startTime) == 0) {
            return this.endTime.compareTo(shift.endTime);
        }
        return this.startTime.compareTo(shift.startTime);
    }
}
