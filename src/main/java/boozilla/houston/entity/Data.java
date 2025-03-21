package boozilla.houston.entity;

import boozilla.houston.asset.AssetContainer;
import boozilla.houston.asset.Scope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Table
@lombok.Data
public class Data {
    @Id
    private Long id;
    private String commitId;
    private Scope scope;
    private String name;
    private String path;
    private String checksum;
    private LocalDateTime applyAt;

    public AssetContainer.Key key()
    {
        return new AssetContainer.Key(this.getSheetName(), this.getPartitionName(), this.getScope());
    }

    public String getSheetName()
    {
        return this.name.split("#", 2)[0];
    }

    public Optional<String> getPartitionName()
    {
        final var split = this.name.split("#", 2);

        if(split.length < 2)
        {
            return Optional.empty();
        }

        return Optional.of(split[1]);
    }
}
