package org.mcguppy.eventplaner.jpa.entities;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author stefan meichtry
 */
@Entity
@Table(name = "staffmember")
public class StaffMember implements Serializable, Comparable {

    private static final long serialVersionUID = 1L;

    public enum Title {

        Herr,
        Frau;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Title title;
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    private String street;
    @Column(nullable = false)
    private String zip;
    @Column(nullable = false)
    private String city;
    private String phoneNr;
    private String cellPhoneNr;
    private String mailAddress;
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "staffMembers")
    private Collection<Shift> shifts;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "responsible")
    private Collection<Shift> responsibleShifts;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCellPhoneNr() {
        return cellPhoneNr;
    }

    public void setCellPhoneNr(String cellPhoneNr) {
        this.cellPhoneNr = cellPhoneNr;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMailAddress() {
        return mailAddress;
    }

    public void setMailAddress(String mailAddress) {
        this.mailAddress = mailAddress;
    }

    public String getPhoneNr() {
        return phoneNr;
    }

    public void setPhoneNr(String phoneNr) {
        this.phoneNr = phoneNr;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public Collection<Shift> getShifts() {
        return shifts;
    }

    public int getShiftsSize() {
        return shifts.size();
    }

    public void setShifts(Collection<Shift> shifts) {
        this.shifts = shifts;
    }

    public Collection<Shift> getResponsibleShifts() {
        return responsibleShifts;
    }

    public void setResponsibleShifts(Collection<Shift> responsibleShifts) {
        this.responsibleShifts = responsibleShifts;
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
        if (!(object instanceof StaffMember)) {
            return false;
        }
        StaffMember other = (StaffMember) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return lastName + " " + firstName + " " + city;
    }

    public int compareTo(Object o) {
        StaffMember staffMember = (StaffMember) o;
        return (this.lastName + this.firstName).compareTo(staffMember.lastName + staffMember.firstName);
    }
}
