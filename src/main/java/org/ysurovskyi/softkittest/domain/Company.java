package org.ysurovskyi.softkittest.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "company")
@Builder
@Audited
public class Company {
    @Id
    private String symbol;
    @Column
    private String companyName;
    @Column
    private Long latestUpdate;
    @Column
    private BigDecimal latestPrice;
    @Column
    private BigDecimal high;
    @Column
    private BigDecimal low;
}
