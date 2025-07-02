package com.lic.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Table(name = "students",
        uniqueConstraints = @UniqueConstraint(columnNames = "pan_number"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sr_no")
    private String srNo;

    @Column(nullable = false)
    private String name;

    @Column(name = "pan_number", nullable = false, length = 10)
    private String panNumber;

    @Column(name = "lic_regd_number")
    private String licRegdNumber;

    private String branch;

    @Column(name = "start_date")
    private String startDate;

    @Column(name = "end_date")
    private String endDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @Column(name = "last_upload", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean lastUpload;
}