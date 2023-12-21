package boozilla.houston.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table
@Data
public class Manifest {
    @Id
    @Column("name")
    private String id;
    private String name;
    private String commitId;
    private byte[] data;
}
