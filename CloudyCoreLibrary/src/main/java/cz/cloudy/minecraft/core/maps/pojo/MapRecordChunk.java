/*
  User: Cloudy
  Date: 02/02/2022
  Time: 02:58
*/

package cz.cloudy.minecraft.core.maps.pojo;

import cz.cloudy.minecraft.core.componentsystem.annotations.CheckConfiguration;
import cz.cloudy.minecraft.core.data_transforming.transformers.Int2ToStringTransform;
import cz.cloudy.minecraft.core.database.DatabaseEntity;
import cz.cloudy.minecraft.core.database.annotation.*;
import cz.cloudy.minecraft.core.types.Int2;

/**
 * @author Cloudy
 */
@Table("__map_record_chunk")
@CheckConfiguration("maps.mapController=true")
public class MapRecordChunk
        extends DatabaseEntity {

    @Column("map_record")
    @ForeignKey
//    @Index
//    @MultiIndex(0)
    protected MapRecord mapRecord;

    @Column("chunk_position")
    @Transform(Int2ToStringTransform.class)
    @Size(16)
//    @Index
//    @MultiIndex(0)
    protected Int2 chunkPosition;

    public MapRecord getMapRecord() {
        return mapRecord;
    }

    public void setMapRecord(MapRecord mapRecord) {
        this.mapRecord = mapRecord;
    }

    public Int2 getChunkPosition() {
        return chunkPosition;
    }

    public void setChunkPosition(Int2 chunkPosition) {
        this.chunkPosition = chunkPosition;
    }
}
