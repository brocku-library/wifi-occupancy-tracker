package brockwifi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serial;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonAlias;

@Entity
@Table(name = "patron_count")
public class PatronCount {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @JsonAlias({ "_start_date" })
    @Column(columnDefinition = "TIMESTAMP")
    private Date dateTime;

    @JsonAlias({ "Floor" })
    private int floor;

    @JsonAlias({ "Count" })
    private int count;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public int getFloor() {
        return floor;
    }

    public String getFloorStr() {
        return switch (floor) {
            case 2 -> "Main Floor";
            case 3 -> "3rd Floor";
            case 4 -> "4th Floor";
            case 5 -> "5th Floor";
            case 6 -> "6th Floor";
            case 7 -> "7th Floor";
            case 8 -> "8th Floor";
            case 9 -> "9th Floor";
            case 10 -> "10th Floor";
            case 11 ->"11th Floor";
            case 20 -> "Makerspace";
            default -> "";
        };
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
